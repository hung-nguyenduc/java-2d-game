package tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class SpriteFixer {

    public static void main(String[] args) {
        String directory = "res/enemy/soldier";
        File dir = new File(directory);
        
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Directory not found: " + directory);
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) return;

        int canvasW = 400;
        int canvasH = 400;
        int centerX = canvasW / 2;
        int bottomY = canvasH - 20;

        for (File file : files) {
            try {
                BufferedImage img = ImageIO.read(file);
                int[] bbox = getBoundingBox(img);
                
                if (bbox != null) {
                    int left = bbox[0];
                    int upper = bbox[1];
                    int right = bbox[2];
                    int lower = bbox[3];
                    
                    int w = right - left;
                    int h = lower - upper;

                    // Cắt lấy phần nhân vật
                    BufferedImage charImg = img.getSubimage(left, upper, w, h);

                    // Tạo khung mới 400x400 trong suốt
                    BufferedImage newImg = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = newImg.createGraphics();
                    
                    // Xóa toàn bộ khung thành trong suốt
                    g2d.setComposite(AlphaComposite.Clear);
                    g2d.fillRect(0, 0, canvasW, canvasH);
                    g2d.setComposite(AlphaComposite.SrcOver);

                    // Căn giữa X và chạm đáy Y
                    int pasteX = centerX - (w / 2);
                    int pasteY = bottomY - h;

                    g2d.drawImage(charImg, pasteX, pasteY, null);
                    g2d.dispose();

                    // Ghi đè file
                    ImageIO.write(newImg, "png", file);
                    System.out.println("Fixed " + file.getName() + ": w=" + w + ", h=" + h);
                } else {
                    System.out.println("Skipped " + file.getName() + ": No visible pixels");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tìm bounding box của các pixel không trong suốt
     * @return mảng [left, upper, right, lower] hoặc null nếu ảnh rỗng
     */
    private static int[] getBoundingBox(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int left = width;
        int upper = height;
        int right = 0;
        int lower = 0;

        boolean found = false;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                
                if (alpha > 0) { // Pixel không trong suốt
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < upper) upper = y;
                    if (y > lower) lower = y;
                    found = true;
                }
            }
        }

        if (found) {
            // right và lower cần +1 vì getSubimage lấy độ rộng là right - left
            return new int[]{left, upper, right + 1, lower + 1};
        }
        return null;
    }
}
