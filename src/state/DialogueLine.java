package state;

import java.awt.image.BufferedImage;

public class DialogueLine {
    public String text;          // Nội dung câu thoại
    public BufferedImage avatar; // Ảnh đại diện của người nói câu này (có thể null nếu là lời dẫn)

    public DialogueLine(String text, BufferedImage avatar) {
        this.text = text;
        this.avatar = avatar;
    }
}