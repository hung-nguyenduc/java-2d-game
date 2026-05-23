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

    public void checkAllCollisions() {
        // 1. Kiểm tra va chạm giữa Player và Enemy, Đạn với các bên
        for (Enemy enemy : gp.enemies) {
            // Player chạm Enemy
            if (gp.player.worldX + 80 > enemy.worldX && gp.player.worldX < enemy.worldX + 80 &&
                    gp.player.worldY + 80 > enemy.worldY && gp.player.worldY < enemy.worldY + 80) {
                gp.player.health -= 1;
            }

            // Đạn của Player trúng Enemy
            for (int i = 0; i < gp.player.bullets.size(); i++) {
                Bullet bullet = gp.player.bullets.get(i);
                if (bullet.worldX + 10 > enemy.worldX && bullet.worldX < enemy.worldX + 80 &&
                        bullet.worldY + 10 > enemy.worldY && bullet.worldY < enemy.worldY + 80) {
                    enemy.health -= 35;
                    gp.player.bullets.remove(i);
                    i--;
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
            Rectangle playerBounds = new Rectangle((int)gp.player.worldX, (int)gp.player.worldY, 80, 80);
            if (playerBounds.intersects(obsBounds)) {
                if (gp.keyH.upPressed)    gp.player.worldY += gp.player.speed;
                if (gp.keyH.downPressed)  gp.player.worldY -= gp.player.speed;
                if (gp.keyH.leftPressed)  gp.player.worldX += gp.player.speed;
                if (gp.keyH.rightPressed) gp.player.worldX -= gp.player.speed;
            }

            // Va chạm Đạn Player - Vật cản
            for (int i = 0; i < gp.player.bullets.size(); i++) {
                Bullet bullet = gp.player.bullets.get(i);
                Rectangle bulletBounds = new Rectangle((int) bullet.worldX, (int)bullet.worldY, 10, 10);
                if (bulletBounds.intersects(obsBounds)) {
                    gp.player.bullets.remove(i);
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