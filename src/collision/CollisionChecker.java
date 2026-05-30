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
        if (gp.keyH.upPressed)    gp.player.worldY += gp.player.speed + 20;
        if (gp.keyH.downPressed)  gp.player.worldY += gp.player.speed + 20;
        if (gp.keyH.leftPressed)  gp.player.worldX -= gp.player.speed;
        if (gp.keyH.rightPressed) gp.player.worldX -= gp.player.speed;
    }
    public void checkAllCollisions() {
        // 1. Kiểm tra va chạm giữa Player và Enemy, Đạn với các bên
        for (Enemy enemy : gp.enemies) {
            // Player chạm Enemy
            if (gp.player.worldX + 80 > enemy.worldX && gp.player.worldX < enemy.worldX + 80 &&
                    gp.player.worldY + 80 > enemy.worldY && gp.player.worldY < enemy.worldY + 80) {
                gp.player.health -= 1;
                restorePos();
            }

            // Đạn của Player trúng Enemy
            if (gp.player.currentWeapon != null) {
                for (int i = 0; i < gp.player.currentWeapon.bullets.size(); i++) {
                    Bullet bullet = gp.player.currentWeapon.bullets.get(i);
                    if (bullet.worldX + 10 > enemy.worldX && bullet.worldX < enemy.worldX + 80 &&
                            bullet.worldY + 10 > enemy.worldY && bullet.worldY < enemy.worldY + 80) {
                        enemy.health -= 35;
                        gp.player.currentWeapon.bullets.remove(i);
                        i--;
                    }
                }
            }
            
            // Kiếm Khí (SwordAura) trúng Enemy
            for (int i = 0; i < gp.player.swordAuras.size(); i++) {
                entity.SwordAura aura = gp.player.swordAuras.get(i);
                Rectangle auraBounds = aura.getBounds();
                Rectangle enemyBounds = new Rectangle((int)enemy.worldX, (int)enemy.worldY, 80, 80);
                if (auraBounds.intersects(enemyBounds)) {
                    enemy.health -= 5; // Sát thương mỗi frame khi xuyên qua
                }
            }
            
            // Bom nổ trúng Enemy
            for (entity.Bomb bomb : gp.player.bombs) {
                if (bomb.exploded) {
                    double dist = Math.hypot(bomb.worldX + 15 - (enemy.worldX + 40), bomb.worldY + 15 - (enemy.worldY + 40));
                    if (dist < bomb.explosionRadius / 2.0 + 40) {
                        enemy.health -= 15; // Sát thương mỗi frame trong vùng nổ
                    }
                }
            }

            // Đạn của Enemy trúng Player
            for (int i = 0; i < enemy.bullets.size(); i++) {
                Bullet bullet = enemy.bullets.get(i);
                if (bullet.worldX + 10 > gp.player.worldX && bullet.worldX < gp.player.worldX + 80 &&
                        bullet.worldY + 10 > gp.player.worldY && bullet.worldY < gp.player.worldY + 80) {
                    gp.player.health -= 10;
                    enemy.bullets.remove(i);
                    i--;
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

        // 2. Lấy danh sách vật cản từ State hiện tại và xử lý va chạm tường
        List<Obstacle> currentObstacles = gp.getCurrentState().getObstacles();

        for (Obstacle obs : currentObstacles) {
            Rectangle obsBounds = obs.getBounds();

            // Va chạm Player - Vật cản
            Rectangle playerBounds = new Rectangle((int)gp.player.worldX + 20, (int)gp.player.worldY + 15, 50, 50);;
            if (playerBounds.intersects(obsBounds)) {
                restorePos();
            }

            // Va chạm Đạn Player - Vật cản
            if (gp.player.currentWeapon != null) {
                for (int i = 0; i < gp.player.currentWeapon.bullets.size(); i++) {
                    Bullet bullet = gp.player.currentWeapon.bullets.get(i);
                    Rectangle bulletBounds = new Rectangle((int) bullet.worldX, (int)bullet.worldY, 10, 10);
                    if (bulletBounds.intersects(obsBounds)) {
                        gp.player.currentWeapon.bullets.remove(i);
                        i--;
                    }
                }
            }
            
            // Va chạm Kiếm Khí - Vật cản (xóa nếu đụng tường)
            for (int i = 0; i < gp.player.swordAuras.size(); i++) {
                entity.SwordAura aura = gp.player.swordAuras.get(i);
                if (aura.getBounds().intersects(obsBounds)) {
                    gp.player.swordAuras.remove(i);
                    i--;
                }
            }

            // Va chạm Enemy - Vật cản
            for (Enemy enemy : gp.enemies) {
                Rectangle enemyBounds = new Rectangle((int)enemy.worldX, (int)enemy.worldY, 80, 80);
                if (enemyBounds.intersects(obsBounds)) {
                    if (enemy.worldX < obs.worldX) enemy.worldX -= 2;
                    else enemy.worldX += 2;
                    if (enemy.worldY < obs.worldY) enemy.worldY -= 2;
                    else enemy.worldY += 2;
                }

                // Va chạm Đạn Enemy - Tường
                for (int i = 0; i < enemy.bullets.size(); i++) {
                    Bullet bullet = enemy.bullets.get(i);
                    Rectangle bulletBounds = new Rectangle((int)bullet.worldX, (int)bullet.worldY, 10, 10);
                    if (bulletBounds.intersects(obsBounds)) {
                        enemy.bullets.remove(i);
                        i--;
                    }
                }
            }
        }
    }
}