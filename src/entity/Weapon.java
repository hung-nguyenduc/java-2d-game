package entity;

import main.GamePanel;
import main.MouseHandler;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class Weapon {
    private GamePanel gp;
    private MouseHandler mouseH;
    private Player player;

    private BufferedImage weaponImage;
    public List<Bullet> bullets = new ArrayList<>(); // Đạn chuyển về cho vũ khí quản lý

    private int shootCooldown = 0;
    public int shootInterval = 3;
    private double aimAngle = 0;
    private boolean previousMousePressed = false;
    
    public boolean shotgunMode = false;

    private static final Color BULLET_COLOR = new Color(0, 80, 200);

    public Weapon(GamePanel gp, MouseHandler mouseH, Player player) {
        this.gp = gp;
        this.mouseH = mouseH;
        this.player = player;
        loadWeaponImage();
    }

    private void loadWeaponImage() {
        try {
            weaponImage = ImageIO.read(getClass().getResourceAsStream("/weapon/shotgun.png"));
        } catch (IOException ex) {
            throw new RuntimeException("Không tìm thấy ảnh súng!", ex);
        }
    }

    public void update() {
        // 1. Tính toán góc ngắm bắn theo chuột (đã chuyển từ Player sang đây)
        int rawCamX = (int) (player.worldX - gp.screenWidth / 2.0);
        int rawCamY = (int) (player.worldY - gp.screenHeight / 2.0);
        int[] cam = gp.clampCameraPosition(rawCamX, rawCamY);

        int playerCenterScreenX = (int) (player.worldX - cam[0]) + 40;
        int playerCenterScreenY = (int) (player.worldY - cam[1]) + 40;

        double dx = mouseH.mouseX - playerCenterScreenX;
        double dy = mouseH.mouseY - playerCenterScreenY;
        aimAngle = Math.toDegrees(Math.atan2(dy, dx));

        // 2. Xử lý logic bắn súng
        shootCooldown++;
        boolean currentMousePressed = mouseH.leftMousePressed;
        boolean justPressed = currentMousePressed && !previousMousePressed;

        if (justPressed || (currentMousePressed && shootCooldown >= shootInterval)) {
            shoot();
            shootCooldown = 0;
        }
        previousMousePressed = currentMousePressed;

        // 3. Cập nhật và xóa đạn ngoài tầm
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if (bullets.get(i).isOutOfRange()) {
                bullets.remove(i);
                i--;
            }
        }
    }

    private void shoot() {
        Bullet bullet1 = new Bullet(player.worldX + 40, player.worldY + 40, aimAngle);
        bullets.add(bullet1);
        
        if (shotgunMode) {
            // Kỹ năng Shotgun: Bắn thêm 2 viên đạn tỏa ra 2 hướng
            Bullet bullet2 = new Bullet(player.worldX + 40, player.worldY + 40, aimAngle - 15); // Lệch lên 15 độ
            Bullet bullet3 = new Bullet(player.worldX + 40, player.worldY + 40, aimAngle + 15); // Lệch xuống 15 độ
            bullets.add(bullet2);
            bullets.add(bullet3);
        }

        // Thêm nhạc khi bắn (update sau)
        // gp.sound.playSE("shoot");
    }

    public void draw(Graphics2D g2, int screenX, int screenY, int cameraX, int cameraY) {
        // 1. Vẽ toàn bộ đạn đang bay
        for (Bullet bullet : bullets) {
            bullet.draw(g2, cameraX, cameraY);
        }

        // 2. Vẽ súng
        int centerX = screenX + 43;
        int centerY = screenY + 45;
        int pivotX = 10;
        int pivotY = 20;

        AffineTransform original = g2.getTransform();
        g2.translate(centerX, centerY);
        g2.rotate(Math.toRadians(aimAngle));

        if (aimAngle > 90 || aimAngle < -90) {
            g2.scale(1, -1);
            g2.drawImage(weaponImage, -pivotX, -(weaponImage.getHeight() - pivotY), null);
        } else {
            g2.drawImage(weaponImage, -pivotX, -pivotY, null);
        }
        g2.setTransform(original);
    }

    public void clearBullets() {
        bullets.clear();
    }
}