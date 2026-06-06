package state;

import main.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Font;
import java.awt.BasicStroke;
import javax.swing.ImageIcon;
import java.awt.event.MouseEvent;

public class LenDuong extends GameState {
    private Image loadingGif;
    private int frameCounter = 0;
    private final int LOADING_DURATION = 180 * 6; // Thời gian chạy GIF (3 giây ở 60 FPS)
    private GameState nextState;

    // --- CÁC BIẾN CHO BẢNG THÔNG BÁO (Lấy từ DialogueManager của mày) ---
    private boolean showDialogue = false;  // Biến check xem đã đến lúc hiện bảng thông báo chưa
    private String[] dialogueLines;        // Mày muốn ghi gì thì nhét các câu vào mảng này
    private int dialogueIndex = 0;         // Câu thoại hiện tại
    private int charIndex = 0;             // Vị trí chữ hiện tại đang hiển thị
    private int textSpeed = 2;             // Tốc độ gõ chữ
    private int textFrameCounter = 0;      // Bộ đếm frame cho chữ

    public LenDuong(GamePanel gp, GameState nextState) {
        super(gp);
        this.nextState = nextState;

        // TỰ ĐỊNH NGHĨA NỘI DUNG THÔNG BÁO Ở ĐÂY NHA MÀY
        dialogueLines = new String[] {
                "Nghe theo tiếng gọi thiêng liêng của Tổ quốc, \nVũ và hàng ngàn sinh viên Bách Khoa đã gác lại giảng đường \nđể lên đường ra mặt trận.",

        };
    }

    @Override
    public void enter() {
        try {
            loadingGif = new ImageIcon(getClass().getResource("/video/lenduong.gif")).getImage();
        } catch (Exception e) {
            System.err.println("Lỗi: Không tìm thấy file GIF LenDuong!");
            e.printStackTrace();
        }

        // Reset lại toàn bộ bộ đếm khi bắt đầu vào State này
        frameCounter = 0;
        showDialogue = false;
        dialogueIndex = 0;
        charIndex = 0;
        textFrameCounter = 0;
    }

    @Override
    public void update() {
        // GIAI ĐOẠN 1: Đang chạy GIF loading
        if (!showDialogue) {
            frameCounter++;
            if (frameCounter >= LOADING_DURATION) {
                showDialogue = true; // Chạy xong GIF thì kích hoạt bảng thông báo
            }
        }
        // GIAI ĐOẠN 2: Hiệu ứng gõ chữ của bảng thông báo
        else {
            if (dialogueLines == null || dialogueIndex >= dialogueLines.length) return;

            String currentLine = dialogueLines[dialogueIndex];

            // Nếu chữ chưa hiện hết câu thì tăng tiến dần dần
            if (charIndex < currentLine.length()) {
                textFrameCounter++;
                if (textFrameCounter >= textSpeed) {
                    charIndex++;
                    textFrameCounter = 0;
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Luôn luôn vẽ nền đen xì
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // NẾU CHƯA XONG GIF -> Vẽ cái file GIF lên
        if (!showDialogue) {
            if (loadingGif != null) {
                int gifWidth = 700;  // Chỉnh lại theo kích thước chuẩn của mày
                int gifHeight = 500;
                int x = 50;
                int y = 50;
                g2.drawImage(loadingGif, x, y, gifWidth, gifHeight, gp);
            }
        }
        // NẾU ĐÃ XONG GIF -> Vẽ bảng thông báo (Bê nguyên form UI của mày sang)
        else {
            if (dialogueLines == null || dialogueIndex >= dialogueLines.length) return;

            // 1. Định vị khung thoại (Tao chỉnh boxY lên 0.4 cho nó nằm lệch giữa nhìn cho đẹp)
            int boxX = gp.screenWidth / 10;
            int boxY = (int) (gp.screenHeight * 0.4);
            int boxWidth = (int) (gp.screenWidth * 0.8);
            int boxHeight = (int) (gp.screenHeight * 0.22);

            // 2. Vẽ khung nền bảng thông báo
            g2.setColor(new Color(0, 0, 0, 210)); // Nền đen trong suốt góc bo tròn
            g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25); // Viền trắng

            // 3. Vẽ chữ lời thoại
            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            g2.setColor(Color.WHITE);

            int textX = boxX + 30;
            int textY = boxY + 45;

            String currentLine = dialogueLines[dialogueIndex];
            int safeCharIndex = Math.min(charIndex, currentLine.length());
            String visibleText = currentLine.substring(0, safeCharIndex);

            // Tách dòng chữ nếu có ký tự xuống dòng '\n'
            for (String line : visibleText.split("\n", -1)) {
                g2.drawString(line, textX, textY);
                textY += 30;
            }

            // 4. Gợi ý bấm nút chuột khi chữ chạy xong
            if (charIndex >= currentLine.length()) {
                g2.setFont(new Font("Arial", Font.ITALIC, 12));
                g2.setColor(Color.YELLOW);
                g2.drawString("[Bấm chuột để tiếp tục]", boxX + boxWidth - 160, boxY + boxHeight - 15);
            }
        }
    }

    @Override
    public void exit() {
        loadingGif = null; // Dọn rác bớt
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        // Chỉ xử lý click chuột khi bảng thông báo đã xuất hiện
        if (showDialogue) {
            if (dialogueLines == null || dialogueIndex >= dialogueLines.length) return;

            String currentLine = dialogueLines[dialogueIndex];

            // TRICK CỦA MÀY: Chữ đang chạy mà click -> Hiện full câu luôn để skip hiệu ứng gõ
            if (charIndex < currentLine.length()) {
                charIndex = currentLine.length();
            } else {
                // Nếu chữ đã full rồi -> Click tiếp theo sẽ nhảy câu mới
                dialogueIndex++;
                charIndex = 0;
                textFrameCounter = 0;

                // NẾU ĐÃ ĐỌC HẾT TẤT CẢ CÁC CÂU -> Lúc này mới chính thức chuyển Map thực sự
                if (dialogueIndex >= dialogueLines.length) {
                    gp.setState(nextState);
                }
            }
        }
    }
    // =========================================================
    // HÀM MAIN ĐỂ TEST ĐỘC LẬP
    // =========================================================
    public static void main(String[] args) {
        // 1. Tạo một cửa sổ Windows cơ bản
        javax.swing.JFrame window = new javax.swing.JFrame("Test LenDuong State");
        window.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // 2. Khởi tạo GamePanel của mày
        main.GamePanel gp = new main.GamePanel();

        // 3. Khởi tạo LenDuong State.
        // Vì chỉ test nên phần nextState tao truyền tạm là 'null'
        LenDuong testState = new LenDuong(gp, null);

        // Ép GamePanel load cái State này và chạy hàm enter()
        gp.setState(testState);
        testState.enter();

        // 4. Nhét GamePanel vào cửa sổ và hiển thị
        window.add(gp);
        window.pack(); // Lệnh này giúp cửa sổ tự ôm khít lấy kích thước (screenWidth, screenHeight) của GamePanel
        window.setLocationRelativeTo(null); // Cho cửa sổ bật lên ở chính giữa màn hình
        window.setVisible(true);

        // LƯU Ý: Nếu trong GamePanel của mày có hàm để khởi động Game Loop
        // (ví dụ như startGameThread() hay run()), thì mày phải gọi nó ở đây để game bắt đầu update và draw liên tục.
        // Ví dụ:
         gp.startGameThread();
    }
}