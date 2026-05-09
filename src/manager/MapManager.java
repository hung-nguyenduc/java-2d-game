package manager;

import main.GameConfig;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;


public class MapManager {
    private ArrayList<Rectangle> walls;
    private BufferedImage backgroundImage;
    
    public MapManager() {
        walls = new ArrayList<>();
        initWalls();
        // Load background image
        backgroundImage = AssetManager.getImage("bg_grid");
    }
    
    private void initWalls() {
        // Tường bao quanh map
        walls.add(new Rectangle(0, 0, GameConfig.WORLD_WIDTH, 40));           // Trên
        walls.add(new Rectangle(0, GameConfig.WORLD_HEIGHT - 40, GameConfig.WORLD_WIDTH, 40)); // Dưới
        walls.add(new Rectangle(0, 0, 40, GameConfig.WORLD_HEIGHT));          // Trái
        walls.add(new Rectangle(GameConfig.WORLD_WIDTH - 40, 0, 40, GameConfig.WORLD_HEIGHT)); // Phải
        
        // Tường ngăn trong phòng
        walls.add(new Rectangle(400, 300, 40, 200));
        walls.add(new Rectangle(800, 500, 40, 200));
        walls.add(new Rectangle(600, 700, 200, 40));
        walls.add(new Rectangle(1200, 200, 40, 300));
        walls.add(new Rectangle(1000, 800, 300, 40));
        walls.add(new Rectangle(1500, 500, 40, 200));
        walls.add(new Rectangle(300, 1000, 200, 40));
        walls.add(new Rectangle(1600, 1000, 40, 300));
    }
    
    public boolean checkCollision(Rectangle rect) {
        for (Rectangle wall : walls) {
            if (rect.intersects(wall)) {
                return true;
            }
        }
        return false;
    }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        // Vẽ nền với ảnh nếu có, nếu không thì vẽ màu đơn giản
        if (backgroundImage != null) {
            // Tiling background image
            int tileWidth = backgroundImage.getWidth();
            int tileHeight = backgroundImage.getHeight();
            
            int startX = (cameraX / tileWidth) * tileWidth - cameraX;
            int startY = (cameraY / tileHeight) * tileHeight - cameraY;
            
            for (int x = startX - tileWidth; x < GameConfig.SCREEN_WIDTH; x += tileWidth) {
                for (int y = startY - tileHeight; y < GameConfig.SCREEN_HEIGHT; y += tileHeight) {
                    g2.drawImage(backgroundImage, x, y, tileWidth, tileHeight, null);
                }
            }
        } else {
            // Fallback: vẽ nền đơn giản
            g2.setColor(GameConfig.BACKGROUND_COLOR);
            g2.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
            
            // Vẽ grid (cho đẹp)
            g2.setColor(new Color(40, 40, 50));
            int gridSize = 50;
            int startX = -cameraX % gridSize;
            int startY = -cameraY % gridSize;
            
            for (int x = startX; x < GameConfig.SCREEN_WIDTH; x += gridSize) {
                g2.drawLine(x, 0, x, GameConfig.SCREEN_HEIGHT);
            }
            for (int y = startY; y < GameConfig.SCREEN_HEIGHT; y += gridSize) {
                g2.drawLine(0, y, GameConfig.SCREEN_WIDTH, y);
            }
        }
        
        // Vẽ tường
        g2.setColor(GameConfig.WALL_COLOR);
        for (Rectangle wall : walls) {
            int screenX = wall.x - cameraX;
            int screenY = wall.y - cameraY;
            g2.fillRect(screenX, screenY, wall.width, wall.height);
            
            // Viền tường
            g2.setColor(Color.BLACK);
            g2.drawRect(screenX, screenY, wall.width, wall.height);
            g2.setColor(GameConfig.WALL_COLOR);
        }
    }
}