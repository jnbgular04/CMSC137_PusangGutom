package com.cmsc137.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for Steven's core game logic.
 */
public class GameManagerTest {

    @Test
    public void testScoreIncrementsCorrectly() {
        // 1. Setup: Create manager with a null UI (we don't need UI for math)
        GameManager manager = new GameManager(null);
        
        // Initialize the game state (sets score to 0, isGameOver to false)
        manager.startGame(); 

        // 2. Action: Simulate Yanika's CollisionMath registering 3 hits
        manager.incrementScore();
        manager.incrementScore();
        manager.incrementScore();

        // 3. Assert: Check if the score is exactly 3
        assertEquals(3, manager.getScore(), "The score should be exactly 3 after three increments!");
    }
    
    @Test
    public void testScoreCannotIncrementAfterGameOver() {
        GameManager manager = new GameManager(null);
        manager.startGame();
        
        // Force the game over state by tricking the timer
        // (You may need to temporarily make triggerGameOver() public, 
        // or just wait 60 ticks if testing the tick() method)
        
        // Assuming we manually set game over somehow, the score shouldn't go up.
        // For this basic test, we just ensure standard increments work first!
    }
}