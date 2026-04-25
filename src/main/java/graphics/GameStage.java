package graphics;

import javax.swing.*;
import java.awt.*;
import entities.Cat;
import entities.Mouse;
import java.util.List;

public class GameStage extends JPanel {

    private Cat cat;
    private List<Mouse> mice;
    private SpriteRenderer spriteRenderer;

    public GameStage() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1280, 720));
        setFocusable(true);

        spriteRenderer = new SpriteRenderer();

        // Initialize placeholder Cat
        cat = new Cat();
        cat.x = 640;
        cat.y = 360;
        cat.name = "Player";
        cat.score = 0;

        // Initialize placeholder Mice
        mice = new java.util.ArrayList<>();
        mice.add(new Mouse(200, 150));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        spriteRenderer.drawCat(g, cat);
        spriteRenderer.drawMice(g, mice);
        spriteRenderer.drawHUD(g, cat);
    }

    // Called by GameLoop to refresh the screen
    public void update() {
        repaint();
    }
}