package state;

import collision.Obstacle;
import collision.ObstacleManager;
import entity.Enemy;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenWorldState extends GameState {
    
    public enum SubState { PLAYING, INVENTORY, MAP_APP, LOADING }
    private SubState subState = SubState.PLAYING;
    
    private BufferedImage inventoryImage, phoneIcon, mapAppImage;
    private int loadingCounter = 0;
    private double targetX = 0, targetY = 0;
    private boolean showPhoneMenu = false;
    private int phoneX, phoneY, phoneW = 64, phoneH = 64; // Bounds for phone icon
    private Rectangle menuUseRect = new Rectangle();
    private boolean iKeyProcessed = false;
    private static class Region {
        String name;
        String imagePath;
        String obstaclePath;
        int offsetX, offsetY;
        double scale;
        Image image;
        int width, height;
        List<Obstacle> obstacles = new ArrayList<>();
        boolean enemiesSpawned = false;
        
        public Region(String name, String imagePath, String obstaclePath, int offsetX, int offsetY, double scale) {
            this.name = name;
            this.imagePath = imagePath;
            this.obstaclePath = obstaclePath;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.scale = scale;
        }
    }

    private List<Region> regions = new ArrayList<>();
    private List<Obstacle> allObstacles = new ArrayList<>();
    private Font HUD_FONT = new Font("Arial", Font.BOLD, 20);

    // Dịch chuyển (Portals)
    private Rectangle hubToC1, hubToKtx, hubToClassroom;
    private Rectangle c1ToHub, ktxToHub, classroomToHub;
    private Rectangle ktxToMyRoom;

    public OpenWorldState(GamePanel gp) {
        super(gp);
        
        // HUB: test.png ở (0, 0)
        regions.add(new Region("HUB", "/maps/test.png", null, 0, 0, 1.0 / 2.5));
        // C1: destroyed-c1.png ở (4000, 0)
        regions.add(new Region("C1", "/maps/destroyed-c1.png", null, 4000, 0, 1.0 / 2.5));
        // KTX: ktx.png ở (8000, 0)
        regions.add(new Region("KTX", "/maps/ktx.png", "/maps/ktx_obstacles.txt", 8000, 0, 1.0 / 2.5));
        // Classroom: classroom.png ở (0, 4000)
        regions.add(new Region("Classroom", "/maps/classroom.png", "/maps/classroom_obstacles.txt", 0, 4000, 1.0 / 2.5));
    }

    @Override
    public void enter() {
        int maxWorldW = 0;
        int maxWorldH = 0;
        allObstacles.clear();

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();

        for (Region r : regions) {
            try {
                InputStream is = getClass().getResourceAsStream(r.imagePath);
                if (is != null) {
                    BufferedImage src = ImageIO.read(is);
                    r.width = (int) (src.getWidth() * r.scale);
                    r.height = (int) (src.getHeight() * r.scale);

                    BufferedImage compat = gc.createCompatibleImage(r.width, r.height, Transparency.OPAQUE);
                    Graphics2D mg = compat.createGraphics();
                    mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    mg.drawImage(src, 0, 0, r.width, r.height, null);
                    mg.dispose();
                    r.image = compat;

                    // Tính lại world size
                    if (r.offsetX + r.width > maxWorldW) maxWorldW = r.offsetX + r.width;
                    if (r.offsetY + r.height > maxWorldH) maxWorldH = r.offsetY + r.height;
                }

                // Đọc obstacles
                if (r.obstaclePath != null) {
                    List<Obstacle> obsList = ObstacleManager.loadObstacles(r.obstaclePath, r.scale);
                    for (Obstacle o : obsList) {
                        // Cộng thêm offset
                        o.worldX += r.offsetX;
                        o.worldY += r.offsetY;
                        r.obstacles.add(o);
                        allObstacles.add(o);
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi nạp ảnh bản đồ tại: " + r.imagePath);
                e.printStackTrace();
            }
        }

        gp.worldWidth = maxWorldW;
        gp.worldHeight = maxWorldH;

        // Reset hệ thống
        gp.killCount = 0;
        gp.player.health = gp.player.maxHealth;
        gp.player.bullets.clear();
        gp.enemies.clear();

        // Trang bị vũ khí Shotgun
        gp.player.equipWeapon(new entity.Shotgun(gp, gp.mouseH, gp.player));

        // Spawn người chơi ở giữa HUB
        Region hub = regions.get(0);
        gp.player.worldX = hub.offsetX + hub.width / 2.0;
        gp.player.worldY = hub.offsetY + hub.height / 2.0;

        // Vùng dịch chuyển từ HUB đi các nơi
        hubToC1 = new Rectangle(hub.offsetX + 300, hub.offsetY + 100, 80, 80);
        hubToKtx = new Rectangle(hub.offsetX + 500, hub.offsetY + 100, 80, 80);
        hubToClassroom = new Rectangle(hub.offsetX + 700, hub.offsetY + 100, 80, 80);
        
        // Vùng dịch chuyển từ các nơi về HUB
        Region c1 = regions.get(1);
        c1ToHub = new Rectangle(c1.offsetX + c1.width / 2, c1.offsetY + c1.height - 150, 80, 80);
        
        Region ktx = regions.get(2);
        ktxToHub = new Rectangle(ktx.offsetX + ktx.width / 2, ktx.offsetY + ktx.height - 150, 80, 80);
        ktxToMyRoom = new Rectangle(ktx.offsetX + ktx.width / 2 + 150, ktx.offsetY + ktx.height - 150, 80, 80);
        
        Region classroom = regions.get(3);
        classroomToHub = new Rectangle(classroom.offsetX + classroom.width / 2, classroom.offsetY + classroom.height - 150, 80, 80);
        
        
        try {
            inventoryImage = ImageIO.read(getClass().getResourceAsStream("/inventory/Inventory.png"));
            // Bỏ dùng InventoryIcon.png vì nó là cái balo, ta sẽ tự vẽ điện thoại
            phoneIcon = null; 
            mapAppImage = ImageIO.read(getClass().getResourceAsStream("/maps/ref/Area_selection_map.png"));
        } catch (Exception e) { e.printStackTrace(); }
        
        checkRegionSpawn();
    }
    
    private void checkRegionSpawn() {
        for (Region r : regions) {
            // Kiểm tra xem player có đang ở trong vùng này không
            if (!r.enemiesSpawned && 
                gp.player.worldX >= r.offsetX && gp.player.worldX <= r.offsetX + r.width &&
                gp.player.worldY >= r.offsetY && gp.player.worldY <= r.offsetY + r.height) {
                
                spawnEnemiesForRegion(r);
                r.enemiesSpawned = true;
            }
        }
    }
    
    private void spawnEnemiesForRegion(Region r) {
        if (r.name.equals("HUB")) {
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 200, r.offsetY + 400, "enemy1"));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 800, r.offsetY + 600, "enemy2"));
        } else if (r.name.equals("C1")) {
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 300,  r.offsetY + 400, "gt1"));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 700,  r.offsetY + 300, "gt3"));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 1100, r.offsetY + 600, "gt1"));
        } else if (r.name.equals("KTX")) {
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 200,  r.offsetY + 800, "enemy1"));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 600,  r.offsetY + 200, "enemy2"));
        } else if (r.name.equals("Classroom")) {
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 500,  r.offsetY + 500, "ds"));
            gp.enemies.add(new Enemy(gp, gp.player, r.offsetX + 900,  r.offsetY + 400, "ds"));
        }
    }

    @Override
    public List<Obstacle> getObstacles() {
        return allObstacles;
    }

    @Override
    public void update() {
        // Toggle Inventory
        if (gp.keyH.iPressed && !iKeyProcessed) {
            iKeyProcessed = true;
            if (subState == SubState.PLAYING) subState = SubState.INVENTORY;
            else if (subState == SubState.INVENTORY) {
                subState = SubState.PLAYING;
                showPhoneMenu = false;
            }
        }
        if (!gp.keyH.iPressed) iKeyProcessed = false;

        // Xử lý Loading
        if (subState == SubState.LOADING) {
            loadingCounter++;
            if (loadingCounter > 60) {
                gp.player.worldX = targetX;
                gp.player.worldY = targetY;
                subState = SubState.PLAYING;
                loadingCounter = 0;
            }
            return;
        }

        // Freeze game nếu không phải PLAYING
        if (subState != SubState.PLAYING) {
            return;
        }

        gp.player.update();
        
        checkRegionSpawn();

        for (int i = 0; i < gp.enemies.size(); i++) {
            gp.enemies.get(i).update();
        }

        gp.checkCollisions();

        if (gp.player.health <= 0) {
            gp.setState(new GameOverState(gp));
            return;
        }

        // Logic dịch chuyển giữa các toà nhà (Teleport)
        Rectangle playerRect = new Rectangle((int)gp.player.worldX, (int)gp.player.worldY, gp.tileSize, gp.tileSize);
        
        if (playerRect.intersects(hubToC1)) {
            teleportPlayerToRegion(regions.get(1));
        } else if (playerRect.intersects(hubToKtx)) {
            teleportPlayerToRegion(regions.get(2));
        } else if (playerRect.intersects(hubToClassroom)) {
            teleportPlayerToRegion(regions.get(3));
        } else if (playerRect.intersects(c1ToHub) || playerRect.intersects(ktxToHub) || playerRect.intersects(classroomToHub)) {
            teleportPlayerToRegion(regions.get(0));
        } else if (playerRect.intersects(ktxToMyRoom)) {
            gp.setState(new MyRoomState(gp));
            return;
        }
    }
    
    private void teleportPlayerToRegion(Region r) {
        gp.player.worldX = r.offsetX + r.width / 2.0;
        gp.player.worldY = r.offsetY + r.height / 2.0 + 100;
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);

        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Màu nền chung cho cả không gian World
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Vẽ tất cả các vùng map có trong camera
        for (Region r : regions) {
            if (r.image != null) {
                // Kiểm tra xem region này có nằm trong màn hình camera không để tối ưu
                if (r.offsetX + r.width > cameraX && r.offsetX < cameraX + gp.screenWidth &&
                    r.offsetY + r.height > cameraY && r.offsetY < cameraY + gp.screenHeight) {
                    
                    int drawX = r.offsetX - cameraX;
                    int drawY = r.offsetY - cameraY;
                    g2.drawImage(r.image, drawX, drawY, r.width, r.height, null);
                }
            }
        }

        // Vẽ cổng dịch chuyển
        g2.setColor(new Color(255, 255, 0, 100)); // Màu vàng cho cổng đi
        g2.fillRect(hubToC1.x - cameraX, hubToC1.y - cameraY, hubToC1.width, hubToC1.height);
        g2.fillRect(hubToKtx.x - cameraX, hubToKtx.y - cameraY, hubToKtx.width, hubToKtx.height);
        g2.fillRect(hubToClassroom.x - cameraX, hubToClassroom.y - cameraY, hubToClassroom.width, hubToClassroom.height);
        
        g2.setColor(new Color(0, 255, 255, 100)); // Màu cyan cho cổng về
        g2.fillRect(c1ToHub.x - cameraX, c1ToHub.y - cameraY, c1ToHub.width, c1ToHub.height);
        g2.fillRect(ktxToHub.x - cameraX, ktxToHub.y - cameraY, ktxToHub.width, ktxToHub.height);
        g2.fillRect(classroomToHub.x - cameraX, classroomToHub.y - cameraY, classroomToHub.width, classroomToHub.height);
        
        // Cổng vào Phòng KTX (màu hồng)
        g2.setColor(new Color(255, 100, 200, 100));
        g2.fillRect(ktxToMyRoom.x - cameraX, ktxToMyRoom.y - cameraY, ktxToMyRoom.width, ktxToMyRoom.height);
        
        g2.setColor(Color.WHITE);
        g2.drawString("To C1", hubToC1.x - cameraX, hubToC1.y - cameraY - 10);
        g2.drawString("To KTX", hubToKtx.x - cameraX, hubToKtx.y - cameraY - 10);
        g2.drawString("To Class", hubToClassroom.x - cameraX, hubToClassroom.y - cameraY - 10);
        
        g2.drawString("Back HUB", c1ToHub.x - cameraX, c1ToHub.y - cameraY - 10);
        g2.drawString("Back HUB", ktxToHub.x - cameraX, ktxToHub.y - cameraY - 10);
        g2.drawString("Back HUB", classroomToHub.x - cameraX, classroomToHub.y - cameraY - 10);
        g2.drawString("Phòng KTX", ktxToMyRoom.x - cameraX, ktxToMyRoom.y - cameraY - 10);

        // Vẽ Enemy
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2, cameraX, cameraY);
        }

        // Vẽ Player
        gp.player.draw(g2, cameraX, cameraY);

        // Vẽ HUD
        g2.setColor(Color.WHITE);
        g2.setFont(HUD_FONT);
        g2.drawString("OPEN WORLD MODE", 10, 30);
        g2.drawString("KILLS: " + gp.killCount, 10, 60);
        
        // Vẽ UI đè lên trên cùng
        if (subState == SubState.INVENTORY) {
            drawInventory(g2);
        } else if (subState == SubState.MAP_APP) {
            drawMapApp(g2);
        } else if (subState == SubState.LOADING) {
            drawLoadingScreen(g2);
        }
    }
    
    private void drawInventory(Graphics2D g2) {
        if (inventoryImage == null) return;
        
        // Draw inventory centered on screen
        int invW = 350;
        int invH = 350;
        int invX = gp.screenWidth / 2 - invW / 2;
        int invY = gp.screenHeight / 2 - invH / 2;
        g2.drawImage(inventoryImage, invX, invY, invW, invH, null);
        
        // Based on the actual Inventory.png layout:
        // The 3x3 grid starts at ~22% from left, ~18% from top of the image
        // Each cell is ~23% of image width/height
        int gridStartX = invX + (int)(invW * 0.22);
        int gridStartY = invY + (int)(invH * 0.18);
        int cellW = (int)(invW * 0.23);
        int cellH = (int)(invH * 0.23);
        
        // Phone icon (map) in the first cell (row 0, col 0) - always present
        phoneW = 30;
        phoneH = 30;
        phoneX = gridStartX + (cellW - phoneW) / 2;
        phoneY = gridStartY + (cellH - phoneH) / 2;
        
        // Draw a small smartphone icon
        g2.setColor(new Color(40, 40, 40)); // phone body
        g2.fillRoundRect(phoneX, phoneY, phoneW, phoneH, 6, 6);
        g2.setColor(new Color(100, 200, 255)); // screen (light blue)
        g2.fillRect(phoneX + 3, phoneY + 3, phoneW - 6, phoneH - 10);
        g2.setColor(Color.WHITE); // home button
        g2.fillOval(phoneX + phoneW / 2 - 3, phoneY + phoneH - 7, 6, 6);
        
        // Draw collected inventory items in remaining cells (slot 1-8)
        int itemSize = 28;
        for (int i = 0; i < gp.player.inventory.size() && i < 8; i++) {
            int slotIndex = i + 1; // slot 0 is the phone
            int row = slotIndex / 3;
            int col = slotIndex % 3;
            int ix = gridStartX + col * cellW + (cellW - itemSize) / 2;
            int iy = gridStartY + row * cellH + (cellH - itemSize) / 2;
            
            entity.Player.InventoryItem item = gp.player.inventory.get(i);
            if (item.icon != null) {
                g2.drawImage(item.icon, ix, iy, itemSize, itemSize, null);
            } else {
                g2.setColor(Color.ORANGE);
                g2.fillRect(ix, iy, itemSize, itemSize);
            }
        }
    }
    
    private void drawMapApp(Graphics2D g2) {
        if (mapAppImage == null) return;
        g2.drawImage(mapAppImage, 0, 0, gp.screenWidth, gp.screenHeight, null);
    }
    
    private void drawLoadingScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        
        // Vẽ text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String text = "LOADING...";
        FontMetrics fm = g2.getFontMetrics();
        int textX = (gp.screenWidth - fm.stringWidth(text)) / 2;
        int textY = gp.screenHeight / 2 + 100;
        g2.drawString(text, textX, textY);
        
        // Vẽ nhân vật đứng giữa
        Image loadingImg = gp.player.down1;
        if (loadingCounter % 20 < 10) loadingImg = gp.player.down2; // Chớp chân
        g2.drawImage(loadingImg, gp.screenWidth / 2 - 45, gp.screenHeight / 2 - 45, 90, 90, null);
    }

    @Override
    public void exit() {
        gp.enemies.clear();
        allObstacles.clear();
        for (Region r : regions) {
            if (r.image != null) {
                r.image.flush();
            }
        }
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        
        if (subState == SubState.INVENTORY) {
            // Click outside phone menu -> hide menu
            // Click on phone menu Use -> trigger map
            // Click on phone -> show menu
            
            // Click on the phone directly opens the map app
            if (mx >= phoneX && mx <= phoneX + phoneW && my >= phoneY && my <= phoneY + phoneH) {
                subState = SubState.MAP_APP;
                showPhoneMenu = false;
                return;
            }
        } else if (subState == SubState.MAP_APP) {
            // BACK and HOME buttons
            if (my < 100) {
                if (mx < 150) {
                    subState = SubState.INVENTORY;
                } else if (mx > gp.screenWidth - 150) {
                    subState = SubState.PLAYING;
                }
            }
            
            // Hitboxes (ước lượng dựa trên ảnh Area_selection_map.png vẽ full màn 768x576)
            // Section 1: C1 (Góc trên trái)
            Rectangle sec1 = new Rectangle(100, 100, 300, 200);
            // Section 2: KTX (Góc trên phải/giữa phải)
            Rectangle sec2 = new Rectangle(450, 150, 250, 200);
            // Section 3: Classroom (Góc dưới trái)
            Rectangle sec3 = new Rectangle(100, 350, 300, 200);
            
            if (sec1.contains(mx, my)) {
                triggerLoadingForRegion(1); // C1
            } else if (sec2.contains(mx, my)) {
                triggerLoadingForRegion(2); // KTX
            } else if (sec3.contains(mx, my)) {
                triggerLoadingForRegion(3); // Classroom
            }
        }
    }

    private void triggerLoadingForRegion(int regionIndex) {
        Region r = regions.get(regionIndex);
        targetX = r.offsetX + r.width / 2.0;
        targetY = r.offsetY + r.height / 2.0 + 100;
        subState = SubState.LOADING;
        loadingCounter = 0;
    }
}
