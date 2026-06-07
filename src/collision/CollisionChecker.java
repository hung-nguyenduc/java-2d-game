package collision;

import entity.Enemy;
import entity.Bullet;
import main.GamePanel;

import java.awt.Rectangle;
import java.util.List;

public class CollisionChecker {
    private GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void restorePos() {
        // Hoàn trả vị trí Player
        gp.player.worldX -= gp.player.vx;
        gp.player.worldY -= gp.player.vy;
        gp.player.vx = 0;
        gp.player.vy = 0;
    }

    public void checkAllCollisions() {
        // 1. Kiểm tra va chạm thực thể (Player <=> Enemy và Đạn)
        for (int i = 0; i < gp.enemies.size(); i++) {
            Enemy enemy = gp.enemies.get(i);

            // Player chạm Enemy
            if (gp.player.worldX + 80 > enemy.worldX && gp.player.worldX < enemy.worldX + 80 &&
                    gp.player.worldY + 80 > enemy.worldY && gp.player.worldY < enemy.worldY + 80) {
                gp.player.health -= enemy.damage;
                restorePos();
            }

            // Đạn của Player trúng Enemy
            if (gp.player.currentWeapon != null) {
                for (int j = 0; j < gp.player.currentWeapon.bullets.size(); j++) {
                    Bullet bullet = gp.player.currentWeapon.bullets.get(j);
                    if (bullet.worldX + 10 > enemy.worldX && bullet.worldX < enemy.worldX + 80 &&
                            bullet.worldY + 10 > enemy.worldY && bullet.worldY < enemy.worldY + 80) {
                        enemy.health -= 17;
                        gp.player.currentWeapon.bullets.remove(j);
                        j--;
                    }
                }
            }

            // Đạn của Enemy trúng Player
            for (int j = 0; j < enemy.bullets.size(); j++) {
                Bullet bullet = enemy.bullets.get(j);
                if (bullet.worldX + 10 > gp.player.worldX && bullet.worldX < gp.player.worldX + 80 &&
                        bullet.worldY + 10 > gp.player.worldY && bullet.worldY < gp.player.worldY + 80) {
                    gp.player.health -= 10;
                    enemy.bullets.remove(j);
                    j--;
                }
            }
        }

        // Xử lý xóa quái chết
        for (int i = 0; i < gp.enemies.size(); i++) {
            if (gp.enemies.get(i).health <= 0) {
                gp.enemies.remove(i);
                gp.killCount++;
                i--;
            }
        }

        // 2. Lấy danh sách vật cản xử lý cho Player và Đạn
        if (gp.getCurrentState() == null) return;
        List<Obstacle> currentObstacles = gp.getCurrentState().getObstacles();
        if (currentObstacles == null) return;

        for (Obstacle obs : currentObstacles) {
            Rectangle obsBounds = obs.getBounds();

            // Va chạm Player - Vật cản
            Rectangle playerBounds = new Rectangle((int) gp.player.worldX + 10, (int) gp.player.worldY + 20, 55, 65);
            if (playerBounds.intersects(obsBounds)) {
                restorePos();
            }

            // Va chạm Đạn Player - Vật cản
            if (gp.player.currentWeapon != null) {
                for (int i = 0; i < gp.player.currentWeapon.bullets.size(); i++) {
                    Bullet bullet = gp.player.currentWeapon.bullets.get(i);
                    Rectangle bulletBounds = new Rectangle((int) bullet.worldX, (int) bullet.worldY, 10, 10);
                    if (bulletBounds.intersects(obsBounds)) {
                        gp.player.currentWeapon.bullets.remove(i);
                        i--;
                    }
                }
            }

            // Va chạm Đạn Enemy - Vật cản
            for (Enemy enemy : gp.enemies) {
                for (int i = 0; i < enemy.bullets.size(); i++) {
                    Bullet bullet = enemy.bullets.get(i);
                    Rectangle bulletBounds = new Rectangle((int) bullet.worldX, (int) bullet.worldY, 10, 10);
                    if (bulletBounds.intersects(obsBounds)) {
                        enemy.bullets.remove(i);
                        i--;
                    }
                }
            }
        }
    }
}