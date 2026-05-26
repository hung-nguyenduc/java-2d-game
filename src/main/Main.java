package main;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame("Sinh tồn ở HUST");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        // Khởi tạo GamePanel và add vào frame
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);

        frame.pack(); // Tự động co giãn JFrame cho vừa khít với kích thước của GamePanel

        frame.setLocationRelativeTo(null); // Hiển thị ở giữa màn hình
        frame.setVisible(true);


        // Khởi động vòng lặp game
        gamePanel.startGameThread();
    }
}