package com.cmsc137.ui;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.cmsc137.engine.GameLoop;
import com.cmsc137.engine.GameManager;
import com.cmsc137.graphics.GameStage;
import com.cmsc137.network.ClientConnection;
import com.cmsc137.network.GameServer;

public class ScreenManager {

    private JFrame mainFrame;
    private JPanel cardContainer;
    private CardLayout cardLayout;

    private final String MENU_VIEW = "MAIN_MENU";
    private final String LOBBY_VIEW = "LOBBY_VIEW";
    private final String GAME_VIEW = "GAME_STAGE";

    private JPanel mainMenuPanel;
    private LobbyPanel lobbyPanel;
    private GameStage gameStagePanel;  // Use GameStage type, not JPanel

    // ADD THESE
    private GameManager gameManager;
    private GameLoop gameLoop;
    private ClientConnection clientConnection;
    private boolean localServerStarted = false;

    public ScreenManager(JFrame mainFrame) {
        this.mainFrame = mainFrame;

    
        this.gameManager = new GameManager(this);
        this.clientConnection = new ClientConnection(gameManager, this);
        this.gameManager.setClientConnection(this.clientConnection);

        initializeLayout();
    }

    private void initializeLayout() {
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        mainMenuPanel = new MainMenuPanel(mainFrame, this);
        lobbyPanel = new LobbyPanel(this);
        gameStagePanel = new GameStage(gameManager, false); 

        this.gameLoop = new GameLoop(gameManager, gameStagePanel);

        cardContainer.add(mainMenuPanel, MENU_VIEW);
        cardContainer.add(lobbyPanel, LOBBY_VIEW);
        cardContainer.add(gameStagePanel, GAME_VIEW);

        mainFrame.add(cardContainer);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    public void showMainMenu() {
        gameLoop.stop(); // Stop the loop when returning to menu
        cardLayout.show(cardContainer, MENU_VIEW);
        System.out.println("STATE CHECK: View Swapped to -> " + MENU_VIEW);
    }

    public void showLobby() {
        lobbyPanel.resetLobby();
        cardLayout.show(cardContainer, LOBBY_VIEW);
        System.out.println("STATE CHECK: View Swapped to -> " + LOBBY_VIEW);
    }

    public void showGame(boolean isMultiplayer) {
        if (!isMultiplayer) {
            System.out.println("Starting Singleplayer Game Mode...");
            gameManager.setLocalPlayerId(1);
            gameManager.setMultiplayerMode(false);
            gameStagePanel.setLocalPlayerId(gameManager.getLocalPlayerId());
            gameStagePanel.setMultiplayerMode(false);
            gameManager.startGame(); // Reset and start game state
            gameLoop.start();        // Begin the 60FPS tick loop
        } else {
            System.out.println("Starting Multiplayer Game Mode (Milestone 2)...");
            gameManager.setMultiplayerMode(true);
            gameStagePanel.setLocalPlayerId(gameManager.getLocalPlayerId());
            gameStagePanel.setMultiplayerMode(true);
            gameManager.startGame();
            gameLoop.start();
        }

        cardLayout.show(cardContainer, GAME_VIEW);
        System.out.println("STATE CHECK: View Swapped to -> " + GAME_VIEW);

        gameStagePanel.requestFocusInWindow();
    }

    public boolean startLocalServer() {
        if (localServerStarted) {
            return false;
        }
        localServerStarted = true;
        Thread serverThread = new Thread(() -> new GameServer().start(), "LocalGameServer");
        serverThread.setDaemon(true);
        serverThread.start();
        return true;
    }

    public void connectClient(String ipAddress) {
        clientConnection.connect(ipAddress);
    }

    public void disconnectClient() {
        clientConnection.disconnect();
    }

    public void sendHostStart() {
        clientConnection.sendHostStart();
    }

    public void onAssignedPlayerId(int playerId) {
        lobbyPanel.onAssignedPlayerId(playerId);
        gameManager.setLocalPlayerId(playerId);
        gameStagePanel.setLocalPlayerId(playerId);
    }

    public void onLobbyUpdate(int[] connectedPlayers) {
        lobbyPanel.onLobbyUpdated(connectedPlayers, clientConnection.getLocalPlayerID());
        gameManager.updateConnectedPlayers(connectedPlayers);
    }
}