package com.cmsc137.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(JFrame frame, ScreenManager screenManager) {
        setLayout(new GridBagLayout());
        setBackground(Colors.BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Label
        JLabel title = new JLabel("Pusang Gutom", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        add(title, gbc);


        gbc.gridy = 2;
        add(Box.createVerticalStrut(30), gbc);

        // Start Button
        JButton singlePlayerButton = createButton("Singleplayer Game");
        singlePlayerButton.addActionListener(e -> {
            System.out.println("Start Game clicked!");
            screenManager.showGame(false);
        });
        gbc.gridy = 3;
        add(singlePlayerButton, gbc);
        
        JButton multiPlayerButton = createButton("Multiplayer Game");
        multiPlayerButton.addActionListener(e -> {
            System.out.println("Start Game clicked!");
            // TODO: screenManager.showGame();
        });
        gbc.gridy = 4;
        add(multiPlayerButton, gbc);

        // Instructions Button
        JButton instructionsButton = createButton("Instructions");
        instructionsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                "Use arrow keys to move.\nCollect food. Avoid enemies!",
                "Instructions",
                JOptionPane.INFORMATION_MESSAGE);
        });
        gbc.gridy = 5;
        add(instructionsButton, gbc);

        // Quit Button
        JButton quitButton = createButton("Quit");
        quitButton.addActionListener(e -> System.exit(0));
        gbc.gridy = 6;
        add(quitButton, gbc);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(80, 80, 80));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(120, 120, 120));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(80, 80, 80));
            }
        });

        return button;
    }
}