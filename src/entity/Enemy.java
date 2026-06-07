package entity;

import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

// Lớp đại diện cho enemy trong game - Lính Mỹ chiến tranh Việt Nam
// Hỗ trợ 6 hướng di chuyển: up, down, left, right, down_left, up_right
public class Enemy extends Entity {
    GamePanel gp;
    Player player;

    // --- 24 BIẾN HÌNH ẢNH CHO 6 HƯỚNG x 4 FRAME ---
    // 4 hướng cơ bản
    public BufferedImage up1, up2, up3, up4;
    public BufferedImage down1, down2, down3, down4;
    public BufferedImage left1, left2, left3, left4;
    public BufferedImage right1, right2, right3, right4;
    // 2 hướng chéo
    public BufferedImage downLeft1, downLeft2, downLeft3, downLeft4;
    public BufferedImage upRight1, upRight2, upRight3, upRight4;

    public BufferedImage currentImage;

    // --- BIẾN ANIMATION CHUYỂN FRAME ---
    public int spriteCounter = 0;
    public int spriteNum = 1; // 1-4 frame animation

    private int shootCooldown = 0;
    private final int shootInterval = 60; // Bắn mỗi 60 frames
    public double minDistance = 150; // Khoảng cách tối thiểu dừng lại giữ khoảng cách với Player
    public boolean canDodge = true;
    public int damage = 1;
    public int strafeDir = (Math.random() < 0.5) ? 1 : -1; // Hướng đi ngang lượn lờ

    public String enemyDirection;

    // Constructor: Khởi tạo Enemy với vị trí ban đầu
    public Enemy(GamePanel gp, Player player, int startX, int startY) {
        this.gp = gp;
        this.player = player;
        worldX = startX;
        worldY = startY;
        speed = 5.0; // Tốc độ di chuyển
        aimAngle = 0;
        health = maxHealth; // Đặt máu ban đầu
        enemyDirection = "down"; // Hướng mặc định ban đầu

        getEnemyImage();
    }

    // Nạp ảnh cho lính Mỹ - 6 hướng x 4 frame = 24 ảnh
    public void getEnemyImage() {
        try {
            String base = "/enemy/soldier/";

            // Hướng xuống (TRƯỚC)
            down1 = ImageIO.read(getClass().getResourceAsStream(base + "down_1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream(base + "down_2.png"));
            down3 = ImageIO.read(getClass().getResourceAsStream(base + "down_3.png"));
            down4 = ImageIO.read(getClass().getResourceAsStream(base + "down_4.png"));

            // Hướng lên (SAU)
            up1 = ImageIO.read(getClass().getResourceAsStream(base + "up_1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream(base + "up_2.png"));
            up3 = ImageIO.read(getClass().getResourceAsStream(base + "up_3.png"));
            up4 = ImageIO.read(getClass().getResourceAsStream(base + "up_4.png"));

            // Hướng trái
            left1 = ImageIO.read(getClass().getResourceAsStream(base + "left_1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream(base + "left_2.png"));
            left3 = ImageIO.read(getClass().getResourceAsStream(base + "left_3.png"));
            left4 = ImageIO.read(getClass().getResourceAsStream(base + "left_4.png"));

            // Hướng phải
            right1 = ImageIO.read(getClass().getResourceAsStream(base + "right_1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream(base + "right_2.png"));
            right3 = ImageIO.read(getClass().getResourceAsStream(base + "right_3.png"));
            right4 = ImageIO.read(getClass().getResourceAsStream(base + "right_4.png"));

            // Hướng chéo xuống-trái (3/4 TRƯỚC)
            downLeft1 = ImageIO.read(getClass().getResourceAsStream(base + "down_left_1.png"));
            downLeft2 = ImageIO.read(getClass().getResourceAsStream(base + "down_left_2.png"));
            downLeft3 = ImageIO.read(getClass().getResourceAsStream(base + "down_left_3.png"));
            downLeft4 = ImageIO.read(getClass().getResourceAsStream(base + "down_left_4.png"));

            // Hướng chéo lên-phải (3/4 SAU)
            upRight1 = ImageIO.read(getClass().getResourceAsStream(base + "up_right_1.png"));
            upRight2 = ImageIO.read(getClass().getResourceAsStream(base + "up_right_2.png"));
            upRight3 = ImageIO.read(getClass().getResourceAsStream(base + "up_right_3.png"));
            upRight4 = ImageIO.read(getClass().getResourceAsStream(base + "up_right_4.png"));

            // Đặt hình mặc định khi spawn
            currentImage = down1;
        } catch (Exception e) {
            System.out.println("LỖI: Không thể nạp ảnh cho Enemy Lính Mỹ!");
            e.printStackTrace();
        }
    }

    // Cập nhật trạng thái của Enemy mỗi frame
    public void update() {
        double dx = player.worldX - worldX;
        double dy = player.worldY - worldY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Khởi tạo vx, vy bằng 0 mỗi frame
        vx = 0;
        vy = 0;

        // --- AI CƠ BẢN CHO QUÁI ---
        if (distance > minDistance) {
            // Nếu ở xa thì tiến thẳng về phía Player
            vx = (dx / distance) * speed;
            vy = (dy / distance) * speed;
        } else {
            // Nếu tới gần thì lượn lờ đi ngang giữ vị trí
            double nx = dx / distance;
            double ny = dy / distance;

            double tangentX = -ny * strafeDir;
            double tangentY = nx * strafeDir;

            vx = tangentX * (speed * 0.7);
            vy = tangentY * (speed * 0.7);
        }

        // --- LOGIC NÉ ĐẠN CỦA PLAYER ---
        if (canDodge && player.currentWeapon != null) {
            for (Bullet b : player.currentWeapon.bullets) {
                double bdx = worldX - b.worldX;
                double bdy = worldY - b.worldY;
                double bDist = Math.sqrt(bdx * bdx + bdy * bdy);

                if (bDist < 150) {
                    double px = -b.vy;
                    double py = b.vx;

                    if (bdx * px + bdy * py < 0) {
                        px = -px;
                        py = -py;
                    }

                    double pLen = Math.sqrt(px * px + py * py);
                    if (pLen > 0) {
                        px /= pLen;
                        py /= pLen;
                        double dodgeSpeed = speed * 1.5;
                        vx = px * dodgeSpeed;
                        vy = py * dodgeSpeed;
                        break;
                    }
                }
            }
        }

        // --- LOGIC ĐẨY NHAU (Tránh quái đứng đè lên nhau) ---
        double repX = 0;
        double repY = 0;
        for (Enemy other : gp.enemies) {
            if (other != this) {
                double odx = worldX - other.worldX;
                double ody = worldY - other.worldY;
                double odist = Math.sqrt(odx * odx + ody * ody);
                if (odist > 0 && odist < 100) {
                    repX += (odx / odist) * (100 - odist) * 0.15;
                    repY += (ody / odist) * (100 - odist) * 0.15;
                }
            }
        }
        vx += repX;
        vy += repY;

        // --- XÁC ĐỊNH HƯỚNG QUAY MẶT THEO VẬN TỐC (6 HƯỚNG) ---
        updateDirection();

        // --- XỬ LÝ ANIMATION HOẠT ẢNH DI CHUYỂN (4 FRAME) ---
        boolean isMoving = (Math.abs(vx) > 0.1 || Math.abs(vy) > 0.1);
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 8) { // Đổi ảnh mỗi 8 frame để mượt hơn
                spriteNum++;
                if (spriteNum > 4) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        } else {
            // Đứng yên thì đưa về frame 1 (dáng đứng)
            spriteNum = 1;
            spriteCounter = 0;
        }

        // Gán ảnh dựa theo hướng và frame hiện tại
        updateCurrentImage();

        // --- CẬP NHẬT TỌA ĐỘ VÀ GIỚI HẠN BẢN ĐỒ ---
        aimAngle = Math.toDegrees(Math.atan2(dy, dx));
        worldX += vx;
        worldY += vy;

        worldX = Math.max(0, Math.min(worldX, gp.worldWidth - 80));
        worldY = Math.max(0, Math.min(worldY, gp.worldHeight - 80));

        // --- CẬP NHẬT ĐẠN ---
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if (bullets.get(i).isOutOfRange()) {
                bullets.remove(i);
                i--;
            }
        }

        // --- BẮN THEO CHU KỲ ---
        shootCooldown++;
        if (shootCooldown >= shootInterval) {
            shoot();
            shootCooldown = 0;
        }
    }

    // Xác định hướng 6 chiều dựa trên góc di chuyển
    private void updateDirection() {
        // Tính góc di chuyển (radian → độ)
        double moveAngle = Math.toDegrees(Math.atan2(vy, vx));
        // Chuẩn hóa về 0-360
        if (moveAngle < 0) moveAngle += 360;

        // Nếu vận tốc gần bằng 0 thì giữ nguyên hướng cũ
        if (Math.abs(vx) < 0.1 && Math.abs(vy) < 0.1) return;

        // Chia 360° thành 8 vùng, mỗi vùng 45°
        // Nhưng chỉ có 6 hướng sprite, nên ta ánh xạ 8 vùng → 6 hướng
        //
        //   315-360/0-45   → right      (0°)
        //   45-90          → down_right  → dùng down (vì không có sprite down_right)
        //   90-135         → down        (90°)
        //   135-180        → down_left   (135°)
        //   180-225        → left        (180°)
        //   225-270        → up_left     → dùng up (vì không có sprite up_left)
        //   270-315        → up          (270°)
        //   Riêng up_right → dùng cho vùng 315-360

        if (moveAngle >= 337.5 || moveAngle < 22.5) {
            enemyDirection = "right";
        } else if (moveAngle >= 22.5 && moveAngle < 67.5) {
            // Chéo xuống-phải → dùng down (flip down_left hoặc dùng down)
            enemyDirection = "down";
        } else if (moveAngle >= 67.5 && moveAngle < 112.5) {
            enemyDirection = "down";
        } else if (moveAngle >= 112.5 && moveAngle < 157.5) {
            enemyDirection = "down_left";
        } else if (moveAngle >= 157.5 && moveAngle < 202.5) {
            enemyDirection = "left";
        } else if (moveAngle >= 202.5 && moveAngle < 247.5) {
            // Chéo lên-trái → dùng up
            enemyDirection = "up";
        } else if (moveAngle >= 247.5 && moveAngle < 292.5) {
            enemyDirection = "up";
        } else if (moveAngle >= 292.5 && moveAngle < 337.5) {
            enemyDirection = "up_right";
        }
    }

    // Chọn ảnh hiện tại dựa theo hướng và frame animation
    private void updateCurrentImage() {
        switch (enemyDirection) {
            case "up":
                currentImage = getFrame(up1, up2, up3, up4);
                break;
            case "down":
                currentImage = getFrame(down1, down2, down3, down4);
                break;
            case "left":
                currentImage = getFrame(left1, left2, left3, left4);
                break;
            case "right":
                currentImage = getFrame(right1, right2, right3, right4);
                break;
            case "down_left":
                currentImage = getFrame(downLeft1, downLeft2, downLeft3, downLeft4);
                break;
            case "up_right":
                currentImage = getFrame(upRight1, upRight2, upRight3, upRight4);
                break;
            default:
                currentImage = down1;
                break;
        }
    }

    // Trả về frame tương ứng với spriteNum hiện tại
    private BufferedImage getFrame(BufferedImage f1, BufferedImage f2, BufferedImage f3, BufferedImage f4) {
        switch (spriteNum) {
            case 1: return f1;
            case 2: return f2;
            case 3: return f3;
            case 4: return f4;
            default: return f1;
        }
    }

    public void shoot() {
        Bullet bullet = new Bullet(worldX + 40, worldY + 40, aimAngle);
        bullets.add(bullet);
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        // Chỉ vẽ khi nằm trong khung hình camera
        if (screenX > -80 && screenX < gp.screenWidth + 80 && screenY > -80 && screenY < gp.screenHeight + 80) {
            g2.drawImage(currentImage, screenX, screenY, 100, 100, null);
            drawHealthBar(g2, screenX, screenY - 10, 80, 10);
        }

        for (Bullet bullet : bullets) {
            bullet.draw(g2, cameraX, cameraY);
        }
    }

    private void drawHealthBar(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(Color.RED);
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.GREEN);
        int healthWidth = (int)((double)health / maxHealth * width);
        g2.fillRect(x, y, healthWidth, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }
}