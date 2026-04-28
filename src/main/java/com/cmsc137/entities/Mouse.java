package com.cmsc137.entities;

/**
 * Dummy data model for target coordinates.
 * Holds X, Y coordinates and active state.
 */
public class Mouse {
	public int id;
    public int x;
    public int y;
    public boolean isActive;

    public Mouse(int id, int x, int y) {
    	this.id = id;
        this.x = x;
        this.y = y;
        this.isActive = true;
    }
}