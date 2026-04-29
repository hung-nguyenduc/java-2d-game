package main;

import entity.Player; // Nhớ import package entity

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage; // Import lớp để xử lý ảnh
import java.io.IOException;

public class GamePanel extends JPanel implements Runnable {

    // -- CẤU HÌNH MÀN HÌNH (Giữ nguyên như cũ) --
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // -- THÊM VÀO 3 ÔNG TƯỚNG NÀY --
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    Player player = new Player(this, keyH); // Truyền Panel và Bàn phím cho Player

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLUE);
        this.setDoubleBuffered(true);

        // HAI DÒNG NÀY CỰC KỲ QUAN TRỌNG ĐỂ NHẬN PHÍM
        this.addKeyListener(keyH);
        this.setFocusable(true); // Để GamePanel tập trung nhận input từ bàn phím
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Game Loop 60 FPS chuẩn
        double drawInterval = 1000000000.0 / 60; // 1 giây chia cho 60 FPS
        double nextDrawTime = System.nanoTime() + drawInterval;

        while(gameThread != null) {
            update();
            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;

                if(remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);

                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        // Gọi hàm update của nhân vật
        player.update();
    }
    // Khai báo biến
//    BufferedImage mapImage;

    // Trong hàm khởi tạo (Constructor) load ảnh

    Image mapImage = new ImageIcon(getClass().getResource("/maps/c1.png")).getImage();

    public final int worldWidth = 2000;
    public final int worldHeight = 2000;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Tọa độ của Map so với màn hình
        // Nếu Player đứng ở World(1000, 1000), Map phải dịch trái 1000px
        int mapScreenX = -player.worldX + (screenWidth / 2);
        int mapScreenY = -player.worldY + (screenHeight / 2);

        g2.drawImage(mapImage, mapScreenX, mapScreenY, worldWidth, worldHeight, null);

        // Gọi hàm draw của nhân vật
        player.draw(g2);

        g2.dispose();
    }
}