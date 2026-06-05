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
    private BufferedImage outgunImage;
    public List<Bullet> bullets = new ArrayList<>(); // Đạn chuyển về cho vũ khí quản lý
    
    private int flashTimer = 0; // Thời gian hiển thị hiệu ứng chớp lửa

    private int shootCooldown = 0;
    public int shootInterval = 3;
    private double aimAngle = 0;
    private boolean previousMousePressed = false;
    
    public boolean shotgunMode = false;
    public boolean automaticFire = false; // Thêm chế độ sấy

    private static final Color BULLET_COLOR = new Color(0, 80, 200);

    public Weapon(GamePanel gp, MouseHandler mouseH, Player player) {
        this.gp = gp;
        this.mouseH = mouseH;
        this.player = player;
        loadWeaponImage();
    }

    private void loadWeaponImage() {
        try {
            BufferedImage weaponTemp = ImageIO.read(getClass().getResourceAsStream("/weapon/ak47.jpg"));
            weaponImage = makeColorTransparent(weaponTemp, Color.WHITE, 40); // Loại bỏ viền trắng với dung sai 40 cho AK47
            BufferedImage outgunTemp = ImageIO.read(getClass().getResourceAsStream("/weapon/outgun.jpg"));
            outgunImage = makeColorTransparent(outgunTemp, Color.WHITE, 40); // Loại bỏ viền trắng với dung sai 40
        } catch (IOException ex) {
            throw new RuntimeException("Không tìm thấy ảnh súng hoặc hiệu ứng outgun!", ex);
        }
    }

    private BufferedImage makeColorTransparent(BufferedImage im, Color color, int tolerance) {
        BufferedImage dimg = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dimg.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(im, null, 0, 0);
        g.dispose();
        for(int i = 0; i < dimg.getHeight(); i++) {
            for(int j = 0; j < dimg.getWidth(); j++) {
                int px = dimg.getRGB(j, i);
                int r = (px >> 16) & 0xFF;
                int g_ = (px >> 8) & 0xFF;
                int b = px & 0xFF;
                if (Math.abs(r - color.getRed()) <= tolerance &&
                    Math.abs(g_ - color.getGreen()) <= tolerance &&
                    Math.abs(b - color.getBlue()) <= tolerance) {
                    dimg.setRGB(j, i, 0x00FFFFFF); // Giữ nguyên RGB nhưng set Alpha = 0
                }
            }
        }
        return dimg;
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

        if (automaticFire) {
            if (justPressed || (currentMousePressed && shootCooldown >= shootInterval)) {
                shoot();
                shootCooldown = 0;
            }
        } else {
            if (justPressed) {
                shoot();
                shootCooldown = 0;
            }
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
        
        // Cập nhật hiệu ứng chớp
        if (flashTimer > 0) {
            flashTimer--;
        }
    }

    private void shoot() {
        Bullet bullet1 = new Bullet(player.worldX + 40, player.worldY + 40, aimAngle);
        bullets.add(bullet1);
        
        flashTimer = 5; // Hiển thị chớp lửa trong 5 frames
        
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
            if (flashTimer > 0 && outgunImage != null) {
                g2.drawImage(outgunImage, weaponImage.getWidth() - pivotX, -(weaponImage.getHeight() - pivotY) - 10, 30, 30, null);
            }
        } else {
            g2.drawImage(weaponImage, -pivotX, -pivotY, null);
            if (flashTimer > 0 && outgunImage != null) {
                g2.drawImage(outgunImage, weaponImage.getWidth() - pivotX, -pivotY - 10, 30, 30, null);
            }
        }
        g2.setTransform(original);
    }

    public void clearBullets() {
        bullets.clear();
    }
}