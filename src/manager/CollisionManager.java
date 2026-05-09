package manager;

import java.awt.Color;  // ← THÊM DÒNG NÀY
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class CollisionManager {
    private MapManager mapManager;
    private ObjectManager objectManager;
    
    public CollisionManager(MapManager mm, ObjectManager om) {
        this.mapManager = mm;
        this.objectManager = om;
    }
    
    public boolean checkWallCollision(Rectangle rect) {
        return mapManager.checkCollision(rect);
    }
    
    public boolean checkObjectCollision(Rectangle rect) {
        return objectManager.checkCollision(rect);
    }
    
    public boolean checkAnyCollision(Rectangle rect) {
        return checkWallCollision(rect) || checkObjectCollision(rect);
    }
}