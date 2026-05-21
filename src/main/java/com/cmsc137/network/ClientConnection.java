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
        if (isConnected) {
            return;
        }

        // Run the blocking connection logic on a separate thread to prevent UI freezing
        new Thread(() -> {
            try {
                // Initialize an unbound socket instance
                socket = new java.net.Socket();
                
                // Set an explicit connection timeout limit (2000 milliseconds = 2 seconds)
                socket.connect(new java.net.InetSocketAddress(ipAddress, 4444), 2000);
                
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isConnected = true;
                
                // Start listening loop for incoming server data broadcasts
                new Thread(this, "ClientListenThread").start(); 
                
            } catch (IOException e) {
                System.out.println("Failed to connect to server: " + e.getMessage());
                isConnected = false;
                
                // Safely notify the user interface on the Event Dispatch Thread (EDT)
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (screenManager != null) {
                        // Display a clean error popup alert dialog window
                        javax.swing.JOptionPane.showMessageDialog(
                            null, 
                            "Could not reach the host room. Please check the Room Code and try again.", 
                            "Connection Timeout", 
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                        
                        // Force the UI view state machine layout back to the entry menu
                        screenManager.showMainMenu(); 
                    }
                });
            }
        }, "ClientConnectionThread").start();
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

            case NetworkProtocol.REMOVE_MOUSE:
                int removeId = Integer.parseInt(tokens[1]);
                gameManager.removeNetworkedMouse(removeId);
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

            case NetworkProtocol.EXIT_TO_MENU: 
                if (screenManager != null) {
                    screenManager.showMainMenu(); 
                }
                break;
            
            case "CHAT":
                int speakerId = Integer.parseInt(tokens[1]);
                // Recombine tokens past index 1 just in case spaces were split
                String chatBody = tokens[2]; 
                
                if (screenManager != null && screenManager.getLobbyPanel() != null) {
                    screenManager.getLobbyPanel().appendChatMessage(speakerId, chatBody);
                }
                break;
        }
    }

    public void sendExitToMenu() {
        // Only the host is allowed to kill the lobby
        if (isConnected && localPlayerID == 1) {
            out.println("EXIT_TO_MENU"); // Assuming this is defined in NetworkProtocol
        }
    }

    public void sendChatMessage(String message) {
        // Clean out commas from the message body so it doesn't break our token parsing arrays!
        String sanitizedMessage = message.replace(",", " ");
        if (isConnected && localPlayerID != -1) {
            out.println("CHAT," + localPlayerID + "," + sanitizedMessage);
        }
    }

    public void setLocalPlayerID(int id) {
        this.localPlayerID = id;
    }
}