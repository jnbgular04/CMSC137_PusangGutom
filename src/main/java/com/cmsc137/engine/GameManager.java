package com.cmsc137.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.cmsc137.entities.Mouse;
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
    
    // Timing Variables (Based on 60FPS tick rate)
    private int frameCounter = 0;
    private int postGameTimerFrames = 0;
    private final int FPS = 60;
    
    // Spawning Constants ("The Pit" Boundaries)
    private final int PIT_MIN_X = 200;
    private final int PIT_MAX_X = 1000;
    private final int PIT_MIN_Y = 200;
    private final int PIT_MAX_Y = 440;
    
    // Entity Tracking
    private List<Mouse> activeMice;
    
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
        this.timeLeftSeconds = 10; // 1-minute countdown
        this.isGameOver = false;
        this.isGameActive = true;
        this.frameCounter = 0;
        this.postGameTimerFrames = 0;
        this.activeMice.clear();
        
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
            // Every 60 frames = 1 real-world second
            if (frameCounter >= FPS) {
                timeLeftSeconds--;
                frameCounter = 0; // Reset frame counter for the next second
                
                // Example logic: Spawn a mouse every second
                spawnMouse();
                
                // Check Win/Loss Condition
                if (timeLeftSeconds <= 0) {
                    triggerGameOver();
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
        
        Mouse newMouse = new Mouse(randomX, randomY);
        activeMice.add(newMouse);
        System.out.println("Spawned Mouse at X:" + randomX + " Y:" + randomY);
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
}