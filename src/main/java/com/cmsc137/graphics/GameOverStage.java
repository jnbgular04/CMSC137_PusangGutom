package com.cmsc137.graphics;

import java.awt.*;
import javax.swing.*;
import com.cmsc137.engine.GameManager;

public class GameOverStage extends JPanel {
    private GameManager gameManager;
    private JButton returnButton;
    private JButton rematchButton;

    public GameOverStage(GameManager gameManager, CardLayout layout, JPanel mainPanel) {
        this.gameManager = gameManager;
        setBackground(new Color(20, 20, 20)); // Sleek dark screen
        setLayout(new BorderLayout());

        // Title Section
        JLabel titleLabel = new JLabel("MATCH RESULTS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Scoreboard Display Component (Custom Inner Panel)
        JPanel scoreboardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int[] players = gameManager.getConnectedPlayers();
                int[] scores = gameManager.getMultiplayerScores();
                int localId = gameManager.getLocalPlayerId();

                g2d.setFont(new Font("Arial", Font.BOLD, 22));
                for (int i = 0; i < players.length; i++) {
                    int pId = players[i];
                    int pScore = scores[Math.max(0, Math.min(scores.length - 1, pId - 1))];
                    
                    if (pId == localId) {
                        g2d.setColor(Color.CYAN);
                        g2d.drawString("👉 Player " + pId + " (You): " + pScore, 50, 60 + (i * 40));
                    } else {
                        g2d.setColor(Color.WHITE);
                        g2d.drawString("   Player " + pId + ": " + pScore, 50, 60 + (i * 40));
                    }
                }
            }
        };
        scoreboardPanel.setOpaque(false);
        add(scoreboardPanel, BorderLayout.CENTER);

        // Control Buttons Footer
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        returnButton = new JButton("Main Menu");
        returnButton.setFont(new Font("Arial", Font.BOLD, 18));
        returnButton.addActionListener(e -> {
            // Reset network states safely here
            layout.show(mainPanel, "MENU_SCREEN");
        });

        buttonPanel.add(returnButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}