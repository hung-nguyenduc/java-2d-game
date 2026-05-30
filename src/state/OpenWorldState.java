package state;

import collision.Obstacle;
import collision.ObstacleManager;
import entity.Enemy;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenWorldState extends GameState {
    
    private static class Region {
        String name;
        String imagePath;
        String obstaclePath;
        int offsetX, offsetY;
        double scale;
        Image image;
        int width, height;
        List<Obstacle> obstacles = new ArrayList<>();
        boolean enemiesSpawned = false;
        
        public Region(String name, String imagePath, String obstaclePath, int offsetX, int offsetY, double scale) {
            this.name = name;
            this.imagePath = imagePath;
            this.obstaclePath = obstaclePath;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.scale = scale;
        }
    }

    private List<Region> regions = new ArrayList<>();
    private List<Obstacle> allObstacles = new ArrayList<>();
    private Font HUD_FONT = new Font("Arial", Font.BOLD, 20);

    // Dịch chuyển
    private Rectangle ktxToClassroomDoor;
    private Rectangle classroomToKtxDoor;

    public OpenWorldState(GamePanel gp) {
        super(gp);
        // Thiết lập các khu vực (Region)
        // Kích thước các map đều được scale với tỉ lệ 1.0/2.5.
        // Bạn có thể chỉnh lại toạ độ các toà nhà ở đây.
        regions.add(new Region("KTX", "/maps/ktx.png", "/maps/ktx_obstacles.txt", 0, 0, 1.0 / 2.5));
        regions.add(new Region("Level2", "/maps/test.png", null, 4000, 0, 1.0 / 2.5));
        regions.add(new Region("Classroom", "/maps/classroom.png", "/maps/classroom_obstacles.txt", 8000, 0, 1.0 / 2.5));
    }

    @Override
    public void enter() {
        int maxWorldW = 0;
        int maxWorldH = 0;
        allObstacles.clear();

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();

        for (Region r : regions) {
            try {
                InputStream is = getClass().getResourceAsStream(r.imagePath);
                if (is != null) {
                    BufferedImage src = ImageIO.read(is);
                    r.width = (int) (src.getWidth() * r.scale);
                    r.height = (int) (src.getHeight() * r.scale);

                    BufferedImage compat = gc.createCompatibleImage(r.width, r.height, Transparency.OPAQUE);
                    Graphics2D mg = compat.createGraphics();
                    mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    mg.drawImage(src, 0, 0, r.width, r.height, null);
                    mg.dispose();
                    r.image = compat;

                    // Tính lại world size
                    if (r.offsetX + r.width > maxWorldW) maxWorldW = r.offsetX + r.width;
                    if (r.offsetY + r.height > maxWorldH) maxWorldH = r.offsetY + r.height;
                }

                // Đọc obstacles
                if (r.obstaclePath != null) {
                    List<Obstacle> obsList = ObstacleManager.loadObstacles(r.obstaclePath, r.scale);
                    for (Obstacle o : obsList) {
                        // Cộng thêm offset
                        o.worldX += r.offsetX;
                        o.worldY += r.offsetY;
                        r.obstacles.add(o);
                        allObstacles.add(o);
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi nạp ảnh bản đồ tại: " + r.imagePath);
                e.printStackTrace();
            }
        }

        gp.worldWidth = maxWorldW;
        gp.worldHeight = maxWorldH;

        // Reset hệ thống
        gp.killCount = 0;
        gp.player.health = gp.player.maxHealth;
        gp.player.bullets.clear();
        gp.enemies.clear();

        // Spawn người chơi ở giữa KTX
        Region ktx = regions.get(0);
        gp.player.worldX = ktx.offsetX + ktx.width / 2.0;
        gp.player.worldY = ktx.offsetY + ktx.height / 2.0;

        // Vùng dịch chuyển (Portal)
        // Ví dụ: cửa của KTX dẫn tới Classroom
        ktxToClassroomDoor = new Rectangle(ktx.offsetX + 400, ktx.offsetY + 400, 100, 100);
        Region classroom = regions.get(2);
        classroomToKtxDoor = new Rectangle(classroom.offsetX + 400, classroom.offsetY + 400, 100, 100);
        
        checkRegionSpawn();
    }
    
    private void checkRegionSpawn() {
        for (Region r : regions) {
            // Kiểm tra xem player có đang ở trong vùng này không
            if (!r.enemiesSpawned && 
                gp.player.worldX >= r.offsetX && gp.player.worldX <= r.offsetX + r.width &&
                gp.player.worldY >= r.offsetY && gp.player.worldY <= r.offsetY + r.height) {
                
                spawnEnemiesForRegion(r);
                r.enemiesSpawned = true;
            }
        }
    }
    
    private void spawnEnemiesForRegion(Region r) {
        if (r.name.equals("KTX")) {
            // Không có quái
        } else if (r.name.equals("Classroom")) {
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 300,  r.offsetY + 400, 0));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 700,  r.offsetY + 300, 1));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 1100, r.offsetY + 600, 2));
        } else if (r.name.equals("Level2")) {
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 200,  r.offsetY + 800, 0));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 600,  r.offsetY + 200, 1));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 1400, r.offsetY + 400, 2));
        }
    }

    @Override
    public List<Obstacle> getObstacles() {
        return allObstacles;
    }

    @Override
    public void update() {
        gp.player.update();
        
        checkRegionSpawn();

        for (int i = 0; i < gp.enemies.size(); i++) {
            gp.enemies.get(i).update();
        }

        gp.checkCollisions();

        if (gp.player.health <= 0) {
            gp.setState(new GameOverState(gp));
            return;
        }

        // Logic dịch chuyển giữa các toà nhà (Teleport)
        Rectangle playerRect = new Rectangle((int)gp.player.worldX, (int)gp.player.worldY, gp.tileSize, gp.tileSize);
        if (playerRect.intersects(ktxToClassroomDoor)) {
            Region classroom = regions.get(2);
            gp.player.worldX = classroom.offsetX + classroom.width / 2.0;
            gp.player.worldY = classroom.offsetY + classroom.height / 2.0;
        } else if (playerRect.intersects(classroomToKtxDoor)) {
            Region ktx = regions.get(0);
            gp.player.worldX = ktx.offsetX + ktx.width / 2.0;
            gp.player.worldY = ktx.offsetY + ktx.height / 2.0;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);

        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Màu nền chung cho cả không gian World
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Vẽ tất cả các vùng map có trong camera
        for (Region r : regions) {
            if (r.image != null) {
                // Kiểm tra xem region này có nằm trong màn hình camera không để tối ưu
                if (r.offsetX + r.width > cameraX && r.offsetX < cameraX + gp.screenWidth &&
                    r.offsetY + r.height > cameraY && r.offsetY < cameraY + gp.screenHeight) {
                    
                    int drawX = r.offsetX - cameraX;
                    int drawY = r.offsetY - cameraY;
                    g2.drawImage(r.image, drawX, drawY, r.width, r.height, null);
                }
            }
        }

        // Vẽ cổng dịch chuyển
        g2.setColor(new Color(255, 255, 0, 100));
        g2.fillRect(ktxToClassroomDoor.x - cameraX, ktxToClassroomDoor.y - cameraY, ktxToClassroomDoor.width, ktxToClassroomDoor.height);
        g2.fillRect(classroomToKtxDoor.x - cameraX, classroomToKtxDoor.y - cameraY, classroomToKtxDoor.width, classroomToKtxDoor.height);

        // Vẽ Enemy
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2, cameraX, cameraY);
        }

        // Vẽ Player
        gp.player.draw(g2, cameraX, cameraY);

        // Vẽ HUD
        g2.setColor(Color.WHITE);
        g2.setFont(HUD_FONT);
        g2.drawString("OPEN WORLD MODE", 10, 30);
        g2.drawString("KILLS: " + gp.killCount, 10, 60);
    }

    @Override
    public void exit() {
        gp.enemies.clear();
        allObstacles.clear();
        for (Region r : regions) {
            if (r.image != null) {
                r.image.flush();
            }
        }
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
    }
}
