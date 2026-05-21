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
    private final Color[] scoreboardColors = { 
        new Color(255, 179, 186), // Soft Pastel Pink/Red (P1)
        new Color(186, 225, 255), // Soft Pastel Blue (P2)
        new Color(186, 255, 201), // Soft Pastel Mint (P3)
        new Color(255, 255, 186)  // Soft Pastel Lemon (P4)
    };

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
        
        // ==========================================
        // GAME OVER / VICTORY OVERLAY SCREEN UPGRADE
        // ==========================================
        if (gameManager.getIsGameOver()) {
            // Soft translucent warm espresso chocolate veil instead of cold black
            g.setColor(new Color(44, 34, 30, 225)); 
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

            // 1. CALCULATE HIGHEST SCORE & IDENTIFY ALL WINNERS (TIE HANDLING)
            int highestScore = -1;
            java.util.List<Integer> winningPlayerIds = new java.util.ArrayList<>();

            if (gameManager.isMultiplayerMode()) {
                // Step A: Discover the peak score ceiling
                for (int networkId : connectedPlayers) {
                    int pId = (networkId < 1) ? networkId + 1 : networkId;
                    int scoreIdx = Math.max(0, Math.min(finalScores.length - 1, pId - 1));
                    int pScore = finalScores[scoreIdx];
                    if (pScore > highestScore) {
                        highestScore = pScore;
                    }
                }
                // Step B: Collect all identities matching that highest value
                for (int networkId : connectedPlayers) {
                    int pId = (networkId < 1) ? networkId + 1 : networkId;
                    int scoreIdx = Math.max(0, Math.min(finalScores.length - 1, pId - 1));
                    if (finalScores[scoreIdx] == highestScore) {
                        winningPlayerIds.add(networkId);
                    }
                }
            } else {
                highestScore = cat.score;
                winningPlayerIds.add(localId);
            }

            boolean isTie = winningPlayerIds.size() > 1;
            boolean amIWinner = winningPlayerIds.contains(localId);

            // Get FontMetrics to calculate exact text widths dynamically
            java.awt.FontMetrics metrics;
            int screenWidth = getWidth();

            // 2. DYNAMIC HEADER DRAWING (PERFECTLY CENTERED)
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 64));
            metrics = g.getFontMetrics();
            String headerText = "";
            
            if (gameManager.isMultiplayerMode()) {
                if (isTie) {
                    if (amIWinner) {
                        g.setColor(new Color(255, 226, 153)); // Soft Lemon Pastel Yellow
                        headerText = "IT'S A TIE WIN!";
                    } else {
                        g.setColor(new Color(240, 128, 128)); // Soft Pastel Coral Red
                        headerText = "MATCH DRAW";
                    }
                } else {
                    int soleWinnerId = winningPlayerIds.get(0);
                    if (localId == soleWinnerId) {
                        g.setColor(new Color(181, 234, 215)); // Gorgeous Pastel Mint Green
                        headerText = "VICTORY!";
                    } else {
                        g.setColor(new Color(255, 154, 162)); // Soft Pastel Coral Pink
                        headerText = "GAME OVER";
                    }
                }
            } else {
                g.setColor(new Color(255, 154, 162)); // Singleplayer soft game over
                headerText = "GAME OVER";
            }
            // Math: Screen Center minus half of the string's precise pixel width
            g.drawString(headerText, (screenWidth - metrics.stringWidth(headerText)) / 2, 180);


            // 3. STANDINGS HEADER (PERFECTLY CENTERED)
            g.setColor(new Color(255, 248, 230)); // Soft warm cream text
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 26));
            metrics = g.getFontMetrics();
            String standingsTitle = "Final Standings:";
            g.drawString(standingsTitle, (screenWidth - metrics.stringWidth(standingsTitle)) / 2, 260);


            // 4. PLAYER ENTRY ITERATION LOOP (PERFECTLY CENTERED)
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            metrics = g.getFontMetrics();
            
            if (gameManager.isMultiplayerMode()) {
                for (int i = 0; i < connectedPlayers.length; i++) {
                    int networkId = connectedPlayers[i];
                    int pId = (networkId < 1) ? networkId + 1 : networkId;
                    int scoreIdx = Math.max(0, Math.min(finalScores.length - 1, pId - 1));
                    int pScore = finalScores[scoreIdx];
                    
                    int yPos = 310 + (i * 35); 
                    boolean individualIsWinner = winningPlayerIds.contains(networkId);
                    String rowText = "";

                    if (individualIsWinner) {
                        g.setColor(new Color(255, 215, 0)); // Bright Rich Gold
                        if (networkId == localId) {
                            rowText = "Player " + pId + " (You): " + pScore + " - Winner!";
                        } else {
                            rowText = "Player " + pId + ": " + pScore + " - Winner!";
                        }
                    } else if (networkId == localId) { 
                        g.setColor(new Color(174, 214, 241)); // Clear Pastel Blue for local identification
                        rowText = "Player " + pId + " (You): " + pScore;
                    } else {
                        g.setColor(new Color(200, 200, 200)); // Muted neutral gray for guest entries
                        rowText = "Player " + pId + ": " + pScore;
                    }
                    
                    // Math: Centers every individual player line entry dynamically based on its text content length
                    g.drawString(rowText, (screenWidth - metrics.stringWidth(rowText)) / 2, yPos);
                }
            } else {
                g.setColor(new Color(255, 226, 153));
                String singlePlayerScoreText = "Your Final Score: " + cat.score;
                g.drawString(singlePlayerScoreText, (screenWidth - metrics.stringWidth(singlePlayerScoreText)) / 2, 310);
            }


            // 5. FOOTER SUBTEXT RUNTIME STATE (PERFECTLY CENTERED)
            g.setColor(new Color(255, 248, 230, 180)); // Soft translucent cream subtext
            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
            metrics = g.getFontMetrics();
            String footerText = "Returning to menu shortly...";
            int footerY = gameManager.isMultiplayerMode() ? 330 + (connectedPlayers.length * 35) : 380;
            
            g.drawString(footerText, (screenWidth - metrics.stringWidth(footerText)) / 2, footerY);
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

            // NEW: Dynamically build the text string to append (You) for the local player instance
            String scoreboardRowText = "Player " + pId;
            if (gameManager.isMultiplayerMode() && networkId == gameManager.getLocalPlayerId()) {
                scoreboardRowText += " (You)";
            } else if (!gameManager.isMultiplayerMode()) {
                // In singleplayer, you are always Player 1
                scoreboardRowText += " (You)";
            }
            scoreboardRowText += ": " + displayScore;

            // Render row text matching your defined brand identification color array index
            g.setColor(scoreboardColors[colorIndex]);
            g.drawString(scoreboardRowText, panelX + 16, panelY + 56 + (i * 22));
        }
    }
}