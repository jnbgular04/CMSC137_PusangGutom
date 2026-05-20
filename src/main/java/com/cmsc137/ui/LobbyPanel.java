package com.cmsc137.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

public class LobbyPanel extends JPanel {
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
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

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

    public void resetLobby() {
        isHostFlow = false;
        ipInputField.setText("");
        hostStatusLabel.setText("Host status: waiting for players...");
        joinStatusLabel.setText("Join status: disconnected");
        hostStartButton.setEnabled(false);
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
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Multiplayer Lobby", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        gbc.gridy = 0;
        panel.add(title, gbc);

        JButton hostButton = new JButton("Host Game");
        hostButton.setPreferredSize(new Dimension(280, 45));
        hostButton.addActionListener(e -> handleHostGame());
        gbc.gridy = 1;
        panel.add(hostButton, gbc);

        JLabel ipLabel = new JLabel("Join via IP Address");
        ipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        panel.add(ipLabel, gbc);

        JTextField ipField = new JTextField();
        ipField.setName("ipInputField");
        ipField.setPreferredSize(new Dimension(280, 35));
        gbc.gridy = 3;
        panel.add(ipField, gbc);

        JButton joinButton = new JButton("Connect to Host");
        joinButton.setPreferredSize(new Dimension(280, 45));
        joinButton.addActionListener(e -> handleJoinGame(ipField.getText().trim()));
        gbc.gridy = 4;
        panel.add(joinButton, gbc);

        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> screenManager.showMainMenu());
        gbc.gridy = 5;
        panel.add(backButton, gbc);

        return panel;
    }

    private JPanel buildHostPanel() {
        JPanel panel = buildLobbyStatePanel("Host Lobby");
        Box body = (Box) panel.getComponent(1);

        JLabel ip = new JLabel("IP Address: ");
        ip.setName("hostIpLabel");
        ip.setAlignmentX(LEFT_ALIGNMENT);
        body.add(ip);
        body.add(Box.createVerticalStrut(8));

        JLabel status = new JLabel("Host status: waiting for players...");
        status.setName("hostStatusLabel");
        status.setAlignmentX(LEFT_ALIGNMENT);
        body.add(status);
        body.add(Box.createVerticalStrut(12));

        JLabel playersHeader = new JLabel("Connected Players");
        playersHeader.setFont(new Font("Arial", Font.BOLD, 16));
        playersHeader.setAlignmentX(LEFT_ALIGNMENT);
        body.add(playersHeader);
        body.add(Box.createVerticalStrut(8));

        for (int i = 1; i <= 4; i++) {
            JLabel slot = new JLabel("Player " + i + ": waiting...");
            slot.setAlignmentX(LEFT_ALIGNMENT);
            hostPlayerSlots.add(slot);
            body.add(slot);
        }

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton start = new JButton("Start Game");
        start.setName("hostStartButton");
        start.setEnabled(false);
        start.addActionListener(e -> screenManager.sendHostStart());
        controls.add(start);

        JButton leave = new JButton("Leave Lobby");
        leave.addActionListener(e -> {
            screenManager.disconnectClient();
            resetLobby();
            screenManager.showMainMenu();
        });
        controls.add(leave);
        body.add(Box.createVerticalStrut(16));
        body.add(controls);

        return panel;
    }

    private JPanel buildJoinPanel() {
        JPanel panel = buildLobbyStatePanel("Joined Lobby");
        Box body = (Box) panel.getComponent(1);

        JLabel status = new JLabel("Join status: disconnected");
        status.setName("joinStatusLabel");
        status.setAlignmentX(LEFT_ALIGNMENT);
        body.add(status);
        body.add(Box.createVerticalStrut(12));

        JLabel playersHeader = new JLabel("Players in this lobby");
        playersHeader.setFont(new Font("Arial", Font.BOLD, 16));
        playersHeader.setAlignmentX(LEFT_ALIGNMENT);
        body.add(playersHeader);
        body.add(Box.createVerticalStrut(8));

        for (int i = 1; i <= 4; i++) {
            JLabel slot = new JLabel("Player " + i + ": waiting...");
            slot.setAlignmentX(LEFT_ALIGNMENT);
            joinPlayerSlots.add(slot);
            body.add(slot);
        }

        JButton leave = new JButton("Leave Lobby");
        leave.setAlignmentX(LEFT_ALIGNMENT);
        leave.addActionListener(e -> {
            screenManager.disconnectClient();
            resetLobby();
            screenManager.showMainMenu();
        });
        body.add(Box.createVerticalStrut(16));
        body.add(leave);

        return panel;
    }

    private JPanel buildLobbyStatePanel(String titleText) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel(titleText, SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(title, BorderLayout.NORTH);

        Box body = new Box(BoxLayout.Y_AXIS);
        body.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private void handleHostGame() {
        isHostFlow = true;
        hostIpLabel.setText("IP Address: " + resolveLocalIpAddress());
        hostStatusLabel.setText("Host status: starting server...");
        cardLayout.show(cardContainer, HOST_VIEW);

        boolean started = screenManager.startLocalServer();
        if (!started) {
            JOptionPane.showMessageDialog(this, "Server is already running or failed to start.", "Host Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        hostStatusLabel.setText("Host status: connecting to local server...");
        screenManager.connectClient("127.0.0.1");
    }

    private void handleJoinGame(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid host IP address.", "Invalid IP", JOptionPane.WARNING_MESSAGE);
            return;
        }
        isHostFlow = false;
        joinStatusLabel.setText("Join status: connecting to " + ipAddress + "...");
        cardLayout.show(cardContainer, JOIN_VIEW);
        screenManager.connectClient(ipAddress);
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
