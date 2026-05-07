package com.cmsc137.graphics;

import java.awt.Color;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.cmsc137.engine.GameManager;
import com.cmsc137.entities.Cat;
import com.cmsc137.entities.Mouse;
import com.cmsc137.input.MouseHandler;

public class GameStage extends JPanel {

    private Cat cat;
    private SpriteRenderer spriteRenderer;
    private GameManager gameManager; 
    private Image backgroundImage;

    public GameStage(GameManager gameManager) { 
        this.gameManager = gameManager;
        
        backgroundImage = new ImageIcon("assets/Game_Stage_BG.png").getImage();

        setPreferredSize(new Dimension(1280, 720));
        setFocusable(true);
        setFocusable(true);
        spriteRenderer = new SpriteRenderer();

        // WIRE THE INPUT 
        MouseHandler mouseHandler = new MouseHandler(gameManager);
        this.addMouseListener(mouseHandler);

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
        
        // fallback colors
        g.setColor(Color.decode("#f2cc92")); 
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        // Get Cat Score
        cat.score = gameManager.getScore();
        int timeLeft = gameManager.getTimeLeft(); //get the total time left 
        
     // Get current mouse position relative to this JPanel
        java.awt.Point cursorPos = getMousePosition();
        
     // Fallback coordinates if the mouse is outside the window
        int targetX = (cursorPos != null) ? cursorPos.x : cat.x + 100;
        int targetY = (cursorPos != null) ? cursorPos.y : cat.y;

        // get the mice from game manager
        List<Mouse> mice = gameManager.getActiveMice();
        spriteRenderer.drawMice(g, mice);
        spriteRenderer.drawCat(g, cat, targetX, targetY);
        
        
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