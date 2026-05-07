package com.cmsc137.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.cmsc137.entities.Cat;
import com.cmsc137.entities.Mouse;

public class SpriteRenderer {

    private static final int MOUSE_SIZE = 100;
    
    private Image catImage;
    private Image mouseImage;

    public SpriteRenderer() {
        try {
        	mouseImage = ImageIO.read(new File("assets/Mouse_Target.PNG"));
            catImage = ImageIO.read(new File("assets/cat_player.PNG"));
        } catch (IOException e) {
            System.out.println("Error: Could not find cat.png in assets folder!");
            e.printStackTrace();
        }
    }

    public void drawCat(Graphics g, Cat cat, int mouseX, int mouseY) {
        if (catImage == null) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Calculate the angle towards the mouse
        double angle = Math.atan2(mouseY - cat.y, mouseX - cat.x);

        // 2. Move to the mouse position (The Paw Position)
        g2d.translate(mouseX, mouseY); 

        // 3. Rotate
        g2d.rotate(angle);

        int catWidth = 1346;  
        int catHeight = 410;
        
        int pawPadding = 150;
        
        g2d.drawImage(catImage, 
        		-catWidth + pawPadding, 
        		-catHeight / 2, 
        		catWidth, 
        		catHeight, null);

        g2d.dispose();
    }

    // Generates the mouse, will replace with drawn graphics
    public void drawMice(Graphics g, List<Mouse> mice) {
        for (Mouse mouse : mice) {
            if (!mouse.isActive) continue;
           
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(mouse.x, mouse.y);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(mouseImage, 
                    -MOUSE_SIZE / 2, 
                    -MOUSE_SIZE / 2, 
                    MOUSE_SIZE, 
                    MOUSE_SIZE, null);

            g2d.dispose();
        }
    }

}