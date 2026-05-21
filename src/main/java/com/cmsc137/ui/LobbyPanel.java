package com.cmsc137.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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

    // Host Chat Components
    private javax.swing.JTextArea hostChatDisplay;
    private JTextField hostChatInput;

    // Guest Chat Components
    private javax.swing.JTextArea joinChatDisplay;
    private JTextField joinChatInput;

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

        if (hostChatDisplay != null) hostChatDisplay.setText("");
        if (joinChatDisplay != null) joinChatDisplay.setText("");

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
            hostBgImage = javax.imageio.ImageIO.read(new java.io.File("assets/HostBG.png")); //
        } catch (Exception e) {
            e.printStackTrace(); //
        }
        
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                if (hostBgImage != null) {
                    g.drawImage(hostBgImage, 0, 0, getWidth(), getHeight(), this); //
                }
            }
        };

        // --- MIDDLE CONTAINER LOBBY BOX ---
        JPanel containerBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(40, 40, 40, 140)); //
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); //
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        containerBox.setLayout(new BoxLayout(containerBox, BoxLayout.Y_AXIS)); //
        containerBox.setOpaque(false); //
        containerBox.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20)); //

        JLabel ip = new JLabel("IP Address: "); ip.setName("hostIpLabel"); //
        ip.setFont(new Font("Comic Sans MS", Font.BOLD, 30)); ip.setForeground(Color.WHITE); ip.setAlignmentX(CENTER_ALIGNMENT); //
        containerBox.add(ip); containerBox.add(Box.createVerticalStrut(8)); //

        JLabel status = new JLabel("Host status: waiting for players..."); status.setName("hostStatusLabel"); //
        status.setFont(new Font("Comic Sans MS", Font.BOLD, 16)); status.setForeground(Color.WHITE); status.setAlignmentX(CENTER_ALIGNMENT); //
        containerBox.add(status); containerBox.add(Box.createVerticalStrut(16)); //

        JLabel playersHeader = new JLabel("Connected Players"); //
        playersHeader.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); playersHeader.setForeground(Color.WHITE); playersHeader.setAlignmentX(CENTER_ALIGNMENT); //
        containerBox.add(playersHeader); containerBox.add(Box.createVerticalStrut(8)); //

        for (int i = 1; i <= 4; i++) { //
            JLabel slot = new JLabel("Player " + i + ": waiting..."); //
            slot.setFont(new Font("Comic Sans MS", Font.BOLD, 16)); slot.setForeground(Color.WHITE); slot.setAlignmentX(CENTER_ALIGNMENT); //
            hostPlayerSlots.add(slot); containerBox.add(slot); containerBox.add(Box.createVerticalStrut(4)); //
        }

        containerBox.setBounds(390, 200, 500, 280);
        panel.add(containerBox);

        // --- CONTROL BUTTONS (FIXED HORIZONTAL WRAP GAP) ---
        // FlowLayout needs a wider container bounding box so your two 280px buttons fit side-by-side!
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); 
        controls.setOpaque(false); //
        
        JButton start = createButton("Start Game"); start.setName("hostStartButton"); start.setEnabled(false); //
        start.addActionListener(e -> screenManager.sendHostStart()); controls.add(start); //

        JButton leave = createButton("Leave Lobby"); //
        leave.addActionListener(e -> { screenManager.disconnectClient(); resetLobby(); screenManager.showMainMenu(); }); //
        controls.add(leave); //
        
        // Expanded layout width to 600px, shifted left to 340 to stay perfectly centered
        controls.setBounds(340, 565, 600, 60);
        panel.add(controls);

        // --- FLOATING CHAT OVERLAY BOX (FIXED NARROW WIDTH) ---
        JPanel chatWrapper = buildChatComponent(true);
        // Width narrowed down to 260px, pushed right to 990 to look sleek and compact
        chatWrapper.setBounds(995, 25, 260, 200);
        panel.add(chatWrapper);

        return panel;
    }

    private JPanel buildJoinPanel() {
        try {
            joinBgImage = javax.imageio.ImageIO.read(new java.io.File("assets/JoinedBG.png")); //
        } catch (Exception e) {
            e.printStackTrace(); //
        }

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                if (joinBgImage != null) {
                    g.drawImage(joinBgImage, 0, 0, getWidth(), getHeight(), this); //
                }
            }
        };

        // --- MIDDLE CONTAINER JOIN BOX ---
        JPanel containerBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(40, 40, 40, 140)); //
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); //
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        containerBox.setLayout(new BoxLayout(containerBox, BoxLayout.Y_AXIS)); //
        containerBox.setOpaque(false); //
        containerBox.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20)); //

        JLabel status = new JLabel("Join status: disconnected"); status.setName("joinStatusLabel"); //
        status.setFont(new Font("Comic Sans MS", Font.BOLD, 16)); status.setForeground(Color.WHITE); status.setAlignmentX(CENTER_ALIGNMENT); //
        containerBox.add(status); containerBox.add(Box.createVerticalStrut(16)); //

        JLabel playersHeader = new JLabel("Players in this lobby"); //
        playersHeader.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); playersHeader.setForeground(Color.WHITE); playersHeader.setAlignmentX(CENTER_ALIGNMENT); //
        containerBox.add(playersHeader); containerBox.add(Box.createVerticalStrut(8)); //

        for (int i = 1; i <= 4; i++) { //
            JLabel slot = new JLabel("Player " + i + ": waiting..."); //
            slot.setFont(new Font("Comic Sans MS", Font.BOLD, 16)); slot.setForeground(Color.WHITE); slot.setAlignmentX(CENTER_ALIGNMENT); //
            joinPlayerSlots.add(slot); containerBox.add(slot); containerBox.add(Box.createVerticalStrut(4)); //
        }

        containerBox.setBounds(390, 200, 500, 280);
        panel.add(containerBox);

        // --- GUEST BUTTONS CONTROLS ---
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); //
        controls.setOpaque(false); //

        JButton leave = createButton("Leave Lobby"); //
        leave.addActionListener(e -> { screenManager.disconnectClient(); resetLobby(); screenManager.showMainMenu(); }); //
        controls.add(leave); //
        
        controls.setBounds(340, 565, 600, 60);
        panel.add(controls);

        // --- FLOATING CHAT OVERLAY BOX (FIXED NARROW WIDTH) ---
        JPanel chatWrapper = buildChatComponent(false);
        chatWrapper.setBounds(995, 25, 260, 200);
        panel.add(chatWrapper);

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

    private JPanel buildChatComponent(boolean isHost) {
        JPanel panel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(40, 40, 40, 140)); //
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        javax.swing.JTextArea displayArea = new javax.swing.JTextArea();
        displayArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 12)); //
        displayArea.setEditable(false); //
        displayArea.setLineWrap(true); //
        displayArea.setWrapStyleWord(true); //
        displayArea.setOpaque(false); //
        displayArea.setForeground(Color.WHITE); //
        
        // Bind to the correct tracking reference based on layout state
        if (isHost) {
            this.hostChatDisplay = displayArea;
        } else {
            this.joinChatDisplay = displayArea;
        }
        
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); //
        scrollPane.setOpaque(false); //
        scrollPane.getViewport().setOpaque(false); //
        panel.add(scrollPane, BorderLayout.CENTER);

        JTextField inputField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40)); //
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); //
                g2.dispose();
                super.paintComponent(g);
            }
        };
        inputField.setFont(new Font("Comic Sans MS", Font.PLAIN, 12)); //
        inputField.setForeground(Color.WHITE); //
        inputField.setCaretColor(Color.WHITE); //
        inputField.setOpaque(false); //
        inputField.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6)); //
        
        if (isHost) {
            this.hostChatInput = inputField;
        } else {
            this.joinChatInput = inputField;
        }
        
        inputField.addActionListener(e -> triggerMessageSend(isHost));

        JButton sendButton = new JButton("Send") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); //
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); //
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sendButton.setFont(new Font("Comic Sans MS", Font.BOLD, 11)); //
        sendButton.setForeground(Color.WHITE); //
        sendButton.setBackground(new Color(68, 70, 74)); //
        sendButton.setFocusPainted(false); //
        sendButton.setBorderPainted(false); //
        sendButton.setContentAreaFilled(false); //
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); //
        sendButton.setPreferredSize(new Dimension(68, 26)); //
        sendButton.addActionListener(e -> triggerMessageSend(isHost));

        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                sendButton.setBackground(new Color(95, 98, 104)); //
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                sendButton.setBackground(new Color(68, 70, 74)); //
            }
        });

        JPanel inputRow = new JPanel(new BorderLayout(4, 0)); //
        inputRow.setOpaque(false); //
        inputRow.add(inputField, BorderLayout.CENTER); //
        inputRow.add(sendButton, BorderLayout.EAST); //
        
        panel.add(inputRow, BorderLayout.SOUTH); //
        return panel;
    }

    private void triggerMessageSend(boolean isHost) {
        JTextField activeInput = isHost ? hostChatInput : joinChatInput;
        if (activeInput == null) return;

        String text = activeInput.getText().trim();
        if (!text.isEmpty()) {
            screenManager.getHelperClientConnection().sendChatMessage(text); //
            activeInput.setText(""); // Clears the correct visible input field layout
        }
    }

    public void appendChatMessage(int playerId, String message) {
        SwingUtilities.invokeLater(() -> {
            String formattedLine = "Player " + playerId + ": " + message + "\n"; //
            
            // Append the message text to both display components to support both states seamlessly
            if (hostChatDisplay != null) {
                hostChatDisplay.append(formattedLine);
                hostChatDisplay.setCaretPosition(hostChatDisplay.getDocument().getLength()); //
            }
            if (joinChatDisplay != null) {
                joinChatDisplay.append(formattedLine);
                joinChatDisplay.setCaretPosition(joinChatDisplay.getDocument().getLength()); //
            }
        });
    }
}