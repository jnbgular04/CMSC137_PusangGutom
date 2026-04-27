package com.cmsc137.input;

import com.cmsc137.entities.Mouse;
import java.util.List;

/**
 * Collision Math for calculating hit detection.
 */
public class CollisionMath {
    
	private CollisionMath() {}
    
    public static Mouse getHitEntity(int clickX, int clickY, List<Mouse> mice, int padding) {
    	// Implement strict 80x80 padding improving responsiveness 
    	final int mouseWidth = 80;
    	final int mouseHeight = 80;
    	
    	for (int i =mice.size() - 1; i >= 0; i--) {
    		Mouse mouse = mice.get(i);
    		
    		// Skip inactive mice
    		if (!mouse.isActive) {
    			continue;
    		}
    		
    		// Calculate bounds
    		int leftBound = mouse.x - padding;
    		int rightBound = mouse.x + mouseWidth + padding;
    		int topBound = mouse.y - padding;
    		int bottomBound = mouse.y + mouseHeight + padding;
    		
    		// Check if click is within bounds
    		if (clickX >= leftBound && clickX <= rightBound && clickY >= topBound && clickY <= bottomBound) {
    			return mouse;
    		}
    	}
    	// No hits
    	return null; 
    }
}

