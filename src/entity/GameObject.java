package entity;

import main.GameConfig;
import manager.AssetManager;
import java.awt.image.BufferedImage;
import java.awt.Color;  // ← THÊM DÒNG NÀY
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class GameObject {
    private String name;
    private int x, y;
    private int width, height;
    private boolean isSolid;
    private Rectangle hitbox;
    private BufferedImage image;
    
    public GameObject(String name, int x, int y, int width, int height, boolean isSolid) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isSolid = isSolid;
        this.hitbox = new Rectangle(x, y, width, height);
        
        // Load ảnh theo tên
        loadImageByName(name);
    }
    
    private void loadImageByName(String name) {
        switch(name.toLowerCase()) {
            case "desk":
                image = AssetManager.getImage("desk");
                break;
            case "chair":
                image = AssetManager.getImage("chair");
                break;
            case "bookshelf":
                image = AssetManager.getImage("bookshelf");
                break;
            default:
                image = null;
        }
    }
    
    public Rectangle getHitbox() {
        return hitbox;
    }
    
    public boolean isSolid() {
        return isSolid;
    }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = x - cameraX;
        int screenY = y - cameraY;
        
        if (image != null && AssetManager.hasImages()) {
            g2.drawImage(image, screenX, screenY, width, height, null);
        } else {
            g2.setColor(GameConfig.OBJECT_COLOR);
            g2.fillRect(screenX, screenY, width, height);
            g2.setColor(Color.BLACK);
            g2.drawRect(screenX, screenY, width, height);
        }
    }
}