package state;

import entity.Enemy;
import main.GamePanel;
import collision.Obstacle;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class BaseMapState extends GameState {

    // =========================================================================
    // KHU VỰC CẤU HÌNH (BẠN CHỈ CẦN CHỈNH SỬA CÁC THAM SỐ TẠI ĐÂY)
    // =========================================================================
    private static final String MAP_PATH = "/maps/ktx.png";         // Đường dẫn ảnh Map
    private static final String OBSTACLES_PATH = "/maps/ktx_obstacles.txt"; // Đường dẫn file vật cản
    private static final double MAP_SCALE = 1.0 / 2.5;              // Tỷ lệ thu nhỏ map (Ví dụ: 1.0 / 2.5)

    // Cấu hình tính năng gameplay của Map
    private static final boolean ENABLE_COMBAT = true;              // true: Bật đánh quái, bắn đạn | false: Map hòa bình (không quái, không bắn)
    private static final int KILLS_REQUIRED = 10;                    // Số lượng quái cần giết để qua màn
    private static final GameState NEXT_LEVEL_STATE = null;         // State của màn tiếp theo (Ví dụ: new Level2State(gp))
    private static final int CURRENT_LEVEL_NUMBER = 1;              // Số thứ tự màn chơi hiển thị trên HUD
    // =========================================================================

    private Image mapImage;
    private List<Obstacle> obstacles;
    private static final Font HUD_FONT = new Font("Arial", Font.BOLD, 20);

    public BaseMapState(GamePanel gp) {
        super(gp);
        this.obstacles = new ArrayList<>();
    }

    @Override
    public void enter() {
        // 1. Tải và Scale bản đồ tự động
        try {
            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_PATH));
            int mapWidth = (int) (src.getWidth() * MAP_SCALE);
            int mapHeight = (int) (src.getHeight() * MAP_SCALE);

            // Cập nhật kích thước thế giới vào GamePanel
            gp.worldWidth = mapWidth;
            gp.worldHeight = mapHeight;

            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compat = gc.createCompatibleImage(mapWidth, mapHeight, Transparency.OPAQUE);
            Graphics2D mg = compat.createGraphics();

            mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            mg.drawImage(src, 0, 0, mapWidth, mapHeight, null);
            mg.dispose();
            mapImage = compat;
        } catch (IOException e) {
            System.err.println("Lỗi: Không thể tải hình ảnh map tại đường dẫn " + MAP_PATH);
            e.printStackTrace();
        }

        // 2. Khởi tạo hệ thống trạng thái nhân vật
        gp.killCount = 0;
        gp.player.setDefaultValues();
        gp.player.spawnAtCenter();
        gp.player.health = gp.player.maxHealth;
        gp.player.bullets.clear();

        // 3. Tải vật cản từ file cấu hình
        initObstacles();

        // 4. Xử lý Spawn quái dựa trên cấu hình ENABLE_COMBAT
        gp.enemies.clear();
        if (ENABLE_COMBAT) {
            spawnEnemies();
        }
    }

    private void initObstacles() {
        obstacles.clear();

        try {
            InputStream is = getClass().getResourceAsStream(OBSTACLES_PATH);
            if (is == null) {
                System.out.println("Cảnh báo: Không tìm thấy file tọa độ vật cản tại: " + OBSTACLES_PATH);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] data = line.split(" ");
                if (data.length == 4) {
                    int x = Integer.parseInt(data[0].trim());
                    int y = Integer.parseInt(data[1].trim());
                    int width = Integer.parseInt(data[2].trim());
                    int height = Integer.parseInt(data[3].trim());

                    // Tự động scale tọa độ của vật cản dựa trên MAP_SCALE cấu hình ở trên
                    int finalX = (int) (x * MAP_SCALE);
                    int finalY = (int) (y * MAP_SCALE);
                    int finalWidth = (int) (width * MAP_SCALE);
                    int finalHeight = (int) (height * MAP_SCALE);

                    // Khởi tạo vật cản tàng hình hoàn toàn
                    obstacles.add(new Obstacle(finalX, finalY, finalWidth, finalHeight, new Color(0, 0, 0, 0)));
                }
            }
            br.close();
            System.out.println("Đã nạp thành công " + obstacles.size() + " vật cản từ file!");

        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi đọc file tọa độ vật cản!");
            e.printStackTrace();
        }
    }

    private void spawnEnemies() {
        // Bạn có thể tùy chỉnh tọa độ sinh quái tại đây (đây là hệ tọa độ gốc, tự động ăn theo kích thước chuẩn)
        gp.enemies.add(new Enemy(gp, gp.player, 300, 300, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 800, 500, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1200, 700, 2));
    }

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
        // Luôn luôn cập nhật di chuyển của Player
        gp.player.update();

        // Khóa tính năng bắn đạn nếu chế độ Combat bị Tắt
        if (!ENABLE_COMBAT) {
            gp.player.bullets.clear();
        }

        // Cập nhật logic của Enemy nếu có bật tính năng Combat
        if (ENABLE_COMBAT) {
            for (int i = 0; i < gp.enemies.size(); i++) {
                gp.enemies.get(i).update();
            }

            // Chạy hàm check va chạm tổng của GamePanel
            //gp.checkCollisions();

            // Kiểm tra trạng thái Game Over
            if (gp.player.health <= 0) {
                gp.setState(new GameOverState(gp));
                return;
            }

            // Kiểm tra điều kiện chuyển màn chơi
            if (gp.killCount >= KILLS_REQUIRED && NEXT_LEVEL_STATE != null) {
                gp.setState(new LevelCompleteState(gp, CURRENT_LEVEL_NUMBER, NEXT_LEVEL_STATE));
            }
        }
        //.checkCollisions();
    }

    @Override
    public void draw(Graphics2D g2) {
        // Tính toán Camera
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // 1. Vẽ hình nền map
        if (mapImage != null) {
            g2.drawImage(mapImage,
                    0, 0, gp.screenWidth, gp.screenHeight,
                    cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight,
                    null);
        }

        // 2. Vẽ các khối vật cản (Đã thiết lập hàm draw rỗng để tàng hình)
        for (Obstacle obs : obstacles) {
            obs.draw(g2, cameraX, cameraY);
        }

        // 3. Vẽ Enemy (Chỉ vẽ khi bật Combat)
        if (ENABLE_COMBAT) {
            for (Enemy enemy : gp.enemies) {
                enemy.draw(g2, cameraX, cameraY);
            }
        }

        // 4. Vẽ Player
        gp.player.draw(g2, cameraX, cameraY);

        // 5. Giao diện HUD hiển thị thông tin màn chơi
        g2.setColor(Color.WHITE);
        g2.setFont(HUD_FONT);
        if (ENABLE_COMBAT) {
            g2.drawString("Man " + CURRENT_LEVEL_NUMBER + " - Giet quai: " + gp.killCount + " / " + KILLS_REQUIRED, 10, 30);
        } else {
            g2.drawString("Man " + CURRENT_LEVEL_NUMBER + " - Khu vuc an toan", 10, 30);
        }
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}

    public static void main(String[] args) {
        // Tạo một cửa sổ Windows chuẩn Swing
        javax.swing.JFrame window = new javax.swing.JFrame();
        window.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Test Template Map - Zombie Game 2026");

        // Khởi tạo GamePanel - Trái tim của game
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // Tự động co dãn cửa sổ khít với kích thước GamePanel

        window.setLocationRelativeTo(null); // Đưa cửa sổ ra chính giữa màn hình máy tính
        window.setVisible(true);

        // ÉP GAME CHẠY THẲNG VÀO MAP TEMPLATE NÀY (Bỏ qua MenuState mặc định)
        BaseMapState testState = new BaseMapState(gamePanel);
        gamePanel.setState(testState);

        // Kích hoạt vòng lặp Game 60 FPS
        gamePanel.startGameThread();
    }
}