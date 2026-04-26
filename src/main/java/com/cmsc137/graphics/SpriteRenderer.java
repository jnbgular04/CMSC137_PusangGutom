package com.cmsc137.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import com.cmsc137.entities.Cat;
import com.cmsc137.entities.Mouse;

public class SpriteRenderer {

    // Placeholder Sizes
    private static final int CAT_SIZE = 40;
    private static final int MOUSE_SIZE = 20;

    // Draws the cat, will replace with drawn graphics
    public void drawCat(Graphics g, Cat cat) {
        // Draw body
        g.setColor(new Color(255, 165, 0)); // Orange
        g.fillRect(cat.x - CAT_SIZE / 2, cat.y - CAT_SIZE / 2, CAT_SIZE, CAT_SIZE);

        // Draw outline
        g.setColor(Color.WHITE);
        g.drawRect(cat.x - CAT_SIZE / 2, cat.y - CAT_SIZE / 2, CAT_SIZE, CAT_SIZE);

        // Draw label
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(Color.WHITE);
    }

    // Generates the mouse, will replace with drawn graphics
    public void drawMice(Graphics g, List<Mouse> mice) {
        for (Mouse mouse : mice) {
            if (!mouse.isActive) continue;

            // Draw body
            g.setColor(new Color(180, 180, 180)); // Gray
            g.fillRect(mouse.x - MOUSE_SIZE / 2, mouse.y - MOUSE_SIZE / 2, MOUSE_SIZE, MOUSE_SIZE);

            // Draw outline
            g.setColor(Color.WHITE);
            g.drawRect(mouse.x - MOUSE_SIZE / 2, mouse.y - MOUSE_SIZE / 2, MOUSE_SIZE, MOUSE_SIZE);

            // Draw label
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.setColor(Color.WHITE);
            g.drawString("MOUSE", mouse.x - 15, mouse.y + MOUSE_SIZE);
        }
    }

}