package main;

import entity.Enemy;
import entity.Checkpoint;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

// Concrete GameState for the "Zombie" level (current map/level 0)
public class ZombieState extends GameState {
    private Image mapImage;
    private Checkpoint checkpoint;
    private String mapPath = "/maps/c1.png"; // Current map path

    // Constructor: Initialize with GamePanel
    public ZombieState(GamePanel gp) {
        super(gp);
    }

    // Enter the state: Load map, spawn enemies, and checkpoint
    @Override
    public void enter() {
        // Load map image
        mapImage = new ImageIcon(getClass().getResource(mapPath)).getImage();

        // Clear and spawn enemies (reuse GamePanel's spawnEnemies logic)
        gp.enemies.clear();
        spawnEnemies();

        // Spawn checkpoint
        spawnCheckpoint();
    }

    // Exit the state: Cleanup resources
    @Override
    public void exit() {
        gp.enemies.clear();
        checkpoint = null;
    }

    // Update logic for this level
    @Override
    public void update() {
        // Update player and enemies (delegate to GamePanel for shared logic)
        gp.player.update();
        for (Enemy enemy : gp.enemies) {
            enemy.update();
        }

        // Check checkpoint collision and handle transitions
        if (checkpoint != null) {
            checkCheckpointCollision();
        }

        // Check collisions (delegate to GamePanel)
        gp.checkCollisions();
    }

    // Draw logic for this level
    @Override
    public void draw(Graphics2D g2) {
        // Calculate camera position (reuse GamePanel's camera logic)
        int cameraX = gp.player.worldX - (gp.screenWidth / 2);
        int cameraY = gp.player.worldY - (gp.screenHeight / 2);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        int mapScreenX = -cameraX;
        int screenY = -cameraY;

        // Draw map
        g2.drawImage(mapImage, mapScreenX, screenY, gp.worldWidth, gp.worldHeight, null);

        // Draw player, enemies, and checkpoint
        gp.player.draw(g2);
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2);
        }
        if (checkpoint != null) {
            checkpoint.draw(g2, gp.player.worldX, gp.player.worldY);
        }
    }

    // Spawn enemies (copied from GamePanel for this level)
    private void spawnEnemies() {
        gp.enemies.add(new Enemy(gp, gp.player, 300, 300, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 800, 500, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1200, 700, 2));
    }

    // Spawn checkpoint (copied from GamePanel)
    private void spawnCheckpoint() {
        checkpoint = new Checkpoint(gp, gp.worldWidth / 2 - 40, gp.worldHeight / 2 - 40);
    }

    // Check checkpoint collision (copied from GamePanel)
    private void checkCheckpointCollision() {
        if (gp.player.worldX + 80 >= checkpoint.worldX && gp.player.worldX <= checkpoint.worldX + checkpoint.sizeX &&
            gp.player.worldY + 80 >= checkpoint.worldY && gp.player.worldY <= checkpoint.worldY + checkpoint.sizeY) {
            gp.nextMap(); // Delegate transition to GamePanel
        }
    }

    // Handle mouse clicks (not used in gameplay state)
    @Override
    public void handleMouseClick(java.awt.event.MouseEvent e) {
        // No mouse interaction in gameplay
    }
}
