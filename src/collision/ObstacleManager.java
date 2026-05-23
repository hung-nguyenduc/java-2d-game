package collision;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ObstacleManager {

    /**
     * Hàm tiện ích static để đọc file tọa độ và tự động scale kích thước vật cản
     * @param filePath Đường dẫn đến file chứa tọa độ (.txt)
     * @param scale Tỉ lệ co giãn map hiện tại
     * @return Danh sách các Obstacle đã được scale chuẩn kích thước game
     */
    public static List<Obstacle> loadObstacles(String filePath, double scale) {
        List<Obstacle> list = new ArrayList<>();

        try {
            InputStream is = ObstacleManager.class.getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("Cảnh báo: Không tìm thấy file tọa độ vật cản tại: " + filePath);
                return list; // Trả về list rỗng để game không bị crash
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Bỏ qua dòng trống hoặc dòng comment bắt đầu bằng dấu #
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Tách các con số bằng dấu cách dựa theo file txt của bạn
                String[] data = line.split(" ");
                if (data.length == 4) {
                    int x = Integer.parseInt(data[0].trim());
                    int y = Integer.parseInt(data[1].trim());
                    int width = Integer.parseInt(data[2].trim());
                    int height = Integer.parseInt(data[3].trim());

                    // Tự động tính toán scale theo map con
                    int finalX = (int) (x * scale);
                    int finalY = (int) (y * scale);
                    int finalWidth = (int) (width * scale);
                    int finalHeight = (int) (height * scale);

                    // Khởi tạo vật cản tàng hình (Alpha = 0)
                    list.add(new Obstacle(finalX, finalY, finalWidth, finalHeight, new Color(0, 250, 0, 0)));
                }
            }
            br.close();
            System.out.println("ObstacleManager: Đã nạp thành công " + list.size() + " vật cản từ " + filePath);

        } catch (Exception e) {
            System.err.println("ObstacleManager: Lỗi khi xử lý đọc file vật cản!");
            e.printStackTrace();
        }

        return list;
    }
}