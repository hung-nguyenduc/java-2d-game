package state;

import main.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.event.MouseEvent;

public class LoadingState2 extends GameState {
    private Image loadingGif;
    private int frameCounter = 0;
    // Chỉnh thời gian loading ở đây (Ví dụ: 60 FPS * 3 giây = 180 frames)
    private final int LOADING_DURATION = 1140;
    private GameState nextState;

    // Nhận vào GamePanel và cái State mà mày muốn chuyển tới sau khi load xong
    public LoadingState2(GamePanel gp, GameState nextState) {
        super(gp);
        this.nextState = nextState;
    }

    @Override
    public void enter() {
        try {
            // LƯU Ý QUAN TRỌNG:
            // Phải dùng ImageIcon thay vì ImageIO.read() thì file GIF nó mới giữ được hoạt ảnh
            loadingGif = new ImageIcon(getClass().getResource("/video/cc-qpan.gif")).getImage();
        } catch (Exception e) {
            System.err.println("Không tìm thấy file GIF loading!");
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        frameCounter++;
        // Hết thời gian thì tự động chuyển sang map thực sự
        // CẢI TIẾN: Nếu ấn Space hoặc hết thời gian loading thì đều chuyển sang map mới
        if (gp.keyH.spacePressed || frameCounter >= LOADING_DURATION) {
            gp.keyH.spacePressed = false; // QUAN TRỌNG: Reset phím Space về false để tránh map sau vừa vào đã tự kích hoạt Space
            gp.setState(new LenDuong(gp, new DanhGiacMy(gp))); // Chuyển sang State LenDuong thay vì nextState trực tiếp để có thêm hiệu ứng bảng thông báo
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Vẽ màn hình nền đen xì
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Căn giữa cái GIF (Mày tự thay đổi chiều rộng, chiều cao cho khớp với ảnh gốc)
        if (loadingGif != null) {
            int gifWidth = 800;  // Chỉnh lại theo độ phân giải GIF của mày
            int gifHeight = 600; // Chỉnh lại theo độ phân giải GIF của mày
            int x = (gp.screenWidth - gifWidth) / 2;
            int y = (gp.screenHeight - gifHeight) / 2;

            // Chú ý: Truyền gp (GamePanel) vào tham số cuối (ImageObserver) để nó cập nhật frame liên tục
            g2.drawImage(loadingGif, x, y, gifWidth, gifHeight, gp);
        }
    }

    @Override
    public void exit() {
        loadingGif = null; // Dọn rác
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        // Có thể cho phép người chơi click chuột để skip loading nếu muốn
    }
}