package com.cmsc137.entities;

public class Cat {
    public int x, y; // Base/Anchor position
    public int score;
    public String name;
    
    // Animation state variables
    public int animTargetX;
    public int animTargetY;
    public boolean isAnimating;
    public float animProgress = 0.0f; // 0.0 to 1.0
    public boolean isRetracting = false;
    public static final float ANIM_SPEED = 0.15f; // Speed of the stretch
    
    // For MS2, this will eventually hold the player's unique ID
    public int id;

    public void updateAnimation() {
        if (!isAnimating) return;

        if (!isRetracting) {
            animProgress += ANIM_SPEED;
            if (animProgress >= 1.0f) {
                animProgress = 1.0f;
                isRetracting = true;
            }
        } else {
            animProgress -= ANIM_SPEED;
            if (animProgress <= 0.0f) {
                animProgress = 0.0f;
                isRetracting = false;
                isAnimating = false;
            }
        }
    }

    public int getAnimatedX(int targetX) {
        return (int) (x + (targetX - x) * animProgress);
    }

    public int getAnimatedY(int targetY) {
        return (int) (y + (targetY - y) * animProgress);
    }
}