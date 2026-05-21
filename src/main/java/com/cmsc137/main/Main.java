package com.cmsc137.main;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.cmsc137.ui.ScreenManager;

/**
 * Main bootstraps the game, initializes the JFrame, and sets up the initial view.
 */
public class Main {
    
    public static void main(String[] args) {
        // Swing GUI updates should be run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            
            JFrame mainFrame = new JFrame("Pusang Gutom");
            
            // Prevent native ungraceful crashing so our custom window listener can intercept the close event
            mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            
            // STRICT DIMENSIONS: Setting the content pane ensures the internal 
            // playable area is exactly 1280x720, ignoring the title bar padding. SST_Master Document.md]
            mainFrame.getContentPane().setPreferredSize(new Dimension(1280, 720));
            
            // PER PROJECT GUIDELINES: pack, resizable false, center SST_Master Document.md]
            mainFrame.pack();
            mainFrame.setResizable(false);
            mainFrame.setLocationRelativeTo(null);
            
            ScreenManager screenManager = new ScreenManager(mainFrame);
            
            // Catch the window close event to clean up socket architecture SST_Master Document.md]
            mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Main window close event triggered. Turning off networking systems cleanly...");
                    
                    // Fire master disconnect chains across server-client boundaries SST_Master Document.md]
                    if (screenManager != null) {
                        screenManager.disconnectClient();
                        screenManager.stopLocalServer();
                    }
                    
                    // Now safely drop the local application thread instance
                    System.exit(0);
                }
            });
            
            // Show the window
            mainFrame.setVisible(true);         
        });
    }
}