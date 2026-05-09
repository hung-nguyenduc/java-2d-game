package main;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Tạo cửa sổ game
        JFrame frame = new JFrame("2D Survival Shooter Game");
        GamePanel gamePanel = new GamePanel();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        
        // Khởi động game
        gamePanel.startGame();
        
        System.out.println("Game started! Controls:");
        System.out.println("  WASD - Move");
        System.out.println("  Mouse - Aim");
        System.out.println("  Left Click - Shoot");
        System.out.println("  R - Restart when game over");
    }
}