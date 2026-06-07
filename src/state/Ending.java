package state;

import main.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Font;
import java.awt.BasicStroke;
import javax.swing.ImageIcon;
import java.awt.event.MouseEvent;

public class Ending extends GameState {

    // --- QUẢN LÝ GIAI ĐOẠN (0 = Thoại, 1 = Chạy GIF) ---
    private int currentPhase = 0;

    // --- BIẾN CHO HỘP THOẠI ---
    private String[] dialogueLines;
    private int dialogueIndex = 0;
    private int charIndex = 0;
    private int textSpeed = 1; // Tốc độ gõ chữ (càng nhỏ càng nhanh)
    private int textFrameCounter = 0;

    // --- BIẾN CHO GIF ---
    private Image endingGif;
    private int gifFrameCounter = 0;
    private final int GIF_DURATION = 760; // Thời gian chạy GIF (VD: 300 frames = 5 giây ở 60FPS)
    private GameState nextState; // State chuyển đến sau khi xem xong Ending (VD: Menu)

    public Ending(GamePanel gp, GameState nextState) {
        super(gp);
        this.nextState = nextState;

        // MÀY TỰ THAY NỘI DUNG THOẠI Ở ĐÂY NHA
        // Dùng \n để ép xuống dòng nếu câu quá dài
        dialogueLines = new String[] {
                "Mùa hè đỏ lửa năm 1971 - 1972, giữa lúc cuộc kháng chiến\nchống Mỹ cứu nước bước vào giai đoạn khốc liệt nhất, " +
                        "\nnghe theo tiếng gọi thiêng liêng của Tổ quốc, gần 3.000\ncán bộ và sinh viên Đại học Bách Khoa Hà Nội đã tạm gác lại\nhoài bão kỹ sư, " +
                        "xếp lại bút nghiên để lên đường ra mặt trận.",
                "Trong những trận chiến sinh tử ấy, hơn 200 người con ưu tú\ncủa Bách Khoa đã mãi mãi gửi lại tuổi đôi mươi nơi chiến\ntrường, " +
                        "xương máu các anh đã hòa vào lòng đất mẹ cho màu\nxanh hòa bình hôm nay.",
                "Những trang sách giảng đường có thể dở dang, nhưng thiên\nanh hùng ca về một thế hệ 'Xếp bút nghiên chiến đấu' sẽ còn\nvang vọng mãi. " +
                        "Xin nghiêng mình tri ân các anh, những người\nđã ngã xuống để cổng trường Parabol luôn rộng mở đón các\nthế hệ mai sau..."

        };
    }

    @Override
    public void enter() {
        try {
            // MÀY ĐỔI ĐƯỜNG DẪN FILE GIF Ở ĐÂY
            endingGif = new ImageIcon(getClass().getResource("/video/dai-tuong-niem.gif")).getImage();
        } catch (Exception e) {
            System.err.println("Lỗi: Không tìm thấy file GIF Ending!");
            e.printStackTrace();
        }

        // Reset lại mọi thứ từ đầu mỗi khi vào State này
        currentPhase = 0;
        dialogueIndex = 0;
        charIndex = 0;
        textFrameCounter = 0;
        gifFrameCounter = 0;
    }

    @Override
    public void update() {
        if (currentPhase == 0) {
            // Đang ở giai đoạn chạy chữ
            if (dialogueLines == null || dialogueIndex >= dialogueLines.length) return;

            String currentLine = dialogueLines[dialogueIndex];

            // Chữ từ từ hiện ra
            if (charIndex < currentLine.length()) {
                textFrameCounter++;
                if (textFrameCounter >= textSpeed) {
                    charIndex++;
                    textFrameCounter = 0;
                }
            }
        }
        else if (currentPhase == 1) {
            // Đang ở giai đoạn chạy GIF
            gifFrameCounter++;

            // Nếu muốn GIF chạy một thời gian rồi tự thoát ra Menu thì dùng cái này:
            if (gifFrameCounter >= GIF_DURATION) {
                if (nextState != null) {
                    gp.setState(nextState);
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Vẽ nền đen
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if (currentPhase == 0) {
            // ==========================================
            // VẼ HỘP THOẠI CỠ LỚN
            // ==========================================
            if (dialogueLines == null || dialogueIndex >= dialogueLines.length) return;

            // Kích thước hộp thoại (Tao để rộng 80%, cao 60% màn hình)
            int boxWidth = (int) (gp.screenWidth * 0.9);
            int boxHeight = (int) (gp.screenHeight * 0.9);
            int boxX = (gp.screenWidth - boxWidth) / 2;
            int boxY = (gp.screenHeight - boxHeight) / 2;

            // Khung nền
            g2.setColor(new Color(0, 0, 0, 220));
            g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 35, 35);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4));
            g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 35, 35);

            // Vẽ chữ
            g2.setFont(new Font("Arial", Font.BOLD, 20)); // Chữ to hơn cho dễ đọc
            g2.setColor(Color.WHITE);

            int textX = boxX + 60; // Thụt lề trái
            int textY = boxY + 80; // Cách lề trên

            String currentLine = dialogueLines[dialogueIndex];
            int safeCharIndex = Math.min(charIndex, currentLine.length());
            String visibleText = currentLine.substring(0, safeCharIndex);

            // Tách dòng
            for (String line : visibleText.split("\n", -1)) {
                g2.drawString(line, textX, textY);
                textY += 40; // Khoảng cách giữa các dòng
            }

            // Gợi ý bấm chuột
            if (charIndex >= currentLine.length()) {
                g2.setFont(new Font("Arial", Font.ITALIC, 16));
                g2.setColor(Color.YELLOW);
                g2.drawString("[Bấm chuột để tiếp tục]", boxX + boxWidth - 220, boxY + boxHeight - 30);
            }
        }
        else if (currentPhase == 1) {
            // ==========================================
            // VẼ GIF
            // ==========================================
            if (endingGif != null) {
                int gifWidth = 880;  // Thay đổi thông số này theo GIF gốc
                int gifHeight = 600;
                int x = -50;
                int y = -30;

                // Cập nhật frame GIF liên tục bằng 'gp'
                g2.drawImage(endingGif, x, y, gifWidth, gifHeight, gp);
            }
        }
    }

    @Override
    public void exit() {
        endingGif = null; // Dọn rác khi xong Ending
        gp.sound.stopMusic(); // Tắt nhạc khi rời màn Ending
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        if (currentPhase == 0) {
            // Tương tác khi đang hiển thị hộp thoại
            if (dialogueLines == null || dialogueIndex >= dialogueLines.length) return;

            String currentLine = dialogueLines[dialogueIndex];

            if (charIndex < currentLine.length()) {
                // Click để skip hiệu ứng gõ chữ
                charIndex = currentLine.length();
            } else {
                // Chuyển sang câu tiếp theo
                dialogueIndex++;
                charIndex = 0;
                textFrameCounter = 0;

                // Nếu hết thoại -> Chuyển sang Phase 1 (Chạy GIF)
                if (dialogueIndex >= dialogueLines.length) {
                    currentPhase = 1;
                    gp.sound.playMusic("am_thanh_end_game"); // Nhạc cho đoạn GIF tưởng niệm
                }
            }
        }
        else if (currentPhase == 1) {
            // Mở comment dòng dưới nếu mày muốn cho người chơi click bỏ qua luôn đoạn GIF
            // if (nextState != null) gp.setState(nextState);
        }
    }
}