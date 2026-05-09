package main;

import java.awt.Color;

public class GameConfig {
    // Kích thước màn hình
    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;
    
    // Kích thước player
    public static final int PLAYER_SIZE = 48;
    public static final int PLAYER_SPEED = 5;
    
    // Kích thước quái vật
    public static final int ENEMY_SIZE = 48;
    public static final int ENEMY_SPEED = 2;
    
    // Kích thước đạn
    public static final int BULLET_SIZE = 10;
    public static final int BULLET_SPEED = 10;
    
    // Kích thước map
    public static final int WORLD_WIDTH = 2000;
    public static final int WORLD_HEIGHT = 2000;
    
    // FPS và debug
    public static final int FPS = 60;
    public static final boolean DEBUG_MODE = true;
    
    // Màu sắc (dùng khi không có ảnh)
    public static final Color PLAYER_COLOR = new Color(0, 100, 255);
    public static final Color ENEMY_COLOR = new Color(200, 0, 0);
    public static final Color BULLET_PLAYER_COLOR = new Color(255, 255, 0);
    public static final Color BULLET_ENEMY_COLOR = new Color(255, 0, 0);
    public static final Color WALL_COLOR = new Color(80, 60, 40);
    public static final Color OBJECT_COLOR = new Color(139, 69, 19);
    public static final Color BACKGROUND_COLOR = new Color(20, 20, 30);
}