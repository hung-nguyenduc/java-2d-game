package entity;

import main.GamePanel;
import main.MouseHandler;

public class Shotgun extends Weapon {

    public Shotgun(GamePanel gp, MouseHandler mouseH, Player player) {
        super(gp, mouseH, player);
    }

    @Override
    protected void shoot() {
        // Tạo 3 viên đạn tỏa ra theo hình nón
        double[] angles = { aimAngle - 15, aimAngle, aimAngle + 15 };
        
        for (double angle : angles) {
            Bullet bullet = new Bullet(player.worldX + 40, player.worldY + 40, angle);
            bullets.add(bullet);
        }
    }
}
