package state;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class DialogueManager {
    private String[] dialogues; // Mảng chứa các câu thoại
    private int dialogueIndex = 0; // Câu thoại hiện tại đang đứng ở đâu
    private boolean isActive = false; // Trạng thái bảng hội thoại có đang mở không

    // Hàm nạp cuộc hội thoại mới vào máy
    public void startDialogue(String[] newDialogues) {
        this.dialogues = newDialogues;
        this.dialogueIndex = 0;
        this.isActive = true;
    }

    // Hàm xử lý khi người chơi ấn chuyển dòng (Gắn vào KeyHandler hoặc MouseListener)
    public void advanceDialogue() {
        if (!isActive) return;

        dialogueIndex++;
        // Nếu đã đọc hết câu cuối cùng trong mảng
        if (dialogueIndex >= dialogues.length) {
            isActive = false; // Tắt bảng hội thoại
            onDialogueComplete(); // Gọi hàm xử lý hậu kỳ (ví dụ: cho phép di chuyển tiếp)
        }
    }

    // Hàm này để bạn override hoặc tùy biến hành động khi nói chuyện xong
    public void onDialogueComplete() {
        System.out.println("Đã đọc xong hội thoại!");
    }

    public boolean isActive() {
        return isActive;
    }

    // Vẽ khung hội thoại (Sub-window) dưới đáy màn hình phong cách Anime Visual Novel
    public void draw(Graphics2D g2, int screenWidth, int screenHeight) {
        if (!isActive || dialogues == null || dialogueIndex >= dialogues.length) return;

        // 1. Thiết lập vị trí và kích thước khung thoại (Nằm ở góc dưới màn hình)
        int x = screenWidth / 10;
        int y = (int) (screenHeight * 0.7);
        int width = (int) (screenWidth * 0.8);
        int height = (int) (screenHeight * 0.22);

        // 2. Vẽ bóng mờ phía sau khung (Màu đen trong suốt)
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRoundRect(x, y, width, height, 25, 25);

        // 3. Vẽ viền trắng cho khung thoại thêm nổi bật
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, width, height, 25, 25);

        // 4. Vẽ nội dung chữ lời thoại
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);

        String currentText = dialogues[dialogueIndex];

        // Xử lý xuống dòng tự động nếu câu thoại chứa ký tự '\n'
        int textX = x + 30;
        int textY = y + 40;
        for (String line : currentText.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 30; // Khoảng cách giữa các dòng chữ
        }

        // 5. Vẽ gợi ý nhỏ ở góc phải dưới: "[Ấn ENTER để tiếp tục]"
        g2.setFont(new Font("Arial", Font.ITALIC, 12));
        g2.setColor(Color.YELLOW);
        g2.drawString("[Ấn ENTER để tiếp tục]", x + width - 150, y + height - 15);
    }
}