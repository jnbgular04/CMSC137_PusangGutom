package com.cmsc137.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Cursor;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.cmsc137.network.NetworkProtocol;

public class LobbyPanel extends JPanel {
    private java.awt.Image entryBgImage;
    private java.awt.Image hostBgImage;
    private java.awt.Image joinBgImage;

    private static final String ENTRY_VIEW = "ENTRY_VIEW";
    private static final String HOST_VIEW = "HOST_VIEW";
    private static final String JOIN_VIEW = "JOIN_VIEW";

    private final ScreenManager screenManager;
    private final CardLayout cardLayout;
    private final JPanel cardContainer;

    private final JLabel hostIpLabel;
    private final JLabel hostStatusLabel;
    private final JLabel joinStatusLabel;
    private final List<JLabel> hostPlayerSlots;
    private final List<JLabel> joinPlayerSlots;
    private final JButton hostStartButton;
    private final JTextField ipInputField;

    private boolean isHostFlow = false;

    public LobbyPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.cardLayout = new CardLayout();
        this.cardContainer = new JPanel(cardLayout);
        this.hostPlayerSlots = new ArrayList<>();
        this.joinPlayerSlots = new ArrayList<>();

        setLayout(new BorderLayout());

        JPanel entryPanel = buildEntryPanel();
        JPanel hostPanel = buildHostPanel();
        JPanel joinPanel = buildJoinPanel();

        this.hostIpLabel = findLabelByName(hostPanel, "hostIpLabel");
        this.hostStatusLabel = findLabelByName(hostPanel, "hostStatusLabel");
        this.joinStatusLabel = findLabelByName(joinPanel, "joinStatusLabel");
        this.hostStartButton = findButtonByName(hostPanel, "hostStartButton");
        this.ipInputField = findInputByName(entryPanel, "ipInputField");

        cardContainer.add(entryPanel, ENTRY_VIEW);
        cardContainer.add(hostPanel, HOST_VIEW);
        cardContainer.add(joinPanel, JOIN_VIEW);
        add(cardContainer, BorderLayout.CENTER);
    }

    // custom styling
    private JButton createButton(String text) {
        // Custom subclass to cleanly paint anti-aliased rounded corners natively
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Set background color based on active/inactive component state
                if (!isEnabled()) {
                    g2.setColor(new Color(130, 130, 130, 120)); // Soft, semi-translucent gray for disabled state
                } else {
                    g2.setColor(getBackground());
                }
                
                // Draw smooth rounded rectangular base bounds
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        // Style setup matching your custom layout constraints
        button.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(68, 70, 74)); // Rich Dark Slate Charcoal (sampled from the tail stripes!)
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false); // Overrides the standard platform box borders
        button.setPreferredSize(new Dimension(280, 45)); // Retains explicit sizing constraints
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Refined Hover/Interact Lifecycle Listeners
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Hover animation shifts palette only if interactive component flag permits
                if (button.isEnabled()) {
                    button.setBackground(new Color(95, 98, 104)); // Lighter slate charcoal highlight
                    button.setForeground(new Color(255, 248, 230)); // Warm pastel cream tint text on hover
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(68, 70, 74)); // Resets cleanly back to primary brand color
                    button.setForeground(Color.WHITE);
                }
            }
        });

        return button;
    }

    public void resetLobby() {
        isHostFlow = false;
        if (ipInputField != null) {
            ipInputField.setText("");
        }
        if (hostStatusLabel != null) {
            hostStatusLabel.setText("Host status: waiting for players...");
        }
        if (joinStatusLabel != null) {
            joinStatusLabel.setText("Join status: disconnected");
        }
        if (hostStartButton != null) {
            hostStartButton.setEnabled(false);
        }
        updatePlayerSlots(hostPlayerSlots, new int[] {});
        updatePlayerSlots(joinPlayerSlots, new int[] {});
        cardLayout.show(cardContainer, ENTRY_VIEW);
    }

    public void onAssignedPlayerId(int playerId) {
        SwingUtilities.invokeLater(() -> {
            if (isHostFlow) {
                hostStatusLabel.setText("Host status: connected as Player " + playerId);
                hostStartButton.setEnabled(playerId == 1);
            } else {
                joinStatusLabel.setText("Join status: connected as Player " + playerId);
            }
        });
    }

    public void onLobbyUpdated(int[] connectedPlayers, int localPlayerId) {
        SwingUtilities.invokeLater(() -> {
            updatePlayerSlots(hostPlayerSlots, connectedPlayers);
            updatePlayerSlots(joinPlayerSlots, connectedPlayers);
            if (isHostFlow) {
                hostStartButton.setEnabled(localPlayerId == 1 && connectedPlayers.length > 1);
            }
        });
    }

    private JPanel buildEntryPanel() {
        try {
            entryBgImage = javax.imageio.ImageIO.read(
                new java.io.File("assets/LobbyPanelBG.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                if (entryBgImage != null) {
                    g.drawImage(entryBgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton hostButton = createButton("Host Game");
        hostButton.addActionListener(e -> handleHostGame());
        gbc.gridy = 1;
        panel.add(hostButton, gbc);

        JLabel ipLabel = new JLabel("Enter Room Code");
        ipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ipLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 20));       
        ipLabel.setForeground(Color.WHITE); 
        gbc.gridy = 2;
        panel.add(ipLabel, gbc);
        
        JTextField ipField = new JTextField();
        ipField.setName("ipInputField");
        ipField.setPreferredSize(new Dimension(280, 35));
        gbc.gridy = 3;
        panel.add(ipField, gbc);

        JButton joinButton = createButton("Connect to Host");
        joinButton.addActionListener(e -> handleJoinGame(ipField.getText().trim()));
        gbc.gridy = 4;
        panel.add(joinButton, gbc);

        JButton backButton = createButton("Back to Main Menu");
        backButton.addActionListener(e -> screenManager.showMainMenu());
        gbc.gridy = 5;
        panel.add(backButton, gbc);

        return panel;
    }

    private JPanel buildHostPanel() {
        try {
            hostBgImage = javax.imageio.ImageIO.read(
                new java.io.File("assets/HostBG.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                if (hostBgImage != null) {
                    g.drawImage(hostBgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        Box body = new Box(BoxLayout.Y_AXIS);
        body.setOpaque(false);

        body.add(Box.createVerticalStrut(220));

        // CREATE THE LOW-OPACITY GRAY CONTAINER
        JPanel containerBox = new JPanel();
        containerBox.setLayout(new BoxLayout(containerBox, BoxLayout.Y_AXIS));
        containerBox.setOpaque(false); 
        containerBox.setAlignmentX(CENTER_ALIGNMENT);   
        containerBox.setMaximumSize(new Dimension(500, 300));
        containerBox.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
     
        containerBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
              
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
              
                g2d.setColor(new Color(40, 40, 40, 140)); 
                
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        containerBox.setLayout(new BoxLayout(containerBox, BoxLayout.Y_AXIS));
        containerBox.setOpaque(false);
        containerBox.setAlignmentX(CENTER_ALIGNMENT);
        containerBox.setMaximumSize(new Dimension(500, 280));
        containerBox.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));


        JLabel ip = new JLabel("IP Address: ");
        ip.setName("hostIpLabel");
        ip.setFont(new Font("Comic Sans MS", Font.BOLD, 30)); 
        ip.setForeground(Color.WHITE);
        ip.setAlignmentX(CENTER_ALIGNMENT); 
        containerBox.add(ip); 
        containerBox.add(Box.createVerticalStrut(8));

        JLabel status = new JLabel("Host status: waiting for players...");
        status.setName("hostStatusLabel");
        status.setFont(new Font("Comic Sans MS", Font.BOLD, 16)); 
        status.setForeground(Color.WHITE);
        status.setAlignmentX(CENTER_ALIGNMENT); 
        containerBox.add(status); 
        containerBox.add(Box.createVerticalStrut(16)); 

        JLabel playersHeader = new JLabel("Connected Players");
        playersHeader.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); 
        playersHeader.setForeground(Color.WHITE);
        playersHeader.setAlignmentX(CENTER_ALIGNMENT); 
        containerBox.add(playersHeader); 
        containerBox.add(Box.createVerticalStrut(8));

        for (int i = 1; i <= 4; i++) {
            JLabel slot = new JLabel("Player " + i + ": waiting...");
            slot.setFont(new Font("Comic Sans MS", Font.BOLD, 16)); 
            slot.setForeground(Color.WHITE); 
            slot.setAlignmentX(CENTER_ALIGNMENT); 
            hostPlayerSlots.add(slot);
            containerBox.add(slot); 
            containerBox.add(Box.createVerticalStrut(4)); 
        }

     
        body.add(containerBox);
        body.add(Box.createVerticalStrut(20)); 


        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controls.setOpaque(false); 
        
        JButton start = createButton("Start Game");
        start.setName("hostStartButton");
        start.setEnabled(false);
        start.addActionListener(e -> screenManager.sendHostStart());
        controls.add(start);

        JButton leave = createButton("Leave Lobby");
        leave.addActionListener(e -> {
            screenManager.disconnectClient();
            resetLobby();
            screenManager.showMainMenu();
        });
        controls.add(leave);
        
        controls.setAlignmentX(CENTER_ALIGNMENT); 
        body.add(controls);

        body.add(Box.createVerticalGlue()); 
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildJoinPanel() {
        try {
            joinBgImage = javax.imageio.ImageIO.read(
                new java.io.File("assets/JoinedBG.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        final java.awt.Image finalJoinBg = joinBgImage;
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                if (finalJoinBg != null) {
                    g.drawImage(finalJoinBg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        Box body = new Box(BoxLayout.Y_AXIS);
        body.setOpaque(false);

        // Aligns vertically with the host layout location
        body.add(Box.createVerticalStrut(220));

        // CREATE THE LOW-OPACITY GRAY CONTAINER (Identical to Host panel)
        JPanel containerBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Matches the gray backdrop hue and opacity (140) of the host lobby box
                g2d.setColor(new Color(40, 40, 40, 140)); 
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        containerBox.setLayout(new BoxLayout(containerBox, BoxLayout.Y_AXIS));
        containerBox.setOpaque(false);
        containerBox.setAlignmentX(CENTER_ALIGNMENT);
        containerBox.setMaximumSize(new Dimension(500, 280));
        containerBox.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        // Join Status Label inside the translucent container
        JLabel status = new JLabel("Join status: disconnected");
        status.setName("joinStatusLabel");
        status.setFont(new Font("Comic Sans MS", Font.BOLD, 16)); // Thicker font applied
        status.setForeground(Color.WHITE);
        status.setAlignmentX(CENTER_ALIGNMENT);
        containerBox.add(status);
        containerBox.add(Box.createVerticalStrut(16));

        // Subheader inside the translucent container
        JLabel playersHeader = new JLabel("Players in this lobby");
        playersHeader.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); // Thicker font applied
        playersHeader.setForeground(Color.WHITE);
        playersHeader.setAlignmentX(CENTER_ALIGNMENT);
        containerBox.add(playersHeader);
        containerBox.add(Box.createVerticalStrut(8));

        // Dynamic player listings
        for (int i = 1; i <= 4; i++) {
            JLabel slot = new JLabel("Player " + i + ": waiting...");
            slot.setFont(new Font("Comic Sans MS", Font.BOLD, 16)); // Thicker font applied
            slot.setForeground(Color.WHITE); // White text for cleaner contrast
            slot.setAlignmentX(CENTER_ALIGNMENT);
            joinPlayerSlots.add(slot);
            containerBox.add(slot);
            containerBox.add(Box.createVerticalStrut(4));
        }

        // Drop the completed layout card container directly into your master tracking flow
        body.add(containerBox);
        body.add(Box.createVerticalStrut(20));

        // Clean footer controls panel matching the host flow layouts
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controls.setOpaque(false);

        JButton leave = createButton("Leave Lobby");
        leave.addActionListener(e -> {
            screenManager.disconnectClient();
            resetLobby();
            screenManager.showMainMenu();
        });
        controls.add(leave);
        
        controls.setAlignmentX(CENTER_ALIGNMENT);
        body.add(controls);

        body.add(Box.createVerticalGlue());
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }
    
    private void handleHostGame() {
        isHostFlow = true;
        String rawIp = resolveLocalIpAddress();
        String pseudoRoomCode = NetworkProtocol.ipToRoomCode(rawIp);

        hostIpLabel.setText("Room Code: " + pseudoRoomCode); 
        hostStatusLabel.setText("Host status: starting server...");
        cardLayout.show(cardContainer, HOST_VIEW);

        boolean started = screenManager.startLocalServer();
        if (!started) {
            JOptionPane.showMessageDialog(this, "Server is already running or failed to start.", "Host Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        hostStatusLabel.setText("Host status: connecting to local server...");
    }

    private void handleJoinGame(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Room Code.", "Invalid Code", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Decode the pseudo-code back into a functional IP address
        String decryptedIp = NetworkProtocol.roomCodeToIp(roomCode);
        
        if (decryptedIp == null) {
            JOptionPane.showMessageDialog(this, "Invalid Room Code format.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        isHostFlow = false;
        joinStatusLabel.setText("Join status: connecting to room " + roomCode + "...");
        cardLayout.show(cardContainer, JOIN_VIEW);
        
        // Pass the real IP to your existing connection system
        screenManager.connectClient(decryptedIp); 
    }

    private void updatePlayerSlots(List<JLabel> slots, int[] connectedPlayers) {
        for (int i = 0; i < slots.size(); i++) {
            int playerNumber = i + 1;
            boolean connected = contains(connectedPlayers, playerNumber);
            slots.get(i).setText("Player " + playerNumber + ": " + (connected ? "connected" : "waiting..."));
        }
    }

    private boolean contains(int[] values, int target) {
        for (int value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }

    private String resolveLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') < 0) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }

    private JLabel findLabelByName(JPanel root, String name) {
        return (JLabel) findByName(root, name, JLabel.class);
    }

    private JButton findButtonByName(JPanel root, String name) {
        return (JButton) findByName(root, name, JButton.class);
    }

    private JTextField findInputByName(JPanel root, String name) {
        return (JTextField) findByName(root, name, JTextField.class);
    }

    private java.awt.Component findByName(java.awt.Container container, String name, Class<?> type) {
        for (java.awt.Component component : container.getComponents()) {
            if (type.isInstance(component) && name.equals(component.getName())) {
                return component;
            }
            if (component instanceof java.awt.Container) {
                java.awt.Component nested = findByName((java.awt.Container) component, name, type);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }
}