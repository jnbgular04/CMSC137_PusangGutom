package com.cmsc137.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.cmsc137.entities.Mouse;
import com.cmsc137.network.ClientConnection;
import com.cmsc137.ui.ScreenManager;

/**
 * GameManager tracks the score, current game state, and mouse spawning logic.
 */
public class GameManager {

    private ScreenManager screenManager;
    private Random randomGenerator;
    
    // Game State Variables
    private int score;
    private int timeLeftSeconds;
    private boolean isGameOver;
    private boolean isGameActive;
    private int nextMouseId = 1;
    
    // Timing Variables (Based on 60FPS tick rate)
    private int frameCounter = 0;
    private int postGameTimerFrames = 0;
    private final int FPS = 60;
    private int spawnTimerFrames = 0;
    
    // Spawning Constants ("The Pit" Boundaries)
    private final int PIT_MIN_X = 200;
    private final int PIT_MAX_X = 1000;
    private final int PIT_MIN_Y = 200;
    private final int PIT_MAX_Y = 440;
    
    // Entity Tracking
    private List<Mouse> activeMice;

    private int[] multiplayerScores = new int[4];
    private int[] connectedPlayers = new int[] { 1 };
    private ClientConnection clientConnection;
    private int localPlayerId = 1;
    private final int[] pawTargetX = new int[4];
    private final int[] pawTargetY = new int[4];
    private static final int PIT_CENTER_X = 600;
    private static final int PIT_CENTER_Y = 320;

    
    
    public GameManager(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.randomGenerator = new Random();
        this.activeMice = new ArrayList<>();
    }
    
    /**
     * Initializes or resets the game state for a new session.
     */
    public void startGame() {
        this.score = 0;
        this.timeLeftSeconds = 60; // 1-minute countdown
        this.isGameOver = false;
        this.isGameActive = true;
        this.frameCounter = 0;
        this.postGameTimerFrames = 0;
        this.activeMice.clear();
        this.nextMouseId = 1;
        this.multiplayerScores = new int[4];
        resetPawTargets();
        if (!isMultiplayerMode) {
            this.connectedPlayers = new int[] { 1 };
        }
        
        System.out.println("Game Started! 60 Seconds on the clock.");
    }
    
    /**
     * Called 60 times per second by the GameLoop.
     */
    public void tick() {
        if (!isGameActive) return;
        
        frameCounter++;
        
        // Handle active gameplay logic
        if (!isGameOver) {
            // Countdown timer (same for singleplayer and multiplayer)
            if (frameCounter >= FPS) {
                timeLeftSeconds--;
                frameCounter = 0;
                
                if (timeLeftSeconds <= 0) {
                    triggerGameOver();
                }
            }
            
            // Local spawn only — multiplayer mice come from the server
            if (!isMultiplayerMode) {
                spawnTimerFrames++;
                if (spawnTimerFrames >= (FPS / 2)) { // Spawns every 0.5 seconds
                    spawnMouse();
                    spawnTimerFrames = 0;
                }
            }
        }
        // Handle Post-Game "Session Result" Delay (5 seconds)
        else {
            postGameTimerFrames++;
            if (postGameTimerFrames >= (5 * FPS)) { // 5 seconds * 60 FPS
                endSessionAndReturnToMenu();
            }
        }
    }
    
    /**
     * Generates a new Mouse entity within the locked Pit dimensions.
     */
    private void spawnMouse() {
        int randomX = randomGenerator.nextInt((PIT_MAX_X - PIT_MIN_X) + 1) + PIT_MIN_X;
        int randomY = randomGenerator.nextInt((PIT_MAX_Y - PIT_MIN_Y) + 1) + PIT_MIN_Y;

        // Pass the nextMouseId and then increment it
        Mouse newMouse = new Mouse(nextMouseId++, randomX, randomY); 
        activeMice.add(newMouse);

        System.out.println("Spawned Mouse ID [" + (nextMouseId-1) + "] at X:" + randomX + " Y:" + randomY);
    }
    
    /**
     * Triggers the Game Over state.
     */
    private void triggerGameOver() {
        isGameOver = true;
        activeMice.clear(); // Despawn all mice
        System.out.println("Game Over! Final Score: " + score);
        System.out.println("Displaying results for 5 seconds...");
    }
    
    /**
     * Exits the game state and tells ScreenManager to swap back to the Main Menu.
     */
    private void endSessionAndReturnToMenu() {
        isGameActive = false;
        System.out.println("Returning to Main Menu.");
        
        SwingUtilities.invokeLater(() -> {
            screenManager.showMainMenu();
        });
    }
    
    // --- Integration Methods for Yanika & Jaz ---
    
    public void incrementScore() {
        if (!isGameOver) {
            score++;
        }
    }
    
    public int getScore() { return score; }
    public int getTimeLeft() { return timeLeftSeconds; }
    public boolean getIsGameOver() { return isGameOver; }
    public List<Mouse> getActiveMice() { return activeMice; }
    public int[] getMultiplayerScores() { return multiplayerScores.clone(); }
    public int[] getConnectedPlayers() { return connectedPlayers.clone(); }
    public int getLocalPlayerId() { return localPlayerId; }

    private boolean isMultiplayerMode = false;

    public void setMultiplayerMode(boolean active) {
        this.isMultiplayerMode = active;
    }

    public boolean isMultiplayerMode() {
        return isMultiplayerMode;
    }

    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public void setLocalPlayerId(int playerId) {
        this.localPlayerId = Math.max(1, Math.min(4, playerId));
    }

    public void sendMultiplayerClick(int x, int y) {
        if (isMultiplayerMode && clientConnection != null) {
            clientConnection.sendClick(x, y);
        }
    }

    /** Local paw follows cursor immediately; network sync is throttled in ClientConnection. */
    public void updateLocalPawPosition(int x, int y) {
        if (!isMultiplayerMode || isGameOver) {
            return;
        }
        triggerNetworkedPawStretch(localPlayerId, x, y);
        if (clientConnection != null) {
            clientConnection.sendPawMove(x, y);
        }
    }

    // Networked State Mutators 
    public void addNetworkedMouse(Mouse mouse) {
        this.activeMice.add(mouse);
    }

    public void triggerNetworkedPawStretch(int playerId, int targetX, int targetY) {
        int idx = playerId - 1;
        if (idx < 0 || idx >= 4) {
            return;
        }
        pawTargetX[idx] = targetX;
        pawTargetY[idx] = targetY;
    }

    public int[] getPawTarget(int playerId) {
        int idx = Math.max(0, Math.min(3, playerId - 1));
        return new int[] { pawTargetX[idx], pawTargetY[idx] };
    }

    private void resetPawTargets() {
        for (int i = 0; i < 4; i++) {
            pawTargetX[i] = PIT_CENTER_X;
            pawTargetY[i] = PIT_CENTER_Y;
        }
    }

    public void updateNetworkedScores(int[] scores) {
        this.multiplayerScores = scores;
    }

    public void updateConnectedPlayers(int[] players) {
        if (players == null || players.length == 0) {
            this.connectedPlayers = new int[] { 1 };
            return;
        }
        this.connectedPlayers = players.clone();
    }

    public void triggerNetworkedGameOver(int winnerId) {
        this.isGameOver = true;
        this.activeMice.clear();
        System.out.println("Game Over! Player " + winnerId + " wins!");
    }

    
}