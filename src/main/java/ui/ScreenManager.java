package ui;

import javax.swing.*;
import graphics.GameStage;
import engine.GameManager;
import engine.GameLoop;
import java.awt.*;

public class ScreenManager {

    private JFrame mainFrame;
    private JPanel cardContainer;
    private CardLayout cardLayout;

    private final String MENU_VIEW = "MAIN_MENU";
    private final String GAME_VIEW = "GAME_STAGE";

    private JPanel mainMenuPanel;
    private GameStage gameStagePanel;  // Use GameStage type, not JPanel

    // ADD THESE
    private GameManager gameManager;
    private GameLoop gameLoop;

    public ScreenManager(JFrame mainFrame) {
        this.mainFrame = mainFrame;

    
        this.gameManager = new GameManager(this);

        initializeLayout();
    }

    private void initializeLayout() {
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        mainMenuPanel = new MainMenuPanel(mainFrame, this);
        gameStagePanel = new GameStage(gameManager); 

        this.gameLoop = new GameLoop(gameManager, gameStagePanel);

        cardContainer.add(mainMenuPanel, MENU_VIEW);
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

    public void showGame(boolean isMultiplayer) {
        if (!isMultiplayer) {
            System.out.println("Starting Singleplayer Game Mode...");
            gameManager.startGame(); // Reset and start game state
            gameLoop.start();        // Begin the 60FPS tick loop
        } else {
            System.out.println("Starting Multiplayer Game Mode (Milestone 2)...");
        }

        cardLayout.show(cardContainer, GAME_VIEW);
        System.out.println("STATE CHECK: View Swapped to -> " + GAME_VIEW);

        gameStagePanel.requestFocusInWindow();
    }
}