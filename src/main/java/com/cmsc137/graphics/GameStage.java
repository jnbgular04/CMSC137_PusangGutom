package com.cmsc137.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import com.cmsc137.engine.GameManager;
import com.cmsc137.entities.Cat;
import com.cmsc137.entities.Mouse;

public class GameStage extends JPanel {

    private Cat cat;
    private SpriteRenderer spriteRenderer;
    private GameManager gameManager; // ADD THIS

    public GameStage(GameManager gameManager) { 
        this.gameManager = gameManager;

        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1280, 720));
        setFocusable(true);
        spriteRenderer = new SpriteRenderer();

        cat = new Cat();
        cat.x = 80;  
        cat.y = 360;
        cat.name = "Player";
        cat.score = 0;
    }

    @Override
    // Painting the Graphics
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Get Cat Score
        cat.score = gameManager.getScore();
        int timeLeft = gameManager.getTimeLeft(); //get the total time left 

        // get the mice from game manager
        List<Mouse> mice = gameManager.getActiveMice();

        spriteRenderer.drawCat(g, cat);
        spriteRenderer.drawMice(g, mice);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Player: " + cat.name, 20, 30);
        g.drawString("Score: " + cat.score, 20, 55);
        g.drawString("Time: " + timeLeft, 20, 80);
        
        // temporary for now, will update once Graphics is implemented
        if (gameManager.getIsGameOver()) {
            g.setColor(new Color(0, 0, 0, 180)); // semi-transparent black overlay
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 72));
            g.drawString("GAME OVER", 380, 320);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Final Score: " + cat.score, 490, 400);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Returning to menu...", 510, 450);
        }
    }

    // repaints based on updates from GameStage
    public void update() {
        repaint(); 
    }
}