package manager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

public class AssetManager {
    private static HashMap<String, BufferedImage> images = new HashMap<>();
    private static boolean hasLoadedImages = false;
    private static String baseImagePath;
    
    static {
        // Tìm đường dẫn tới thư mục res/image
        String userDir = System.getProperty("user.dir");
        baseImagePath = userDir + File.separator + "res" + File.separator + "image" + File.separator;
        System.out.println("Looking for images in: " + baseImagePath);
    }
    
    public static void loadAllImages() {
        try {
            // Background images
            loadImage("background/bg_grid.png", "bg_grid");
            
            // Player images
            loadImage("player/player_idle.png", "player_idle");
            loadImage("player/player_up.png", "player_up");
            loadImage("player/player_down.png", "player_down");
            loadImage("player/player_left.png", "player_left");
            loadImage("player/player_right.png", "player_right");
            
            // Enemy images
            loadImage("enemies/enemy_red.png", "enemy_red");
            
            // Bullet images
            loadImage("bullets/bullet_player.png", "bullet_player");
            loadImage("bullets/bullet_enemy.png", "bullet_enemy");
            
            // Object images
            loadImage("objects/desk.png", "desk");
            loadImage("objects/chair.png", "chair");
            loadImage("objects/bookshelf.png", "bookshelf");
            
            if (!images.isEmpty()) {
                hasLoadedImages = true;
                System.out.println("✓ Loaded " + images.size() + " images successfully!");
            } else {
                System.out.println("⚠ No images found. Game will use colored shapes instead.");
                hasLoadedImages = false;
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Error loading images: " + e.getMessage());
            hasLoadedImages = false;
        }
    }
    
    private static void loadImage(String relativePath, String key) {
        try {
            File imageFile = new File(baseImagePath + relativePath);
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                if (img != null) {
                    images.put(key, img);
                    System.out.println("  ✓ Loaded: " + key + " from " + imageFile.getAbsolutePath());
                }
            } else {
                System.out.println("  ✗ File not found: " + imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("  ✗ Error loading " + key + ": " + e.getMessage());
        }
    }
    
    public static BufferedImage getImage(String key) {
        return images.get(key);
    }
    
    public static boolean hasImages() {
        return hasLoadedImages && !images.isEmpty();
    }
}