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
    
    private final Image[] catImages = new Image[4];
    private Image catImage;
    private Image mouseImage;
    private int localPlayerId; // for the 

    public SpriteRenderer(int localPlayerId) {
        this.localPlayerId = Math.max(1, Math.min(4, localPlayerId));
        loadAssets();
    }
    
    private void loadAssets() {
        try {
            mouseImage = ImageIO.read(new File("assets/Mouse_Target.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load per-player cat images independently so one missing file
        // does not break all cat rendering.
        for (int i = 1; i <= 4; i++) {
            String catPath = "assets/cat_player" + i + ".PNG";
            try {
                catImages[i - 1] = ImageIO.read(new File(catPath));
            } catch (IOException e) {
                System.out.println("Missing/invalid cat asset: " + catPath);
                if (i == 1) {
                    try {
                        catImages[0] = ImageIO.read(new File("assets/cat_player.PNG"));
                    } catch (IOException ignored) {
                        catImages[0] = null;
                    }
                } else {
                    catImages[i - 1] = null;
                }
            }
        }

        // Fallback to any available cat image so rendering still works.
        catImage = catImages[localPlayerId - 1];
        if (catImage == null) {
            for (Image img : catImages) {
                if (img != null) {
                    catImage = img;
                    break;
                }
            }
        }
    }

    public void setLocalPlayerId(int localPlayerId) {
        int sanitizedId = Math.max(1, Math.min(4, localPlayerId));
        if (this.localPlayerId == sanitizedId) {
            return;
        }
        this.localPlayerId = sanitizedId;
        if (catImages[sanitizedId - 1] == null) {
            loadAssets();
        } else {
            catImage = catImages[sanitizedId - 1];
        }
    }

    public void drawCat(Graphics g, Cat cat, int mouseX, int mouseY) {
        if (catImage == null) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double angle = 0;

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

    // Rendering for Multiplayer
    public void drawCatForPlayer(Graphics g, int playerId, int catX, int catY, int targetX, int targetY) {
        int sanitizedId = Math.max(1, Math.min(4, playerId));
        Image playerCatImage = catImages[sanitizedId - 1];
        if (playerCatImage == null) {
            playerCatImage = catImage; // fallback image if specific player asset is missing
            if (playerCatImage == null) return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Preserve the source aspect ratio to avoid stretching.
        int srcW = Math.max(1, playerCatImage.getWidth(null));
        int srcH = Math.max(1, playerCatImage.getHeight(null));
        int targetLongEdge = 1500;
        int catWidth;
        int catHeight;
        if (srcW >= srcH) {
            catWidth = targetLongEdge;
            catHeight = (int) Math.round(((double) srcH / srcW) * targetLongEdge);
        } else {
            catHeight = targetLongEdge;
            catWidth = (int) Math.round(((double) srcW / srcH) * targetLongEdge);
        }

        // Per-player tuning: edit values inside each case block.
        int anchorOffsetX = 0;
        int anchorOffsetY = 0;
        int pawPadding;
        int pivotX;
        int pivotY;
        int drawX;
        int drawY;

        int bodyX = catX;
        int bodyY = catY;
        double angle;

        switch (sanitizedId) {
            case 1: // copied from milestone1
                anchorOffsetX = 24;
                anchorOffsetY = 0;
                pawPadding = 150; 
                bodyX = catX + anchorOffsetX;
                bodyY = catY + anchorOffsetY;
                angle = 0;
                pivotX = targetX;
                pivotY = targetY;
                drawX = -catWidth + pawPadding;
                drawY = -catHeight / 2;
                break;
            case 2:
                anchorOffsetX = 0;
                anchorOffsetY = 16;
                bodyX = catX + anchorOffsetX;
                bodyY = catY + anchorOffsetY;
                angle = 0;
                
                pivotX = targetX;
                pivotY = targetY;
                
          
                pawPadding = 240; 
                
                drawX = -pawPadding; 
                drawY = -catHeight + 170;
                break;
            case 3: // RIGHT WALL (Horizontal asset pointing left toward the pit)
                anchorOffsetX = -24;
                anchorOffsetY = 0;
                bodyX = catX + anchorOffsetX;
                bodyY = catY + anchorOffsetY;
                angle = 0;
  
                pivotX = targetX;
                pivotY = targetY;
                pawPadding = 150; 
                
                drawX = -pawPadding; 
                drawY = -catHeight / 2;
                break;
            case 4: // BOTTOM WALL (Vertical asset naturally pointing UP toward the pit)
                anchorOffsetX = 0;
                anchorOffsetY = -24;
                bodyX = catX + anchorOffsetX;
                bodyY = catY + anchorOffsetY;
                
                angle = 0;

                
                // 2. Lock the rotation pivot center right on your cursor target
                pivotX = targetX;
                pivotY = targetY;
                
                // 3. Since the paw is at the top of the PNG, we pull the image down slightly 
                // past the cursor pivot so the mouse rests right on the pink paw pads.
                pawPadding = 150; // Match your Case 3 padding size feel
                
                // 4. THE VERTICAL FLIP OF CASE 3 LOGIC:
                // Horizontally center the thickness (width) of the arm asset over the cursor
                drawX = -catWidth / 2; 
                
                // Vertically align the top edge of the drawing (the paw) to the cursor
                drawY = -pawPadding; 
                break;
            default:
                pawPadding = 150;
                bodyX = catX;
                bodyY = catY;
                angle = Math.atan2(targetY - bodyY, targetX - bodyX);
                pivotX = targetX;
                pivotY = targetY;
                drawX = -catWidth + pawPadding;
                drawY = -170;
                break;
        }

        g2d.translate(pivotX, pivotY);
        g2d.rotate(angle);
        g2d.drawImage(playerCatImage, drawX, drawY, catWidth, catHeight, null);

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