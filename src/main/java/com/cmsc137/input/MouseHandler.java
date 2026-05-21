package com.cmsc137.input;

import com.cmsc137.engine.GameManager;
import com.cmsc137.entities.Mouse;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

/**
 * MouseHandler for listening to clicks.
 */
public class MouseHandler extends MouseAdapter {
    private GameManager gameManager;
    private final int clickPadding = 15;
    
    // Link MouseHandler to GameManager
    public MouseHandler(GameManager gameManager) {
    	this.gameManager = gameManager;
    }
    
    @Override
    // Event Listener Method
    public void mousePressed(MouseEvent e) {
    	// If timer runs out, stop execution
    	if (gameManager.getIsGameOver()) {
    		return;
    	}
    	
    	int clickX = e.getX();
    	int clickY = e.getY();

        // Multiplayer uses server-authoritative hit/score resolution.
        if (gameManager.isMultiplayerMode()) {
            // VISUAL LOCK: Ignore clicks if already stretching
            if (gameManager.isLocalCatAnimating()) {
                return;
            }
            gameManager.sendMultiplayerClick(clickX, clickY);
            return;
        }
    	List<Mouse> activeMice = gameManager.getActiveMice();
    	
    	// Get ID from CollisionMath
    	int hitMouseId = CollisionMath.getHitEntity(clickX, clickY, activeMice, clickPadding);
    	
    	// If ID is found
    	if (hitMouseId != -1) {
    		gameManager.incrementScore();
    		
    		// Find mice in list and remove
    		for (int i =0; i < activeMice.size(); i++) {
    			if (activeMice.get(i).id == hitMouseId) {
    				activeMice.get(i).isActive = false;
    				activeMice.remove(i);
    				break;
    			}
    		}
    		
    		System.out.println("Hit registered on Mouse ID: " + hitMouseId);
    	}
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameManager.getIsGameOver()) {
            return;
        }
        // Continuous tracking disabled in multiplayer to prevent flooding
        if (!gameManager.isMultiplayerMode()) {
            // Local singleplayer behavior (handled by GameStage.getMousePosition())
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
}
