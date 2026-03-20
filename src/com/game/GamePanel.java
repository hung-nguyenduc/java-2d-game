package com.game;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;

public class GamePanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // set màu
        g.setColor(Color.RED);

        // vẽ hình vuông (x, y, width, height)
        g.fillRect(100, 100, 50, 50);
    }
}