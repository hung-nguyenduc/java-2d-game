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
        // Hoàn trả lại chính xác quãng đường đã di chuyển trong frame này
        // và reset vận tốc để không bị đẩy lùi gấp bội nếu chạm nhiều vật cản/quái cùng lúc
        gp.player.worldX -= gp.player.vx;
        gp.player.worldY -= gp.player.vy;
        gp.player.vx = 0;
        gp.player.vy = 0;
    }

    public void checkAllCollisions() {
        // Trước khi cập nhật tọa độ mới cho vòng lặp vật cản, lưu lại vị trí an toàn trước đó của Enemy
        // Cách này giúp xử lý va chạm tường của Enemy mượt mà hơn rất nhiều
        int[] oldEnemyX = new int[gp.enemies.size()];
        int[] oldEnemyY = new int[gp.enemies.size()];
        for (int i = 0; i < gp.enemies.size(); i++) {
            oldEnemyX[i] = (int)gp.enemies.get(i).worldX;
            oldEnemyY[i] = (int)gp.enemies.get(i).worldY;
        }

        // 1. Kiểm tra va chạm thực thể (Player <=> Enemy và Đạn trúng các bên)
        for (Enemy enemy : gp.enemies) {
            // Player chạm Enemy (Hitbox kích thước 80x80)
            if (gp.player.worldX + 80 > enemy.worldX && gp.player.worldX < enemy.worldX + 80 &&
                    gp.player.worldY + 80 > enemy.worldY && gp.player.worldY < enemy.worldY + 80) {
                gp.player.health -= enemy.damage;
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

        // 2. Lấy danh sách vật cản từ State hiện tại và xử lý va chạm tường biên cứng
        if (gp.getCurrentState() == null) return;
        List<Obstacle> currentObstacles = gp.getCurrentState().getObstacles();
        if (currentObstacles == null) return;

        for (Obstacle obs : currentObstacles) {
            Rectangle obsBounds = obs.getBounds();

            // Va chạm Player - Vật cản
            Rectangle playerBounds = new Rectangle((int)gp.player.worldX + 20, (int)gp.player.worldY + 15, 50, 50);
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

            // Va chạm Enemy - Vật cản (Chặn đứng không cho xuyên tường)
            for (int k = 0; k < gp.enemies.size(); k++) {
                Enemy enemy = gp.enemies.get(k);
                Rectangle enemyBounds = new Rectangle((int)enemy.worldX, (int)enemy.worldY, 80, 80);

                if (enemyBounds.intersects(obsBounds)) {
                    // Thay vì nảy +-2px gây lỗi giật hình, ép quái lùi về tọa độ trước khi chạm tường
                    enemy.worldX = oldEnemyX[k];
                    enemy.worldY = oldEnemyY[k];
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