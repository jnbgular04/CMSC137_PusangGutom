package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

import ui.ScreenManager;
import engine.GameManager;
import engine.GameLoop;

/**
 * Main bootstraps the game, initializes the JFrame, and sets up the initial view.
 */
public class Main {
    
    public static void main(String[] args) {
        // Swing GUI updates should be run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            
            JFrame mainFrame = new JFrame("Pusang Gutom - M1 Engine Test");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // STRICT DIMENSIONS: Setting the content pane ensures the internal 
            // playable area is exactly 1280x720, ignoring the title bar padding.
            mainFrame.getContentPane().setPreferredSize(new Dimension(1280, 720));
            
            // PER PROJECT GUIDELINES: pack, resizable false, center
            mainFrame.pack();
            mainFrame.setResizable(false);
            mainFrame.setLocationRelativeTo(null);
            
            // 1. Initialize the Switchboard (UI)
            ScreenManager screenManager = new ScreenManager(mainFrame);
            
            // 2. Initialize the Engine (Logic)
            GameManager gameManager = new GameManager(screenManager);
            GameLoop gameLoop = new GameLoop(gameManager);
            
            // Show the window
            mainFrame.setVisible(true);
//            
//            // ==========================================
//            // 🚨 TEMPORARY AUTO-START FOR STEVEN'S TESTING
//            // Remove this block later when Jaz implements the Main Menu buttons!
//            // ==========================================
//            System.out.println("--- TRIGGERING AUTO-START FOR ENGINE TESTING ---");
//            screenManager.showGame(false); // Swaps to game view
//            gameManager.startGame();       // Resets timer/score
//            gameLoop.start();              // Starts the 60FPS thread
//            
        });
    }
}