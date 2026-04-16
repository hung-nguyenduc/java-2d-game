package main;

import javax.swing.JFrame;

// Lớp chính khởi động ứng dụng game
public class Main {
    // Phương thức main: Điểm vào của chương trình
    public static void main(String[] args) {

        JFrame frame = new JFrame("Sinh ton o HUST");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

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