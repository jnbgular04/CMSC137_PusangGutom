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
    private boolean isMultiplayer;
    private final Color[] scoreboardColors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };

    public GameStage(GameManager gameManager, boolean isMultiplayer) { 
        this.gameManager = gameManager;
        this.isMultiplayer = isMultiplayer;
        
        backgroundImage = new ImageIcon("assets/Game_Stage_BG.png").getImage();

        setPreferredSize(new Dimension(1280, 720));
        setFocusable(true);
        setFocusable(true);
        spriteRenderer = new SpriteRenderer(gameManager.getLocalPlayerId());

        // WIRE THE INPUT 
        MouseHandler mouseHandler = new MouseHandler(gameManager);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);

        cat = new Cat();
        cat.x = 80;  
        cat.y = 360;
        cat.name = "Player";
        cat.score = 0;
    }

    public void setMultiplayerMode(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
    }

    public void setLocalPlayerId(int playerId) {
        spriteRenderer.setLocalPlayerId(playerId);
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
        
        int targetX = cat.x + 100;
        int targetY = cat.y;
        if (!isMultiplayer) {
            // Local mode keeps continuous cursor tracking for paw orientation.
            java.awt.Point cursorPos = getMousePosition();
            targetX = (cursorPos != null) ? cursorPos.x : targetX;
            targetY = (cursorPos != null) ? cursorPos.y : targetY;
        }

        // get the mice from game manager
        List<Mouse> mice = gameManager.getActiveMice();
        spriteRenderer.drawMice(g, mice);
        if (isMultiplayer) {
            drawMultiplayerCats(g);
        } else {
            spriteRenderer.drawCat(g, cat, targetX, targetY);
        }
        
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Time: " + timeLeft, 20, 35);
        drawScoreboard(g);
        
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

    private void drawMultiplayerCats(Graphics g) {
        int[] connectedPlayers = gameManager.getConnectedPlayers();
        int localId = gameManager.getLocalPlayerId();
        java.awt.Point cursor = getMousePosition();
        int fallbackX = getWidth() / 2;
        int fallbackY = getHeight() / 2;

        // Track which UI positions (1 to 4) have been filled
        boolean[] positionFilled = new boolean[5];

        // 1. Determine local player's rendering slot
        int localSlot = 1;
        for (int i = 0; i < connectedPlayers.length; i++) {
            if (connectedPlayers[i] == localId) {
                localSlot = i + 1; 
                break;
            }
        }
        if (localSlot > 4) localSlot = 1;

        // draw other players
        for (int i = 0; i < connectedPlayers.length; i++) {
            int remotePlayerId = connectedPlayers[i];
            
            // Skip drawing the local player during this first pass
            if (remotePlayerId == localId) {
                continue;
            }

            int assignedSlot = i + 1;
            if (assignedSlot > 4) continue; 

            // Draw the remote player cat at its assigned slot
            drawPlayerCatForSlot(g, remotePlayerId, assignedSlot, cursor, fallbackX, fallbackY);
            positionFilled[assignedSlot] = true;
        }

        // draw  local player
        drawPlayerCat(g, localId, cursor, fallbackX, fallbackY, new boolean[5]); 
        positionFilled[localSlot] = true;
    }
    
    private void drawPlayerCatForSlot(Graphics g, int playerId, int slotId, java.awt.Point cursor, int fallbackX, int fallbackY) {
        int[] anchor = getMultiplayerCatAnchor(slotId); // Use slot (1-4) for screen position
        int[] paw = gameManager.getPawTarget(playerId); // Use raw network ID to get coordinates
        
        int targetX = paw[0];
        int targetY = paw[1];

        // Pass the slotId to SpriteRenderer so it uses the right image rotation configuration
        spriteRenderer.drawCatForPlayer(g, slotId, anchor[0], anchor[1], targetX, targetY);
    }

    private void drawPlayerCat(Graphics g, int playerId, java.awt.Point cursor,
            int fallbackX, int fallbackY, boolean[] drawn) {
        if (playerId < 1 || playerId > 4 || drawn[playerId]) {
            return;
        }
        drawn[playerId] = true;
        int localId = gameManager.getLocalPlayerId();
        int[] anchor = getMultiplayerCatAnchor(playerId);
        int targetX;
        int targetY;
        if (playerId == localId) {
            targetX = (cursor != null) ? cursor.x : fallbackX;
            targetY = (cursor != null) ? cursor.y : fallbackY;
        } else {
            int[] paw = gameManager.getPawTarget(playerId);
            targetX = paw[0];
            targetY = paw[1];
        }
        spriteRenderer.drawCatForPlayer(g, playerId, anchor[0], anchor[1], targetX, targetY);
    }

    private int[] getMultiplayerCatAnchor(int playerId) {
        switch (playerId) {
            case 2:
                return new int[] { getWidth() / 2, 220 }; // top
            case 3:
                return new int[] { getWidth() - 80, getHeight() / 2 }; // right
            case 4:
                return new int[] { getWidth() / 2, getHeight() - 90 }; // bottom
            case 1:
            default:
                return new int[] { 80, getHeight() / 2 };      // left (singleplayer position)
        }
    }

    private void drawScoreboard(Graphics g) {
        int panelX = getWidth() - 280;
        int panelY = 20;
        int panelW = 240;
        int[] connectedPlayers = gameManager.getConnectedPlayers();
        int playerCount = connectedPlayers.length;
        
        if (!isMultiplayer) {
            playerCount = 1;
            connectedPlayers = new int[] { 1 };
        }
        int panelH = 62 + (playerCount * 22);

        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 14, 14);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Scoreboard", panelX + 16, panelY + 28);

        int[] scores = gameManager.getMultiplayerScores();
        if (!isMultiplayer) {
            scores[0] = gameManager.getScore();
        }

        g.setFont(new Font("Arial", Font.BOLD, 16));
        for (int i = 0; i < connectedPlayers.length; i++) {
            int networkId = connectedPlayers[i];
            // Normalize IDs to 1-indexed for the display layout
            int playerId = (networkId < 1 && networkId >= 0) ? networkId + 1 : networkId;
            
            int colorIndex = Math.max(0, Math.min(scoreboardColors.length - 1, playerId - 1));
            int scoreIndex = Math.max(0, Math.min(scores.length - 1, playerId - 1));
            
            g.setColor(scoreboardColors[colorIndex]);
            g.drawString("Player " + playerId + ": " + scores[scoreIndex], panelX + 16, panelY + 56 + (i * 22));
        }
    }
}