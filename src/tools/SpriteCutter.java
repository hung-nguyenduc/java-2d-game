package tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Công cụ cắt sprite sheet lính Mỹ thành từng frame riêng lẻ cho Enemy.
 * Chạy 1 lần duy nhất để tạo ra các file ảnh trong thư mục res/enemy/soldier/
 *
 * Sprite sheet 2752x1536 layout:
 * - Thanh tiêu đề: ~0-80px
 * - Hàng 1: TRƯỚC (left, 4 frame) + SAU (right, 4 frame) ~80-500px
 * - Nhãn: ~500-560px  
 * - Hàng 2: TRÁI (left, 4 frame) + PHẢI (right, 4 frame) ~560-1000px
 * - Nhãn: ~1000-1060px
 * - Hàng 3: 3/4 TRƯỚC (left, 4 frame) + 3/4 SAU (right, 4 frame) ~1060-1480px
 * - Nhãn: ~1480-1536px
 */
public class SpriteCutter {

    public static void main(String[] args) throws Exception {
        String inputPath = "res/enemy/soldier_spritesheet.png";
        String outputDir = "res/enemy/soldier";

        File outFolder = new File(outputDir);
        if (!outFolder.exists()) {
            outFolder.mkdirs();
        }

        BufferedImage sheet = ImageIO.read(new File(inputPath));
        int sheetW = sheet.getWidth();  // 2752
        int sheetH = sheet.getHeight(); // 1536

        System.out.println("Sprite sheet size: " + sheetW + " x " + sheetH);

        // === CẤU HÌNH CẮT CHÍNH XÁC ===
        // Nửa chiều rộng (tách 2 nhóm trái/phải)
        int halfW = sheetW / 2;  // 1376

        // Tọa độ Y bắt đầu mỗi hàng sprite (sau tiêu đề/nhãn)
        int row1Y = 80;    // TRƯỚC + SAU
        int row2Y = 560;   // TRÁI + PHẢI  
        int row3Y = 1060;  // 3/4 TRƯỚC + 3/4 SAU

        // Chiều cao vùng sprite mỗi hàng (trừ nhãn)
        int spriteRowH = 420;

        // Mỗi nửa chia 4 frame
        int frameW = halfW / 4;  // ~344
        int frameH = spriteRowH;

        System.out.println("Frame size: " + frameW + " x " + frameH);

        // Định nghĩa 6 hướng: [tên, rowStartY, side(0=left,1=right)]
        Object[][] directions = {
            {"down",      row1Y, 0},  // TRƯỚC - hàng 1 trái
            {"up",        row1Y, 1},  // SAU   - hàng 1 phải
            {"left",      row2Y, 0},  // TRÁI  - hàng 2 trái
            {"right",     row2Y, 1},  // PHẢI  - hàng 2 phải
            {"down_left", row3Y, 0},  // 3/4 TRƯỚC - hàng 3 trái
            {"up_right",  row3Y, 1},  // 3/4 SAU   - hàng 3 phải
        };

        int totalFrames = 0;

        for (Object[] dir : directions) {
            String name = (String) dir[0];
            int startY = (int) dir[1];
            int side = (int) dir[2];

            for (int frame = 0; frame < 4; frame++) {
                int x = side * halfW + frame * frameW;
                int y = startY;

                // Đảm bảo không vượt quá giới hạn ảnh
                int cropW = Math.min(frameW, sheetW - x);
                int cropH = Math.min(frameH, sheetH - y);

                if (cropW <= 0 || cropH <= 0) {
                    System.out.println("SKIP: " + name + "_" + (frame + 1) + " - out of bounds");
                    continue;
                }

                BufferedImage frameCrop = sheet.getSubimage(x, y, cropW, cropH);

                // Tạo ảnh với nền trong suốt
                BufferedImage output = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = output.createGraphics();
                g2.drawImage(frameCrop, 0, 0, null);
                g2.dispose();

                String fileName = name + "_" + (frame + 1) + ".png";
                File outFile = new File(outputDir, fileName);
                ImageIO.write(output, "png", outFile);
                System.out.println("Saved: " + outFile.getPath() + " (" + cropW + "x" + cropH + ")");
                totalFrames++;
            }
        }

        System.out.println("\n=== CẮT HOÀN TẤT! ===");
        System.out.println("Tổng cộng: " + totalFrames + " frame");
        System.out.println("Thư mục output: " + outFolder.getAbsolutePath());
    }
}
