package com.cmsc137.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenuPanel extends JPanel {
	private java.awt.Image backgroundImage;

	public MainMenuPanel(JFrame frame, ScreenManager screenManager) {
	    
	    try {
	        backgroundImage = javax.imageio.ImageIO.read(
	            new java.io.File("assets/Main_Menu_BG.png")
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    setLayout(new GridBagLayout());
	    setBackground(Colors.BACKGROUND);

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(10, 10, 10, 10);
	    gbc.gridx = 0;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    
	    
	    gbc.gridy = 0;
	    add(Box.createVerticalStrut(150), gbc);

	    // Start Button (Moved to gridy 0)
	    JButton singlePlayerButton = createButton("Singleplayer Game");
	    singlePlayerButton.addActionListener(e -> {
	        System.out.println("Start Game clicked!");
	        screenManager.showGame(false);
	    });
	    singlePlayerButton.setPreferredSize(new Dimension(300, 50));
	    gbc.gridy = 1; 
	    add(singlePlayerButton, gbc);
	    
	    
	    // Multiplayer Button
	    JButton multiPlayerButton = createButton("Multiplayer Game");
	    multiPlayerButton.addActionListener(e -> {
	        System.out.println("Opening multiplayer lobby...");
	        screenManager.showLobby();
	    });
	    gbc.gridy = 2;
	    multiPlayerButton.setPreferredSize(new Dimension(300, 50));
	    add(multiPlayerButton, gbc);

	    // Instructions Button
	    JButton instructionsButton = createButton("Instructions");
	    instructionsButton.addActionListener(e -> {
	        JOptionPane.showMessageDialog(frame,
	            "Click on the spawned mouse to collect them.\n The cat with the most mice wins!",
	            "Instructions",
	            JOptionPane.INFORMATION_MESSAGE);
	    });
	    gbc.gridy = 3;
	    instructionsButton.setPreferredSize(new Dimension(300, 50));
	    add(instructionsButton, gbc);

	    // Quit Button
	    JButton quitButton = createButton("Quit");
	    quitButton.addActionListener(e -> System.exit(0));
	    quitButton.setPreferredSize(new Dimension(300, 50));
	    gbc.gridy = 4;
	    add(quitButton, gbc);
	}
    
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // This draws the image scaled to fill the entire panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
    
    private JButton createButton(String text) {
		// Custom subclass to paint clean, anti-aliased rounded corners natively
		JButton button = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getBackground());
				// Draws a smooth rounded rectangle
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
				g2.dispose();
				super.paintComponent(g);
			}
		};

		button.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
		button.setForeground(Color.WHITE);
		button.setBackground(new Color(90, 65, 55)); // Warm Dark Chocolate/Espresso
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false); // Prevents default square background rendering
		button.setPreferredSize(new Dimension(240, 50));
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent e) {
				button.setBackground(new Color(135, 100, 85)); // Lighter warm brown
				button.setForeground(new Color(255, 235, 220)); // Soft cream text on hover
			}
			public void mouseExited(java.awt.event.MouseEvent e) {
				button.setBackground(new Color(90, 65, 55));
				button.setForeground(Color.WHITE);
			}
		});

		return button;
	}
}