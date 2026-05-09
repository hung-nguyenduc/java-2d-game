package manager;

import entity.GameObject;
import main.GameConfig;
import java.util.ArrayList;

import java.awt.Color;  // ← THÊM DÒNG NÀY
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class ObjectManager {
    private ArrayList<GameObject> objects;
    
    public ObjectManager() {
        objects = new ArrayList<>();
        initObjects();
    }
    
    private void initObjects() {
        // Thêm bàn ghế vào map
        objects.add(new GameObject("Desk", 300, 400, 60, 60, true));
        objects.add(new GameObject("Desk", 400, 400, 60, 60, true));
        objects.add(new GameObject("Desk", 500, 400, 60, 60, true));
        objects.add(new GameObject("Desk", 1000, 600, 60, 60, true));
        objects.add(new GameObject("Desk", 1100, 600, 60, 60, true));
        
        objects.add(new GameObject("Bookshelf", 800, 200, 50, 100, true));
        objects.add(new GameObject("Bookshelf", 1400, 700, 50, 100, true));
        
        objects.add(new GameObject("Chair", 320, 470, 35, 35, true));
        objects.add(new GameObject("Chair", 420, 470, 35, 35, true));
        objects.add(new GameObject("Chair", 520, 470, 35, 35, true));
        
        objects.add(new GameObject("Chair", 1020, 670, 35, 35, true));
        objects.add(new GameObject("Chair", 1120, 670, 35, 35, true));
    }
    
    public boolean checkCollision(Rectangle rect) {
        for (GameObject obj : objects) {
            if (obj.isSolid() && rect.intersects(obj.getHitbox())) {
                return true;
            }
        }
        return false;
    }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        for (GameObject obj : objects) {
            obj.draw(g2, cameraX, cameraY);
        }
    }
}