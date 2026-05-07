import entity.Player;
import main.GamePanel;
import main.KeyHandler;
import main.MouseHandler;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Test {
    public static void main(String[] args) {
        System.out.println("Testing Java 2D Game features...");

        // Test 1: Tạo GamePanel
        try {
            GamePanel gp = new GamePanel();
            System.out.println("GamePanel created successfully. Screen size: " + gp.screenWidth + "x" + gp.screenHeight);
        } catch (Exception e) {
            System.out.println("Error creating GamePanel: " + e.getMessage());
        }

        // Test 2: Tạo KeyHandler và MouseHandler
        KeyHandler keyH = new KeyHandler();
        MouseHandler mouseH = new MouseHandler();
        System.out.println("KeyHandler and MouseHandler created.");

        // Test 3: Tạo Player (cần classpath cho res)
        try {
            GamePanel gp = new GamePanel();
            Player player = new Player(gp, keyH, mouseH);
            System.out.println("Player created. Health: " + player.health + ", Speed: " + player.speed);
        } catch (Exception e) {
            System.out.println("Error creating Player: " + e.getMessage());
        }

        // Test 4: Load image (test resource loading)
        try {
            BufferedImage img = ImageIO.read(Test.class.getResourceAsStream("/player/up1.png"));
            if (img != null) {
                System.out.println("Image loaded successfully. Size: " + img.getWidth() + "x" + img.getHeight());
            } else {
                System.out.println("Image not found.");
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
        }

        System.out.println("Test completed.");
    }
}