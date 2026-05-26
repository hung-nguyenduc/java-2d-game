package state;

import collision.CollisionChecker;
import entity.Enemy;
import entity.Weapon;
import entity.Bullet;
import main.MouseHandler;
import main.*;
import collision.Obstacle;
import collision.ObstacleManager;

//import java.awt.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;


public class ZombieState extends GameState {
    private BufferedImage mapImage;
    private static final String MAP_PATH = "/maps/destroyed-c1.png";
    private static final Font HUD_FONT = new Font("Arial", Font.BOLD, 20);
    private List<Obstacle> obstacles;
    //private CollisionChecker collisionChecker;
    private Weapon weapon;
    //private Bullet bullet;

    public ZombieState(GamePanel gp) {
        super(gp);
        obstacles = new ArrayList<>();
        weapon = new Weapon(gp, gp.mouseH, gp.player);
    }

    double scale;
    @Override
    public void enter() {
        // Pre-scale map một lần duy nhất → mỗi frame chỉ blit vùng nhìn thấy (GPU accelerated)
        try {
            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_PATH));

            this.scale = 1.0 / 1.0;
            int mapWidth = (int) (src.getWidth() * scale);
            int mapHeight = (int) (src.getHeight() * scale);

            // Cập nhật lại kích thước thế giới trong game
            gp.worldWidth = mapWidth;
            gp.worldHeight = mapHeight;

            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compat = gc.createCompatibleImage(mapWidth, mapHeight, Transparency.OPAQUE);
            Graphics2D mg = compat.createGraphics();

            // Bật chế độ mượt hình ảnh khi thu nhỏ
            mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Vẽ thu nhỏ bức ảnh gốc vào mapImage
            mg.drawImage(src, 0, 0, mapWidth, mapHeight, null);
            mg.dispose();
            mapImage = compat;
        } catch (IOException e) {
            e.printStackTrace();
        }
        gp.enemies.clear();
        gp.killCount = 0;

        gp.player.setDefaultValues();
        gp.player.spawnAtCenter();
        gp.player.health = gp.player.maxHealth;

        this.weapon = new Weapon(gp, gp.mouseH, gp.player);
        gp.player.equipWeapon(this.weapon);
        weapon.clearBullets();
        this.obstacles = ObstacleManager.loadObstacles("/maps/ktx_obstacles.txt", this.scale);
        spawnEnemies();
    }

    @Override
    public List<Obstacle> getObstacles() {
        return obstacles;
    }
    @Override
    public void exit() {
        gp.enemies.clear();
         obstacles.clear();
         weapon.clearBullets();
    }

    @Override
    public void update() {
        gp.player.update();
        for (int i = 0; i < gp.enemies.size(); i++) {
            gp.enemies.get(i).update();
        }

        gp.checkCollisions();
        if (gp.player.health <= 0) {
            gp.setState(new GameOverState(gp));
            return;
        }

        if (gp.killCount >= 3) {
            gp.setState(new LevelCompleteState(gp, 1, new Level2State(gp)));
        }

    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Chỉ copy vùng camera nhìn thấy (~768×576) thay vì toàn bộ 2000×2000
        g2.drawImage(mapImage,
            0, 0, gp.screenWidth, gp.screenHeight,
            cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight,
            null);

        // 5. VẼ CÁC VẬT CẢN
        for (Obstacle obs : obstacles) {
            obs.draw(g2, cameraX, cameraY);
        }

        gp.player.draw(g2, cameraX, cameraY);
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2, cameraX, cameraY);
        }
        int screenX = (int) (gp.player.worldX - cameraX);
        int screenY = (int) (gp.player.worldY - cameraY);

        weapon.draw(g2, screenX, screenY, cameraX, cameraY);
        g2.setColor(Color.RED);
        g2.setFont(HUD_FONT);
        g2.drawString("Man 1 - Giet quai: " + gp.killCount + " / 3", 10, 30);
    }

    private void spawnEnemies() {
        gp.enemies.add(new Enemy(gp, gp.player, 300, 300, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 800, 500, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1200, 700, 2));
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}
