package com.cmsc137.entities;

public class Cat {
    public int x, y;
    public int score;
    public String name;
    
    // Animation state variables
    public int animTargetX;
    public int animTargetY;
    public boolean isAnimating;
    
    
    // For MS2, this will eventually hold the player's unique ID
    public int id;
}