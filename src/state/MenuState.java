package state;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.GamePanel;

public class MenuState extends GameState {
    // Khai báo các ảnh UI
    private BufferedImage background;
    private BufferedImage titleImg; // Ảnh chữ "Sinh Tồn Ở HUST" có hiệu ứng lửa

    // Ảnh nút bấm (Mỗi nút cần 2 ảnh: Bình thường và khi di chuột vào)
    private BufferedImage playImg, playHoverImg;
    private BufferedImage insImg, insHoverImg;
    private BufferedImage infoImg, infoHoverImg;

    private Rectangle playButton, instructionsButton, infoButton;

    // Biến lưu trạng thái xem chuột có đang nằm trên nút không
    private boolean isPlayHover = false;
    private boolean isInsHover = false;
    private boolean isInfoHover = false;

    public MenuState(GamePanel gp) {
        super(gp);
        loadImages();

        // Setup kích thước nút (giả sử ảnh mày tải về tỷ lệ này)
        int btnWidth = 260;
        int btnHeight = 70;
        int centerX = (gp.screenWidth - btnWidth) / 2;

        playButton = new Rectangle(centerX, 250, btnWidth, btnHeight);
        instructionsButton = new Rectangle(centerX, 340, btnWidth, btnHeight);
        infoButton = new Rectangle(centerX, 430, btnWidth, btnHeight);
    }

    private void loadImages() {
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/DialogueBackground/thu-vien.png"));

            // Mày tự tạo thư mục /ui/ trong res và bỏ ảnh tải trên mạng vào nhé
            titleImg = ImageIO.read(getClass().getResourceAsStream("/ui/logo_game.png"));

            // playImg = ImageIO.read(getClass().getResourceAsStream("/ui/btn_play.png"));
            // playHoverImg = ImageIO.read(getClass().getResourceAsStream("/ui/btn_play_hover.png"));

            // insImg = ImageIO.read(getClass().getResourceAsStream("/ui/btn_ins.png"));
            // insHoverImg = ImageIO.read(getClass().getResourceAsStream("/ui/btn_ins_hover.png"));

            // infoImg = ImageIO.read(getClass().getResourceAsStream("/ui/btn_info.png"));
            // infoHoverImg = ImageIO.read(getClass().getResourceAsStream("/ui/btn_info_hover.png"));

        } catch (Exception e) {
            System.out.println("Lỗi load ảnh");
        }
    }

    @Override
    public void enter() {}

    @Override
    public void exit() {}

    @Override
    public void update() {
        // Lấy vị trí chuột hiện tại trên màn hình
        Point mousePos = gp.getMousePosition();
        if (mousePos != null) {
            isPlayHover = playButton.contains(mousePos);
            isInsHover = instructionsButton.contains(mousePos);
            isInfoHover = infoButton.contains(mousePos);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // 1. Vẽ nền lớp học
        if (background != null) {
            g2.drawImage(background, 0, 0, gp.screenWidth, gp.screenHeight, null);
        }

        // 2. Phủ lớp kính đen mờ để làm nổi bật UI
//        g2.setColor(new Color(0, 0, 0, 160));
//        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // 3. Vẽ Logo Game
        if (titleImg != null) {
            int titleX = (gp.screenWidth - titleImg.getWidth()) / 2;
            g2.drawImage(titleImg, titleX, 30, null);
        } else {
            // Backup nếu chưa có ảnh logo
            g2.setColor(Color.ORANGE);
            g2.setFont(new Font("Arial", Font.BOLD, 65));
            g2.drawString("SINH TỒN Ở HUST", gp.screenWidth/2 - 300, 250);
        }

        // 4. Vẽ các nút bấm
        drawButton(g2, isPlayHover ? playHoverImg : playImg, playButton, "CHƠI NGAY", isPlayHover);
        drawButton(g2, isInsHover ? insHoverImg : insImg, instructionsButton, "HƯỚNG DẪN", isInsHover);
        drawButton(g2, isInfoHover ? infoHoverImg : infoImg, infoButton, "THÔNG TIN", isInfoHover);
    }

    // Hàm phụ trợ để vẽ nút. Nếu chưa có ảnh thì vẽ tay làm backup
    private void drawButton(Graphics2D g2, BufferedImage img, Rectangle rect, String text, boolean isHover) {
        if (img != null) {
            g2.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
        } else {
            // BACKUP: Vẽ tay nếu mày chưa kiếm được ảnh
            g2.setColor(isHover ? new Color(255, 100, 0) : new Color(150, 50, 0));
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

            g2.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();
            int tx = rect.x + (rect.width - fm.stringWidth(text)) / 2;
            int ty = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, tx, ty);
        }
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
}