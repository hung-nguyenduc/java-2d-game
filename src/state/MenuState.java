package state;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import main.GamePanel;

public class MenuState extends GameState {
    private Rectangle playButton;
    private Rectangle instructionsButton;
    private Rectangle infoButton;
    private BufferedImage background;

    // Hệ thống hạt (Particles) cho hiệu ứng lửa và khói
    private ArrayList<Particle> particles;
    private Random random;

    public MenuState(GamePanel gp) {
        super(gp);
        getBackgroundImage();

        // Căn giữa các nút theo kích thước màn hình
        int btnWidth = 220;
        int btnHeight = 60;
        int centerX = (gp.screenWidth - btnWidth) / 2;

        playButton = new Rectangle(centerX, 250, btnWidth, btnHeight);
        instructionsButton = new Rectangle(centerX, 330, btnWidth, btnHeight);
        infoButton = new Rectangle(centerX, 410, btnWidth, btnHeight);

        particles = new ArrayList<>();
        random = new Random();
    }

    @Override
    public void enter() {}

    @Override
    public void exit() {}

    @Override
    public void update() {
        // Tạo thêm hạt lửa/khói mới mỗi frame
        for (int i = 0; i < 5; i++) {
            particles.add(new Particle(
                    random.nextInt(gp.screenWidth), // Xuất hiện ngẫu nhiên theo chiều ngang
                    gp.screenHeight + 10,           // Bắt đầu từ dưới đáy màn hình
                    random.nextInt(15) + 5          // Kích thước hạt
            ));
        }

        // Cập nhật vị trí hạt và xóa các hạt đã bay lên quá cao hoặc hết "tuổi thọ"
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update();
            if (p.life <= 0 || p.y < 0) {
                it.remove();
            }
        }
    }

    public void getBackgroundImage() {
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/DialogueBackground/thu-vien.png"));
        } catch (IOException e) {
            System.out.println("Lỗi load ảnh background, nhớ check lại đường dẫn nha mày!");
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vẽ background
        if (background != null) {
            g2.drawImage(background, 0, 0, gp.screenWidth, gp.screenHeight, null);
        }

        // 2. Phủ một lớp bóng tối lên background để làm nổi bật lửa và UI
        g2.setColor(new Color(20, 10, 10, 180)); // Màu tối hơi ám đỏ
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // 3. Vẽ hiệu ứng hạt (Lửa và Khói)
        for (Particle p : particles) {
            p.draw(g2);
        }

        // 4. Vẽ Tiêu đề (Gradient Lửa)
        drawFieryTitle(g2, "SINH TỒN Ở HUST", 150);

        // 5. Vẽ các nút bấm style Sinh tồn
        drawSurvivalButton(g2, playButton, "CHƠI NGAY");
        drawSurvivalButton(g2, instructionsButton, "HƯỚNG DẪN");
        drawSurvivalButton(g2, infoButton, "THÔNG TIN");
    }

    private void drawFieryTitle(Graphics2D g2, String text, int y) {
        g2.setFont(new Font("Arial", Font.BOLD, 75)); // Dùng font cứng cáp hơn
        FontMetrics fm = g2.getFontMetrics();
        int x = (gp.screenWidth - fm.stringWidth(text)) / 2;

        // Đổ bóng đen siêu đậm phía sau
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(text, x + 6, y + 6);
        g2.drawString(text, x - 2, y + 2);

        // Gradient màu lửa cho chữ
        GradientPaint fireGradient = new GradientPaint(
                x, y - 70, new Color(255, 200, 0),    // Vàng sáng ở trên
                x, y, new Color(220, 20, 20)          // Đỏ rực ở dưới
        );
        g2.setPaint(fireGradient);
        g2.drawString(text, x, y);
    }

    private void drawSurvivalButton(Graphics2D g2, Rectangle rect, String text) {
        // Nền nút màu đen xám trong suốt
        g2.setColor(new Color(30, 30, 30, 200));
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);

        // Viền nút màu cam/đỏ rực
        g2.setColor(new Color(255, 69, 0)); // Orange Red
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);

        // Glow nhẹ ở viền trong
        g2.setColor(new Color(255, 140, 0, 100)); // Dark Orange mờ
        g2.drawRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4);

        // Text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();

        g2.drawString(text, textX, textY);
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        Point p = e.getPoint();
        if (playButton.contains(p)) {
            gp.setState(new KTXState(gp));
        } else if (instructionsButton.contains(p)) {
            gp.setState(new InstructionsState(gp));
        } else if (infoButton.contains(p)) {
            gp.setState(new InfoState(gp));
        }
    }

    // --- INNER CLASS ĐỂ QUẢN LÝ CÁC HẠT LỬA/KHÓI ---
    private class Particle {
        float x, y;
        float speedX, speedY;
        int size;
        int life, maxLife;
        Color color;

        public Particle(float x, float y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.maxLife = random.nextInt(100) + 50;
            this.life = this.maxLife;

            this.speedX = (random.nextFloat() - 0.5f) * 2; // Bay lượn ngang xíu
            this.speedY = -(random.nextFloat() * 3 + 1);   // Bay tốc độ khác nhau lên trên

            // Random màu: 60% Đỏ/Cam, 20% Vàng, 20% Xám đen (khói)
            int colorChoice = random.nextInt(100);
            if (colorChoice < 40) {
                color = new Color(220, 20, 20, 150); // Đỏ
            } else if (colorChoice < 60) {
                color = new Color(255, 140, 0, 150); // Cam
            } else if (colorChoice < 80) {
                color = new Color(255, 215, 0, 150); // Vàng
            } else {
                color = new Color(80, 80, 80, 100);  // Khói
            }
        }

        public void update() {
            x += speedX;
            y += speedY;
            life--;
            // Hạt nhỏ dần khi bay lên
            if (life % 10 == 0 && size > 1) {
                size--;
            }
        }

        public void draw(Graphics2D g2) {
            // Mờ dần theo thời gian sống
            float alpha = (float) life / maxLife;
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * alpha));
            g2.setColor(c);
            g2.fillOval((int) x, (int) y, size, size);
        }
    }
}