package com.cmsc137.network;

import com.cmsc137.entities.Mouse;
import com.cmsc137.input.CollisionMath; // Assuming Yanika made this stateless

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GameServer {
    private static final int PORT = 4444;
    private static final int WIN_THRESHOLD = 30;
    
    // The Pit Boundaries
    private static final int PIT_MIN_X = 200;
    private static final int PIT_MAX_X = 1000;
    private static final int PIT_MIN_Y = 200;
    private static final int PIT_MAX_Y = 440;

    private ServerSocket serverSocket;
    private final Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> scores = new ConcurrentHashMap<>();
    private final List<Mouse> activeMice = new CopyOnWriteArrayList<>();
    
    private boolean isGameStarted = false;
    private int nextMouseId = 1;
    private long lastActivityTime = System.currentTimeMillis();

    public static void main(String[] args) {
        new GameServer().start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("GameServer initialized on port " + PORT);

            // Heartbeat thread to kill ghost processes
            startHeartbeatMonitor();

            // Accept up to 4 clients
            while (clients.size() < 4) {
                Socket clientSocket = serverSocket.accept();
                int playerId = clients.size() + 1; // 1-4 assignment
                
                ClientHandler handler = new ClientHandler(clientSocket, playerId);
                clients.put(playerId, handler);
                scores.put(playerId, 0); // Initialize score
                
                new Thread(handler).start();
                
                System.out.println("Player " + playerId + " connected.");
                lastActivityTime = System.currentTimeMillis();
                broadcastLobbyState();
            }

            System.out.println("Lobby full. Waiting for HOST_START...");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startHeartbeatMonitor() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000); // Check every 10 seconds
                    if (clients.isEmpty() && (System.currentTimeMillis() - lastActivityTime > 60000)) {
                        System.out.println("Server idle for 60 seconds. Shutting down.");
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private synchronized void startGameLoop() {
        if (isGameStarted) return;
        isGameStarted = true;
        broadcast(NetworkProtocol.START_GAME);

        new Thread(() -> {
            Random random = new Random();
            while (isGameStarted) {
                try {
                    Thread.sleep(random.nextInt(1000) + 500); // Spawn interval
                    
                    int x = random.nextInt((PIT_MAX_X - PIT_MIN_X) + 1) + PIT_MIN_X;
                    int y = random.nextInt((PIT_MAX_Y - PIT_MIN_Y) + 1) + PIT_MIN_Y;
                    
                    Mouse newMouse = new Mouse(nextMouseId++, x, y);
                    activeMice.add(newMouse);
                    
                    broadcast(NetworkProtocol.formatSpawnMouse(newMouse.id, x, y));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private synchronized void processClick(int playerId, int x, int y) {
        if (!isGameStarted) return;

        // 1. Immediately broadcast the PAW_STRETCH for visual feedback
        broadcast(NetworkProtocol.formatPawStretch(playerId, x, y));

        // 2. Validate hit using Yanika's CollisionMath
        int hitId = CollisionMath.getHitEntity(x, y, activeMice, 15);
        
        if (hitId != -1) {
            // Remove mouse
            activeMice.removeIf(m -> m.id == hitId);
            
            // Increment score
            int newScore = scores.get(playerId) + 1;
            scores.put(playerId, newScore);
            
            // Broadcast Score
            broadcast(NetworkProtocol.formatScoreUpdate(
                scores.getOrDefault(1, 0), scores.getOrDefault(2, 0),
                scores.getOrDefault(3, 0), scores.getOrDefault(4, 0)
            ));

            // Check Win Condition
            if (newScore >= WIN_THRESHOLD) {
                isGameStarted = false;
                broadcast(NetworkProtocol.GAME_OVER + "," + playerId);
            }
        }
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    private void broadcastLobbyState() {
        List<Integer> sortedPlayers = new ArrayList<>(clients.keySet());
        Collections.sort(sortedPlayers);
        String csv = sortedPlayers.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        broadcast(NetworkProtocol.formatLobbyUpdate(csv));
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private int playerId;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
        }

        public void sendMessage(String msg) {
            if (out != null) {
                out.println(msg);
            }
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Initial handshake
                sendMessage(NetworkProtocol.ASSIGN_ID + "," + playerId);

                String line;
                while ((line = in.readLine()) != null) {
                    lastActivityTime = System.currentTimeMillis();
                    String[] tokens = line.split(",");
                    String command = tokens[0];

                    switch (command) {
                        case NetworkProtocol.HOST_START:
                            if (playerId == 1) startGameLoop(); // Only host can start
                            break;
                        case NetworkProtocol.CLICK_EVENT:
                            int x = Integer.parseInt(tokens[2]);
                            int y = Integer.parseInt(tokens[3]);
                            processClick(playerId, x, y);
                            break;
                        case NetworkProtocol.PAW_MOVE:
                            int mx = Integer.parseInt(tokens[2]);
                            int my = Integer.parseInt(tokens[3]);
                            if (isGameStarted) {
                                broadcast(NetworkProtocol.formatPawStretch(playerId, mx, my));
                            }
                            break;
                        case NetworkProtocol.DISCONNECT:
                            handleDisconnect();
                            return;
                    }
                }
            } catch (IOException e) {
                handleDisconnect();
            }
        }

        private void handleDisconnect() {
            clients.remove(playerId);
            isGameStarted = false;
            broadcast(NetworkProtocol.PLAYER_DISCONNECT);
            broadcastLobbyState();
            System.out.println("Player " + playerId + " disconnected.");
        }
    }
}