package com.game;

import javax.swing.JFrame;

public class GameWindow {
    public GameWindow() {
        JFrame frame = new JFrame("My Game");

        GamePanel panel = new GamePanel();
        frame.add(panel);

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}