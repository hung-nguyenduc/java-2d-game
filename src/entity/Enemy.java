package entity;

import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import collision.Obstacle;

// Lớp đại diện cho enemy trong game
public class Enemy extends Entity {
    GamePanel gp;
    Player player;
    public BufferedImage imageLeft, imageRight, currentImage;
    private int shootCooldown = 0;
    private final int shootInterval = 60; // Shoot every 60 frames (1 second at 60 FPS)
    public double minDistance = 50; // Khoảng cách tối thiểu dừng lại
    public boolean canDodge = true;
    public int damage = 1;
    public int tick = (int)(Math.random() * 1000); // Dùng cho thuật toán Zig-zag
    public int strafeDir = (Math.random() < 0.5) ? 1 : -1; // Hướng đi ngang lượn lờ quanh người chơi
    
    // Thuộc tính AI cho loại 3 (Taunter)
    public int lapsCompleted = 0;
    public double previousAngle = 0;
    public double totalAngleTraversed = 0;
    public Obstacle targetObstacle = null;
    public int fleeCooldown = 0;

    private int enemyType; // 0, 1, 2, 3 for different enemy types

    // Constructor: Khởi tạo Enemy với vị trí ban đầu
    public Enemy(GamePanel gp, Player player, int startX, int startY, int enemyType) {
        this.gp = gp;
        this.player = player;
        this.enemyType = enemyType;
        worldX = startX;
        worldY = startY;
        speed = 5.0; // Tốc độ mặc định, có thể ghi đè
        aimAngle = 0;
        health = maxHealth; // Đặt máu ban đầu

        getEnemyImage();
    }
    public String enemyDirection;
    public String[] enemyNames = {"gt1", "gt3", "ds", "ds", "gt3"}; // Thêm phần tử cho loại 4
    //public String enemySource = "/enemy/" + enemyNames[enemyType] + "_" + enemyDirection + ".png";
    public String getEnemySource() {
        return "/enemy/" + enemyNames[enemyType] + "_" + enemyDirection + ".png";
    }
    public void getEnemyDirection(double dx) {
        if (dx >= 0) {
            enemyDirection = "right";
        }
        else {
            enemyDirection = "left";
        }
    }
    // Tải hình ảnh của Enemy dựa trên loại enemy
    public void getEnemyImage() {
//        try {
//            // Load 3 different enemy images from enemy folder
////            switch(enemyType) {
////                case 0:
////                    enemyImage = ImageIO.read(getClass().getResourceAsStream(enemySource));
////                    break;
////                case 1:
////                    enemyImage = ImageIO.read(getClass().getResourceAsStream(enemySource));
////                    break;
////                case 2:
////                    enemyImage = ImageIO.read(getClass().getResourceAsStream(enemySource));
////                    break;
////                default:
////                    enemyImage = ImageIO.read(getClass().getResourceAsStream(enemySource));
//            enemyImage = ImageIO.read(getClass().getResourceAsStream(getEnemySource()));
////            }
//        } catch (IOException e) {
//            System.out.println("LỖI: Không tìm thấy ảnh quái vật!");
//            e.printStackTrace();
//        }

        try {
            String base = "/enemy/" + enemyNames[enemyType];
            imageLeft = ImageIO.read(getClass().getResourceAsStream(base + "_left.png"));
            imageRight = ImageIO.read(getClass().getResourceAsStream(base + "_right.png"));

            // Set a default starting image
            currentImage = imageLeft;
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("LỖI: Không tìm thấy ảnh cho " + enemyNames[enemyType]);
            e.printStackTrace();
        }
    }

    // Cập nhật trạng thái của Enemy mỗi frame
    public void update() {
        // Calculate direction towards player
        double dx = player.worldX - worldX;

        double dy = player.worldY - worldY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        // Update direction string AND the current image
        if (dx >= 0) {
            enemyDirection = "right";
            currentImage = imageRight;
        } else {
            enemyDirection = "left";
            currentImage = imageLeft;
        }
//        if (dx >= 0) {
//            // enemy ben trai player -> quay phai
//            this.enemyDirection = "right";
//            System.out.println("right");
////            try {
////                enemyImage = ImageIO.read(getClass().getResourceAsStream(enemySource));
////            } catch (IOException e) {
////                System.out.println("Oops! Could not find or read the file.");
////                e.printStackTrace();
////            }
//        }
//        else {
//            this.enemyDirection = "left";
//            System.out.println("left");
////
//        }
        // Khởi tạo vx, vy bằng 0 mỗi frame
        vx = 0;
        vy = 0;

        // Tính toán hướng đi thông minh theo từng loại quái
        if (enemyType == 4) { // Bắn tỉa nhút nhát (Chạy lượn lờ thả diều)
            tick++;
            double randomX = Math.cos(tick * 0.05 + this.hashCode()) * 1.5;
            double randomY = Math.sin(tick * 0.05 + this.hashCode()) * 1.5;
            
            if (distance < 400) { // Khi người chơi đến gần, vừa chạy lùi vừa đi ngang
                double fleeX = -(dx / distance);
                double fleeY = -(dy / distance);
                double tangentX = -(dy / distance) * strafeDir;
                double tangentY = (dx / distance) * strafeDir;
                
                vx = (fleeX * 1.2 + tangentX * 0.5 + randomX * 0.2) * speed;
                vy = (fleeY * 1.2 + tangentY * 0.5 + randomY * 0.2) * speed;
            } else { // Xa an toàn thì lượn lờ ngẫu nhiên
                vx = randomX * (speed * 0.5);
                vy = randomY * (speed * 0.5);
            }
        } else if (distance > minDistance) { // Tiến lại gần
            switch (enemyType) {
                case 0: { // Predictive Chaser: Đoán trước hướng đi của Player
                    double predictX = player.worldX + player.vx * 30; // Dự đoán trước 30 frame
                    double predictY = player.worldY + player.vy * 30;
                    double pdx = predictX - worldX;
                    double pdy = predictY - worldY;
                    double pDist = Math.sqrt(pdx * pdx + pdy * pdy);
                    if (pDist > 0) {
                        vx = (pdx / pDist) * speed;
                        vy = (pdy / pDist) * speed;
                    }
                    break;
                }

                case 1: { // Flanker: Đi vòng sang hai bên sườn
                    double nx = dx / distance;
                    double ny = dy / distance;
                    // Lực tiếp tuyến (vuông góc với hướng tới player) để tạo đường vòng
                    double tangentX = -ny;
                    double tangentY = nx;
                    // Tản ra hai hướng trái/phải dựa vào chẵn lẻ để bọc lót
                    if (this.hashCode() % 2 == 0) {
                        tangentX = -tangentX;
                        tangentY = -tangentY;
                    }
                    // Trộn hướng đi thẳng và tiếp tuyến (hơi lệch ra ngoài)
                    double flankX = nx + tangentX * 1.5;
                    double flankY = ny + tangentY * 1.5;
                    double fDist = Math.sqrt(flankX * flankX + flankY * flankY);
                    vx = (flankX / fDist) * speed;
                    vy = (flankY / fDist) * speed;
                    break;
                }

                case 2: { // Erratic/Zig-zag: Lảo đảo lắt léo
                    tick++;
                    double zx = dx / distance;
                    double zy = dy / distance;
                    // Lực vuông góc dao động theo hình sin
                    double perpX = -zy;
                    double perpY = zx;
                    double wave = Math.sin(tick * 0.1) * 2.0; // Tần số 0.1, biên độ lớn (2.0) để lắc mạnh
                    
                    double finalX = zx + perpX * wave;
                    double finalY = zy + perpY * wave;
                    double finalDist = Math.sqrt(finalX * finalX + finalY * finalY);
                    vx = (finalX / finalDist) * speed;
                    vy = (finalY / finalDist) * speed;
                    break;
                }

                case 3: { // Taunter: Chạy trốn và chạy vòng quanh thùng
                    double fleeX = -(dx / distance);
                    double fleeY = -(dy / distance);
                    
                    if (fleeCooldown > 0) {
                        fleeCooldown--;
                        vx = fleeX * speed * 1.2;
                        vy = fleeY * speed * 1.2;
                    } else {
                        if (targetObstacle == null && gp.getCurrentState() != null && gp.getCurrentState().getObstacles() != null) {
                            double minDist = Double.MAX_VALUE;
                            for (Obstacle obs : gp.getCurrentState().getObstacles()) {
                                if (obs.type == 1 || obs.type == 2) {
                                    double cx = obs.worldX + obs.width / 2.0;
                                    double cy = obs.worldY + obs.height / 2.0;
                                    double dToObs = Math.sqrt(Math.pow(cx - worldX, 2) + Math.pow(cy - worldY, 2));
                                    if (dToObs < 400 && dToObs < minDist) {
                                        minDist = dToObs;
                                        targetObstacle = obs;
                                        totalAngleTraversed = 0;
                                        lapsCompleted = 0;
                                        previousAngle = Math.atan2(worldY - cy, worldX - cx);
                                    }
                                }
                            }
                        }

                        if (targetObstacle != null) {
                            double cx = targetObstacle.worldX + targetObstacle.width / 2.0;
                            double cy = targetObstacle.worldY + targetObstacle.height / 2.0;
                            double ox = cx - worldX;
                            double oy = cy - worldY;
                            double distToObs = Math.sqrt(ox * ox + oy * oy);
                            
                            double currentAngle = Math.atan2(-oy, -ox);
                            double deltaAngle = currentAngle - previousAngle;
                            while (deltaAngle <= -Math.PI) deltaAngle += 2 * Math.PI;
                            while (deltaAngle > Math.PI) deltaAngle -= 2 * Math.PI;
                            
                            totalAngleTraversed += Math.abs(deltaAngle);
                            previousAngle = currentAngle;
                            
                            lapsCompleted = (int)(totalAngleTraversed / (2 * Math.PI));
                            
                            if (lapsCompleted >= 3) {
                                targetObstacle = null;
                                fleeCooldown = 180; // Bỏ chạy ra ngoài 3 giây trước khi tìm thùng mới
                                vx = fleeX * speed * 1.2;
                                vy = fleeY * speed * 1.2;
                            } else {
                                double orbitRadius = 100;
                                double moveX = 0, moveY = 0;
                                if (distToObs > orbitRadius + 10) {
                                    moveX = (ox / distToObs);
                                    moveY = (oy / distToObs);
                                } else if (distToObs < orbitRadius - 10) {
                                    moveX = -(ox / distToObs);
                                    moveY = -(oy / distToObs);
                                }
                                double tangentX = -oy / distToObs;
                                double tangentY = ox / distToObs;
                                
                                double fX = moveX * 0.5 + tangentX * 1.5;
                                double fY = moveY * 0.5 + tangentY * 1.5;
                                double fD = Math.sqrt(fX * fX + fY * fY);
                                vx = (fX / fD) * speed * 1.5;
                                vy = (fY / fD) * speed * 1.5;
                            }
                        } else {
                            vx = fleeX * speed;
                            vy = fleeY * speed;
                        }
                    }
                    break;
                }
                    
                default: {
                    vx = (dx / distance) * speed;
                    vy = (dy / distance) * speed;
                    break;
                }
            }
        } else { // Khi đạt tới minDistance -> chuyển sang trạng thái lượn lờ (Strafing)
            double nx = dx / distance;
            double ny = dy / distance;
            
            // Lực đi ngang (vuông góc với hướng tới player)
            double tangentX = -ny * strafeDir;
            double tangentY = nx * strafeDir;
            
            // Mặc định lượn ngang quanh người chơi
            double moveX = tangentX * (speed * 0.7);
            double moveY = tangentY * (speed * 0.7);

            // Nếu người chơi lao tới quá gần, kết hợp vừa đi ngang vừa lùi lại
            if (distance < minDistance - 20 && distance > 0) {
                double backSpeed = (enemyType == 3) ? speed * 1.2 : speed * 0.6;
                moveX += -nx * backSpeed;
                moveY += -ny * backSpeed;
            }
            vx = moveX;
            vy = moveY;
        }

        // Logic né đạn
        if (canDodge && player.currentWeapon != null) {
            for (Bullet b : player.currentWeapon.bullets) {
                double bdx = worldX - b.worldX;
                double bdy = worldY - b.worldY;
                double bDist = Math.sqrt(bdx * bdx + bdy * bdy);

                if (bDist < 150) { // Nếu đạn bay đến gần (< 150px)
                    // Tính vector vuông góc với hướng bay của đạn
                    double px = -b.vy;
                    double py = b.vx;

                    // Chọn hướng vuông góc giúp quái di chuyển ra xa quỹ đạo đạn
                    if (bdx * px + bdy * py < 0) {
                        px = -px;
                        py = -py;
                    }

                    // Chuẩn hóa vector né
                    double pLen = Math.sqrt(px * px + py * py);
                    if (pLen > 0) {
                        px /= pLen;
                        py /= pLen;
                        double dodgeSpeed = speed * 1.5; // Tăng tốc độ khi né đạn
                        vx = px * dodgeSpeed;
                        vy = py * dodgeSpeed;
                        break; // Chỉ xử lý né 1 viên đạn trong cùng 1 frame
                    }
                }
            }
        }

        // Separation logic (tránh quái đè lên nhau)
        double repX = 0;
        double repY = 0;
        for (Enemy other : gp.enemies) {
            if (other != this) {
                double odx = worldX - other.worldX;
                double ody = worldY - other.worldY;
                double odist = Math.sqrt(odx * odx + ody * ody);
                if (odist > 0 && odist < 100) { // Bán kính đẩy nhau tăng lên 100px
                    repX += (odx / odist) * (100 - odist) * 0.15; // Lực đẩy mạnh hơn
                    repY += (ody / odist) * (100 - odist) * 0.15;
                }
            }
        }
        vx += repX;
        vy += repY;

        // Calculate aim angle towards player
        aimAngle = Math.toDegrees(Math.atan2(dy, dx));

        // Apply movement (worldX/worldY là double → tránh mất precision khi normalize)
        worldX += vx;
        worldY += vy;

        // Giới hạn enemy trong phạm vi map
        worldX = Math.max(0, Math.min(worldX, gp.worldWidth - 80));
        worldY = Math.max(0, Math.min(worldY, gp.worldHeight - 80));

        // Update bullets
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if (bullets.get(i).isOutOfRange()) {
                bullets.remove(i);
                i--;
            }
        }

        // Periodic shooting
        shootCooldown++;
        if (shootCooldown >= shootInterval) {
            shoot();
            shootCooldown = 0;
        }
    }

    // Bắn đạn về phía Player
    public void shoot() {
        // Create bullet towards player
        Bullet bullet = new Bullet(worldX + 40, worldY + 40, aimAngle);
        bullets.add(bullet);
    }

    // Vẽ Enemy theo camera đã clamp (tránh enemy "trượt" khi player tới rìa map)
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        if (screenX > -80 && screenX < gp.screenWidth + 80 && screenY > -80 && screenY < gp.screenHeight + 80) {
            g2.drawImage(currentImage, screenX, screenY, 100, 100, null);
            drawHealthBar(g2, screenX, screenY - 10, 80, 10);
        }

        // Vẽ đạn ngoài khối culling: đạn đã ra khỏi enemy nhưng có thể vẫn trong screen
        for (Bullet bullet : bullets) {
            bullet.draw(g2, cameraX, cameraY);
        }
    }

    // Vẽ thanh máu
    private void drawHealthBar(Graphics2D g2, int x, int y, int width, int height) {
        // Background (red)
        g2.setColor(Color.RED);
        g2.fillRect(x, y, width, height);
        // Foreground (green)
        g2.setColor(Color.GREEN);
        int healthWidth = (int)((double)health / maxHealth * width);
        g2.fillRect(x, y, healthWidth, height);
        // Border
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }
}
