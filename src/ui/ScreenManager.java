package ui;

import javax.swing.*;
import java.awt.*;

/**
 * ScreenManager controls the JFrame and uses CardLayout to swap between
 * the MainMenuPanel, LobbyPanel, and GameStage.
 */
public class ScreenManager {

    private JFrame mainFrame;
    private JPanel cardContainer;
    private CardLayout cardLayout;
    
    // String identifiers for the CardLayout mappings
    private final String MENU_VIEW = "MAIN_MENU";
    private final String GAME_VIEW = "GAME_STAGE";
    
    // Placeholder References (Jaz will flesh these out)
    private JPanel mainMenuPanel;
    private JPanel gameStagePanel;

    public ScreenManager(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeLayout();
    }

    /**
     * Sets up the core CardLayout container and attaches it to the JFrame.
     */
    private void initializeLayout() {
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        
        // TODO: Replace with Jaz's actual MainMenuPanel and GameStage implementations
        mainMenuPanel = new JPanel();
        mainMenuPanel.setBackground(Color.DARK_GRAY);
        mainMenuPanel.add(new JLabel("MAIN MENU PLACEHOLDER"));
        
        gameStagePanel = new JPanel();
        gameStagePanel.setBackground(Color.BLACK);
        gameStagePanel.add(new JLabel("GAME STAGE PLACEHOLDER"));
        
        // Add panels to the card container
        cardContainer.add(mainMenuPanel, MENU_VIEW);
        cardContainer.add(gameStagePanel, GAME_VIEW);
        
        // Add the container to the main window
        mainFrame.add(cardContainer);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    /**
     * Swaps the view to the Main Menu.
     */
    public void showMainMenu() {
        cardLayout.show(cardContainer, MENU_VIEW);
    }

    /**
     * Swaps the view to the active Game Stage.
     * @param isMultiplayer Flag to determine if local or server logic should run (M2 integration)
     */
    public void showGame(boolean isMultiplayer) {
        if (!isMultiplayer) {
            System.out.println("Starting Singleplayer Game Mode...");
            // NOTE FOR INTEGRATION: This is where you will initialize GameManager 
            // and call gameManager.startGame(), followed by gameLoop.start();
        } else {
            System.out.println("Starting Multiplayer Game Mode (Milestone 2)...");
        }
        
        cardLayout.show(cardContainer, GAME_VIEW);
        
        // Ensure the game panel can receive keyboard/mouse focus
        gameStagePanel.requestFocusInWindow(); 
    }
}