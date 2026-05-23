package main;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

// Abstract class representing a game state (e.g., a level or screen)
public abstract class GameState {
    protected GamePanel gp; // Reference to the main game panel for shared resources

    // Constructor: Initialize with GamePanel reference
    public GameState(GamePanel gp) {
        this.gp = gp;
    }

    // Called when entering this state (e.g., initialize level-specific data)
    public abstract void enter();

    // Called when exiting this state (e.g., cleanup resources)
    public abstract void exit();

    // Update logic for this state (e.g., player movement, enemy AI)
    public abstract void update();

    // Draw logic for this state (e.g., render map, entities)
    public abstract void draw(Graphics2D g2);

    // Handle mouse clicks (e.g., for menu buttons)
    public abstract void handleMouseClick(MouseEvent e);

    // Thêm hàm này vào file GameState.java
    public java.util.List<Obstacle> getObstacles() {
        return new java.util.ArrayList<>();
    }
}
