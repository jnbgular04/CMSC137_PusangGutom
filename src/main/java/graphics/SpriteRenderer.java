package graphics;

import java.awt.*;
import java.util.List;
import entities.Cat;
import entities.Mouse;

public class SpriteRenderer {

    // Cat placeholder size
    private static final int CAT_SIZE = 40;

    // Mouse placeholder size
    private static final int MOUSE_SIZE = 20;

    /**
     * Draws the Cat as an orange square with a label.
     */
    public void drawCat(Graphics g, Cat cat) {
        // Draw body
        g.setColor(new Color(255, 165, 0)); // Orange
        g.fillRect(cat.x - CAT_SIZE / 2, cat.y - CAT_SIZE / 2, CAT_SIZE, CAT_SIZE);

        // Draw outline
        g.setColor(Color.WHITE);
        g.drawRect(cat.x - CAT_SIZE / 2, cat.y - CAT_SIZE / 2, CAT_SIZE, CAT_SIZE);

        // Draw label
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        g.drawString("CAT", cat.x - 10, cat.y + CAT_SIZE);
    }

    /**
     * Draws each active Mouse as a gray square with a label.
     */
    public void drawMice(Graphics g, List<Mouse> mice) {
        for (Mouse mouse : mice) {
            if (!mouse.isActive) continue;

            // Draw body
            g.setColor(new Color(180, 180, 180)); // Gray
            g.fillRect(mouse.x - MOUSE_SIZE / 2, mouse.y - MOUSE_SIZE / 2, MOUSE_SIZE, MOUSE_SIZE);

            // Draw outline
            g.setColor(Color.WHITE);
            g.drawRect(mouse.x - MOUSE_SIZE / 2, mouse.y - MOUSE_SIZE / 2, MOUSE_SIZE, MOUSE_SIZE);

            // Draw label
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.setColor(Color.WHITE);
            g.drawString("MOUSE", mouse.x - 15, mouse.y + MOUSE_SIZE);
        }
    }

    /**
     * Draws the HUD (score + player name) at the top of the screen.
     */
    public void drawHUD(Graphics g, Cat cat) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Player: " + cat.name, 20, 30);
        g.drawString("Score: " + cat.score, 20, 55);
    }
}