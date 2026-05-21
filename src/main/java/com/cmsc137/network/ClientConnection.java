package com.cmsc137.network;

import com.cmsc137.engine.GameManager;
import com.cmsc137.entities.Cat;
import com.cmsc137.entities.Mouse;
import com.cmsc137.ui.ScreenManager;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientConnection implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;
    
    private int localPlayerID = -1;
    private GameManager gameManager;
    private ScreenManager screenManager;
    private long lastPawMoveSentMs = 0;
    private static final long PAW_MOVE_INTERVAL_MS = 33; // ~30 updates/sec

    public ClientConnection(GameManager gameManager, ScreenManager screenManager) {
        this.gameManager = gameManager;
        this.screenManager = screenManager;
    }

    public void connect(String ipAddress) {
        try {
            if (isConnected) {
                return;
            }
            socket = new Socket(ipAddress, 4444);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;
            
            new Thread(this).start(); // Start listening for server broadcasts
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }
    
    public int getLocalPlayerID() {
        return this.localPlayerID;
    }

    public void sendClick(int x, int y) {
        if (isConnected && localPlayerID != -1) {
            out.println(NetworkProtocol.formatClickEvent(localPlayerID, x, y));
        }
    }

    public void sendPawMove(int x, int y) {
        if (!isConnected || localPlayerID == -1) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastPawMoveSentMs < PAW_MOVE_INTERVAL_MS) {
            return;
        }
        lastPawMoveSentMs = now;
        out.println(NetworkProtocol.formatPawMove(localPlayerID, x, y));
    }
    
    public void sendHostStart() {
        if (isConnected && localPlayerID == 1) {
            out.println(NetworkProtocol.HOST_START);
        }
    }

    public void disconnect() {
        try {
            if (out != null && isConnected) {
                out.println(NetworkProtocol.DISCONNECT);
            }
            isConnected = false;
            localPlayerID = -1;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                parseServerMessage(message);
            }
        } catch (IOException e) {
            isConnected = false;
            System.out.println("Disconnected from Server.");
        }
    }

    private void parseServerMessage(String message) {
        String[] tokens = message.split(",");
        String command = tokens[0];

        switch (command) {
            case NetworkProtocol.ASSIGN_ID:
                localPlayerID = Integer.parseInt(tokens[1]);
                System.out.println("Assigned Player ID: " + localPlayerID);
                if (screenManager != null) {
                    screenManager.onAssignedPlayerId(localPlayerID);
                }
                break;

            case NetworkProtocol.START_GAME:
                screenManager.showGame(true); // Triggers the swap to the actual game
                break;

            case NetworkProtocol.SPAWN_MOUSE:
                int mouseId = Integer.parseInt(tokens[1]);
                int mX = Integer.parseInt(tokens[2]);
                int mY = Integer.parseInt(tokens[3]);
                gameManager.addNetworkedMouse(new Mouse(mouseId, mX, mY));
                break;

            case NetworkProtocol.PAW_STRETCH:
                int pId = Integer.parseInt(tokens[1]);
                int tX = Integer.parseInt(tokens[2]);
                int tY = Integer.parseInt(tokens[3]);
                // Hand-off to Jaz: Trigger the stretching animation for this specific Cat ID
                gameManager.triggerNetworkedPawStretch(pId, tX, tY);
                break;

            case NetworkProtocol.SCORE_UPDATE:
                // Expected format: SCORE_UPDATE,p1Score,p2Score,p3Score,p4Score
                int[] newScores = {
                    Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), 
                    Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4])
                };
                gameManager.updateNetworkedScores(newScores);
                break;

            case NetworkProtocol.GAME_OVER:
                int winnerId = Integer.parseInt(tokens[1]);
                gameManager.triggerNetworkedGameOver(winnerId);
                break;
                
            case NetworkProtocol.PLAYER_DISCONNECT:
                // Fallback to menu if someone drops
                if (screenManager != null) {
                    screenManager.showMainMenu();
                }
                break;

            case NetworkProtocol.LOBBY_UPDATE:
                if (screenManager != null) {
                    int[] connectedPlayers = new int[Math.max(0, tokens.length - 1)];
                    for (int i = 1; i < tokens.length; i++) {
                        connectedPlayers[i - 1] = Integer.parseInt(tokens[i]);
                    }
                    screenManager.onLobbyUpdate(connectedPlayers);
                }
                break;
        }
    }

    // Add this temporarily to ClientConnection.java for quick testing
    public static void main(String[] args) {
        // Create a dummy GameManager and ScreenManager just so the parser doesn't crash
        com.cmsc137.engine.GameManager dummyManager = new com.cmsc137.engine.GameManager(null);
        ClientConnection testingClient = new ClientConnection(dummyManager, null);
        
        System.out.println("Testing Mode: Attempting to connect to localhost...");
        testingClient.connect("127.0.0.1"); 
    }
}