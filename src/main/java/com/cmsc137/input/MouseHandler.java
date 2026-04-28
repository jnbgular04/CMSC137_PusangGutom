package com.cmsc137.input;

import com.cmsc137.engine.GameManager;
import com.cmsc137.entities.Mouse;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
}
