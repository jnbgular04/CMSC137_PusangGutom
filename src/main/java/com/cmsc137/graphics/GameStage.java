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
    private final Color[] scoreboardColors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };

    public GameStage(GameManager gameManager) { 
        this.gameManager = gameManager;
        
        backgroundImage = new ImageIcon("assets/Game_Stage_BG.png").getImage();

        setPreferredSize(new Dimension(1280, 720));
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

    public void setLocalPlayerId(int playerId) {
        spriteRenderer.setLocalPlayerId(playerId);
    }

    @Override
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
        int timeLeft = gameManager.getTimeLeft(); 
        
        int targetX = cat.x + 100;
        int targetY = cat.y;
        if (!gameManager.isMultiplayerMode()) {
            java.awt.Point cursorPos = getMousePosition();
            targetX = (cursorPos != null) ? cursorPos.x : targetX;
            targetY = (cursorPos != null) ? cursorPos.y : targetY;
        }

        // get the mice from game manager
        List<Mouse> mice = gameManager.getActiveMice();
        spriteRenderer.drawMice(g, mice);
        if (gameManager.isMultiplayerMode()) {
            drawMultiplayerCats(g);
        } else {
            spriteRenderer.drawCat(g, cat, targetX, targetY);
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        g.drawString("Time: " + timeLeft, 20, 35);
        drawScoreboard(g);
        
        // GAME OVER SCREEN OVERLAY
        if (gameManager.getIsGameOver()) {
            g.setColor(new Color(0, 0, 0, 200)); 
            g.fillRect(0, 0, getWidth(), getHeight());

            int localId = gameManager.getLocalPlayerId();
            int[] connectedPlayers = gameManager.getConnectedPlayers();
            int[] finalScores = gameManager.getMultiplayerScores();

            if (connectedPlayers == null || connectedPlayers.length == 0) {
                connectedPlayers = new int[] { 1 };
            }
            if (finalScores == null || finalScores.length == 0) {
                finalScores = new int[4];
            }

            // Unify winner extraction parameters
            int winningScore = -1;
            int winnerId = -1;
            
            if (gameManager.isMultiplayerMode()) {
                for (int i = 0; i < connectedPlayers.length; i++) {
                    int networkId = connectedPlayers[i];
                    int pId = (networkId < 1) ? networkId + 1 : networkId;
                    
                    int scoreIdx = Math.max(0, Math.min(finalScores.length - 1, pId - 1));
                    int pScore = finalScores[scoreIdx];
                    if (pScore > winningScore) {
                        winningScore = pScore;
                        winnerId = networkId; // Lock original network identity context
                    }
                }
            } else {
                winningScore = cat.score;
                winnerId = localId;
            }

            g.setFont(new Font("Comic Sans MS", Font.BOLD, 64));
            if (gameManager.isMultiplayerMode()) {
                if (localId == winnerId) {
                    g.setColor(Color.GREEN);
                    g.drawString("VICTORY!", 470, 180);
                } else {
                    g.setColor(Color.RED);
                    g.drawString("GAME OVER", 440, 180);
                }
            } else {
                g.setColor(Color.RED);
                g.drawString("GAME OVER", 440, 180);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 24));
            g.drawString("Final Standings:", 545, 260);

            g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            if (gameManager.isMultiplayerMode()) {
                for (int i = 0; i < connectedPlayers.length; i++) {
                    int networkId = connectedPlayers[i];
                    int pId = (networkId < 1) ? networkId + 1 : networkId;
                    
                    int scoreIdx = Math.max(0, Math.min(finalScores.length - 1, pId - 1));
                    int pScore = finalScores[scoreIdx];
                    
                    // FIXED IDENTITY CLAUSES FOR THE LAST ENTRIES
                    if (networkId == winnerId) {
                        g.setColor(Color.YELLOW);
                        g.drawString("Player " + pId + ": " + pScore + " (Winner)", 510, 310 + (i * 30));
                    } else if (networkId == localId || pId == localId) { 
                        g.setColor(Color.CYAN);
                        g.drawString("Player " + pId + " (You): " + pScore, 510, 310 + (i * 30));
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                        g.drawString("Player " + pId + ": " + pScore, 510, 310 + (i * 30));
                    }
                }
            } else {
                g.setColor(Color.YELLOW);
                g.drawString("Your Score: " + cat.score, 550, 310);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
            int footerY = gameManager.isMultiplayerMode() ? 350 + (connectedPlayers.length * 30) : 380;
            g.drawString("Returning to menu shortly...", 520, footerY);
        }
    }

    public void update() {
        repaint(); 
    }

    private void drawMultiplayerCats(Graphics g) {
        List<Cat> cats = gameManager.getNetworkedCats();
        
        for (Cat c : cats) {
            // Only draw if the player is connected (based on IDs the server sent)
            if (isPlayerConnected(c.id)) {
                int targetX = c.getAnimatedX(c.animTargetX);
                int targetY = c.getAnimatedY(c.animTargetY);
                
                spriteRenderer.drawCatForPlayer(g, c.id, c.x, c.y, targetX, targetY);
            }
        }
    }

    private boolean isPlayerConnected(int id) {
        int[] connected = gameManager.getConnectedPlayers();
        for (int p : connected) {
            if (p == id) return true;
        }
        return false;
    }

    private void drawScoreboard(Graphics g) {
        int panelX = getWidth() - 280;
        int panelY = 20;
        int panelW = 240;
        
        int[] connectedPlayers = gameManager.getConnectedPlayers();
        if (connectedPlayers == null || connectedPlayers.length == 0) {
            connectedPlayers = new int[] { 1 };
        }

        int playerCount = connectedPlayers.length;
        if (!gameManager.isMultiplayerMode()) {
            playerCount = 1;
            connectedPlayers = new int[] { 1 };
        }

        int panelH = 62 + (playerCount * 22);

        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 14, 14);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        g.drawString("Scoreboard", panelX + 16, panelY + 28);

        int[] scores = gameManager.getMultiplayerScores();
        if (scores == null || scores.length == 0) {
            scores = new int[4]; 
        }

        if (!gameManager.isMultiplayerMode()) {
            scores[0] = gameManager.getScore();
        }

        g.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
        for (int i = 0; i < connectedPlayers.length; i++) {
            int networkId = connectedPlayers[i];
            int pId = (networkId < 1) ? networkId + 1 : networkId;
            if (pId < 1 || pId > 4) {
                pId = 1; 
            }
            
            int colorIndex = Math.max(0, Math.min(scoreboardColors.length - 1, pId - 1));
            int scoreIndex = Math.max(0, Math.min(scores.length - 1, pId - 1));
            
            int displayScore = scores[scoreIndex];

            g.setColor(scoreboardColors[colorIndex]);
            g.drawString("Player " + pId + ": " + displayScore, panelX + 16, panelY + 56 + (i * 22));
        }
    }
}