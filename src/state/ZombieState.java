package state;

import collision.CollisionChecker;
import entity.Enemy;
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
    private Image mapImage; // GPU-friendly compatible image
    private static final String MAP_PATH = "/maps/destroyed-c1.png";
    private static final Font HUD_FONT = new Font("Arial", Font.BOLD, 20);
    private List<Obstacle> obstacles;
    private CollisionChecker collisionChecker;
    public ZombieState(GamePanel gp) {
        super(gp);
        obstacles = new ArrayList<>();
    }

    double scale;
    @Override
    public void enter() {
        // Pre-scale map một lần duy nhất → mỗi frame chỉ blit vùng nhìn thấy (GPU accelerated)
        try {
//            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_PATH));
//            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
//                    .getDefaultScreenDevice().getDefaultConfiguration();
//            BufferedImage compat = gc.createCompatibleImage(gp.worldWidth, gp.worldHeight, Transparency.OPAQUE);
//            Graphics2D mg = compat.createGraphics();
//            mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//            mg.drawImage(src, 0, 0, gp.worldWidth, gp.worldHeight, null);
//            mg.dispose();
//            mapImage = compat;

            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_PATH));

            // CHỈNH Ở ĐÂY: Thu nhỏ kích thước map xuống (ví dụ chia 2.5)
            this.scale = 1.0 / 2.5;
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
        gp.player.bullets.clear();

        this.obstacles = ObstacleManager.loadObstacles("/maps/ktx_obstacles.txt", this.scale);
        spawnEnemies();
    }
//    private void initObstacles() {
//        obstacles.clear(); // Xóa sạch danh sách cũ
//
//        // Đường dẫn đến file chứa tọa độ (đặt trong thư mục resource của bạn)
//        String filePath = "/maps/ktx_obstacles.txt";
//
//        try {
//            // Đọc file dưới dạng Stream từ thư mục resource (giống cách bạn đọc ảnh ktx.png)
//            InputStream is = getClass().getResourceAsStream(filePath);
//            if (is == null) {
//                System.out.println("Không tìm thấy file tọa độ vật cản: " + filePath);
//                return;
//            }
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            String line;
//
//            // Đọc từng dòng cho đến khi hết file
//            while ((line = br.readLine()) != null) {
//                // Bỏ qua dòng trống hoặc dòng comment bắt đầu bằng dấu # (nếu có)
//                line = line.trim();
//                if (line.isEmpty() || line.startsWith("#")) {
//                    continue;
//                }
//
//                // Tách các con số bằng dấu phẩy
//                String[] data = line.split(" ");
//                if (data.length == 4) {
//                    int x = Integer.parseInt(data[0].trim());
//                    int y = Integer.parseInt(data[1].trim());
//                    int width = Integer.parseInt(data[2].trim());
//                    int height = Integer.parseInt(data[3].trim());
//
//                    // Thêm vật cản tàng hình (Color alpha = 0) vào danh sách
//                    // Nhân với scale (tức là nhân với 0.4) để thu nhỏ tọa độ lại cho khớp với map trong game
//                    int finalX = (int) (x * this.scale);
//                    int finalY = (int) (y * this.scale);
//                    int finalWidth = (int) (width * this.scale);
//                    int finalHeight = (int) (height * this.scale);
//
//                    obstacles.add(new Obstacle(finalX, finalY, finalWidth, finalHeight, new Color(0, 0, 0, 0)));
//                }
//            }
//            br.close();
//            System.out.println("Đã nạp thành công " + obstacles.size() + " vật cản từ file!");
//
//        } catch (Exception e) {
//            System.out.println("Lỗi khi đọc file tọa độ vật cản!");
//            e.printStackTrace();
//        }
//    }

    @Override
    public List<Obstacle> getObstacles() {
        return obstacles;
    }
    @Override
    public void exit() {
        gp.enemies.clear();
         obstacles.clear();
    }

    @Override
    public void update() {
        gp.player.update();
        for (int i = 0; i < gp.enemies.size(); i++) {
            gp.enemies.get(i).update();
        }
        //collisionChecker.checkAllCollisions();
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

        // 5. VẼ CÁC VẬT CẢN (Vẽ trước Player và Enemy để quái/người che lên vật cản nếu cần, hoặc ngược lại)
        for (Obstacle obs : obstacles) {
            obs.draw(g2, cameraX, cameraY);
        }

        gp.player.draw(g2, cameraX, cameraY);
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2, cameraX, cameraY);
        }

        g2.setColor(Color.WHITE);
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
