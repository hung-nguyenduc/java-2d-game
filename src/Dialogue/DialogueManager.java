package Dialogue;

import java.awt.*;

public class DialogueManager {
    private DialogueLine[] lines;
    private int dialogueIndex = 0;
    private boolean isActive = false;

    // --- CÁC BIẾN MỚI CHO HIỆU ỨNG GÕ CHỮ ---
    private int charIndex = 0;   // Vị trí chữ hiện tại đang hiển thị
    private int textSpeed = 1;   // Tốc độ gõ (số frame chờ để hiện 1 chữ, nhỏ thì nhanh)
    private int frameCounter = 0; // Bộ đếm đếm số frame trôi qua

    public void startDialogue(DialogueLine[] newLines) {
        this.lines = newLines;
        this.dialogueIndex = 0;
        this.isActive = true;

        // Nhớ reset lại bộ đếm khi bắt đầu đoạn thoại mới
        this.charIndex = 0;
        this.frameCounter = 0;
    }

    // TẠO THÊM HÀM UPDATE: Mày cần gọi hàm này liên tục trong Game Loop
    public void update() {
        if (!isActive || lines == null || dialogueIndex >= lines.length) return;

        DialogueLine currentLine = lines[dialogueIndex];

        // Nếu chữ chưa hiện hết thì tăng bộ đếm để hiện từ từ
        if (charIndex < currentLine.text.length()) {
            frameCounter++;
            if (frameCounter >= textSpeed) {
                charIndex++; // Mở khóa ký tự tiếp theo
                frameCounter = 0;
            }
        }
    }

    public void advanceDialogue() {
        if (!isActive) return;

        DialogueLine currentLine = lines[dialogueIndex];

        // TRICK HAY CHO GAME:
        // Nếu chữ đang chạy mà bấm Space -> Cho hiện toàn bộ câu luôn (skip hiệu ứng)
        if (charIndex < currentLine.text.length()) {
            charIndex = currentLine.text.length();
        } else {
            // Nếu chữ đã hiện full rồi -> Chuyển sang câu tiếp theo
            dialogueIndex++;
            charIndex = 0; // Reset lại cho câu mới
            frameCounter = 0;

            if (dialogueIndex >= lines.length) {
                isActive = false;
                onDialogueComplete();
            }
        }
    }

    public void onDialogueComplete() {
        // Xử lý sau khi đọc xong thoại
    }

    public boolean isActive() { return isActive; }

    public void draw(Graphics2D g2, int screenWidth, int screenHeight) {
        if (!isActive || lines == null || dialogueIndex >= lines.length) return;

        // 1. Định vị khung thoại
        int boxX = screenWidth / 10;
        int boxY = (int) (screenHeight * 0.7);
        int boxWidth = (int) (screenWidth * 0.8);
        int boxHeight = (int) (screenHeight * 0.22);

        // 2. Lấy dữ liệu
        DialogueLine currentLine = lines[dialogueIndex];

        // 3. VẼ AVATAR
        if (currentLine.avatar != null) {
            int avatarSizeX = 110;
            int avatarSizeY = 90;
            int avatarX = boxX + 50;
            int avatarY = boxY - 80;

            g2.drawImage(currentLine.avatar, avatarX, avatarY, avatarSizeX, avatarSizeY, null);
        }

        // 4. VẼ KHUNG THOẠI CHÍNH
        g2.setColor(new Color(0, 0, 0, 210));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25);

        // 5. VẼ CHỮ LỜI THOẠI (Hiệu ứng từng chữ)
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);

        int textX = boxX + 30;
        int textY = boxY + 45;

        // Cắt chuỗi gốc từ đầu cho đến vị trí charIndex hiện tại
        // Dùng Math.min để phòng hờ lỗi OutOfBounds nếu game lag
        int safeCharIndex = Math.min(charIndex, currentLine.text.length());
        String visibleText = currentLine.text.substring(0, safeCharIndex);

        // Tách chuỗi đang hiển thị theo dấu \n để vẽ (tham số -1 giúp giữ lại các dòng trống)
        for (String line : visibleText.split("\n", -1)) {
            g2.drawString(line, textX, textY);
            textY += 30;
        }

        // Gợi ý bấm nút: Mày có thể set cho nó chỉ hiện khi câu thoại đã chạy xong chữ
        if (charIndex >= currentLine.text.length()) {
            g2.setFont(new Font("Arial", Font.ITALIC, 12));
            g2.setColor(Color.YELLOW);
            g2.drawString("[Ấn Space để tiếp tục]", boxX + boxWidth - 150, boxY + boxHeight - 15);
        }
    }
}