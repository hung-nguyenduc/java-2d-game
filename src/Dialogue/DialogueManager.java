package Dialogue;

import java.awt.*;

public class DialogueManager {
    private DialogueLine[] lines; // Mảng chứa các câu thoại đã nâng cấp
    private int dialogueIndex = 0;
    private boolean isActive = false;

    public void startDialogue(DialogueLine[] newLines) {
        this.lines = newLines;
        this.dialogueIndex = 0;
        this.isActive = true;
    }

    public void advanceDialogue() {
        if (!isActive) return;
        dialogueIndex++;
        if (dialogueIndex >= lines.length) {
            isActive = false;
            onDialogueComplete();
        }
    }

    public void onDialogueComplete() {
        // Xử lý sau khi đọc xong thoại
    }

    public boolean isActive() { return isActive; }

    public void draw(Graphics2D g2, int screenWidth, int screenHeight) {
        if (!isActive || lines == null || dialogueIndex >= lines.length) return;

        // 1. Định vị khung thoại dưới đáy màn hình
        int boxX = screenWidth / 10;
        int boxY = (int) (screenHeight * 0.7);
        int boxWidth = (int) (screenWidth * 0.8);
        int boxHeight = (int) (screenHeight * 0.22);

        // 2. Lấy dữ liệu câu thoại hiện tại
        DialogueLine currentLine = lines[dialogueIndex];

        // 3. VẼ AVATAR NGƯỜI NÓI (Nếu câu thoại đó có gán ảnh)
        if (currentLine.avatar != null) {
            int avatarSizeX = 110; // Kích thước ảnh đại diện (hình vuông)
            int avatarSizeY = 90;
            // Đặt Avatar nằm sát lề trái, nhô lên cạnh trên của khung thoại một chút
            int avatarX = boxX + 50;
            int avatarY = boxY -  80; // Trừ đi một nửa kích thước để nó đè lên cạnh trên

            // Vẽ một lớp nền bo tròn nhỏ phía sau Avatar cho đẹp mắt
//            g2.setColor(new Color(30, 30, 30, 230));
//            g2.fillRoundRect(avatarX - 4, avatarY - 4, avatarSize + 8, avatarSize + 8, 15, 15);
//            g2.setColor(Color.WHITE);
//            g2.setStroke(new BasicStroke(2));
//            g2.drawRoundRect(avatarX - 4, avatarY - 4, avatarSize + 8, avatarSize + 8, 15, 15);

            // Vẽ ảnh nhân vật (ví dụ ảnh Độ Mimi hoặc Vũ)
            g2.drawImage(currentLine.avatar, avatarX, avatarY, avatarSizeX, avatarSizeY, null);
        }

        // 4. VẼ KHUNG THOẠI CHÍNH
        g2.setColor(new Color(0, 0, 0, 210)); // Màu đen trong suốt
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25);

        // 5. VẼ CHỮ LỜI THOẠI
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);

        // Nếu có avatar, ta đẩy lề chữ dịch sang phải một chút để không bị chữ đè lên ảnh
        int textX = (currentLine.avatar != null) ? boxX + 30 : boxX + 30;
        int textY = boxY + 45;

        for (String line : currentLine.text.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 30;
        }

        // Gợi ý bấm nút
        g2.setFont(new Font("Arial", Font.ITALIC, 12));
        g2.setColor(Color.YELLOW);
        g2.drawString("[Ấn Space để tiếp tục]", boxX + boxWidth - 150, boxY + boxHeight - 15);
    }
}