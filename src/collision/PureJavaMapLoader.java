package collision;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class PureJavaMapLoader {
    private List<Rectangle2D.Float> obstacleList = new ArrayList<>();

    public void loadMap(String jsonFilePath) {
        try {
            // 1. Đọc toàn bộ file JSON thành 1 chuỗi String
            BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            String json = sb.toString();

            // 2. Tìm đến đoạn chứa lớp "Hitboxes"
            if (!json.contains("\"name\":\"Hitboxes\"")) {
                System.out.println("Không tìm thấy layer Hitboxes!");
                return;
            }

            // Cắt chuỗi lấy riêng phần mảng objects bên trong Hitboxes
            String hitboxesSection = json.split("\"name\":\"Hitboxes\"")[1];
            String objectsArray = hitboxesSection.split("\"objects\":\\[")[1].split("\\]")[0];

            // 3. Tách từng object hình chữ nhật bằng dấu ngoặc nhọn { }
            String[] objects = objectsArray.split("\\},\\s*\\{");
            for (String obj : objects) {
                // Dùng Regex hoặc hàm cắt chuỗi để lấy các thông số x, y, width, height
                float x = getValueFromKey(obj, "x");
                float y = getValueFromKey(obj, "y");
                float w = getValueFromKey(obj, "width");
                float h = getValueFromKey(obj, "height");

                // Thêm vào danh sách vật cản
                obstacleList.add(new Rectangle2D.Float(x, y, w, h));
            }
            System.out.println("Đã load thành công " + obstacleList.size() + " vật cản bằng Java thuần!");

        } catch (Exception e) {
            System.err.println("Lỗi đọc file map: " + e.getMessage());
        }
    }

    // Hàm bổ trợ để tìm giá trị số từ key trong chuỗi JSON nén
    private float getValueFromKey(String objStr, String key) {
        String pattern = "\"" + key + "\":";
        if (!objStr.contains(pattern)) return 0;
        String start = objStr.split(pattern)[1];
        String valueStr = start.split(",")[0].replaceAll("[\\}\\]\"\\s]", "");
        return Float.parseFloat(valueStr);
    }

    // Hàm check va chạm giữ nguyên logic cũ
    public boolean canMove(float nextX, float nextY, float playerW, float playerH) {
        Rectangle2D.Float nextPlayerPos = new Rectangle2D.Float(nextX, nextY, playerW, playerH);
        for (Rectangle2D.Float obs : obstacleList) {
            if (nextPlayerPos.intersects(obs)) {
                return false; // Đụng tường!
            }
        }
        return true; // Đi tiếp
    }
}