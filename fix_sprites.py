import os
from PIL import Image

def fix_alignment():
    directory = "res/enemy/soldier"
    
    # Kích thước khung chuẩn chung
    canvas_w, canvas_h = 400, 400
    # Tọa độ X tâm chuẩn và Y đáy chuẩn
    center_x = canvas_w // 2
    bottom_y = canvas_h - 20
    
    for filename in os.listdir(directory):
        if not filename.endswith(".png"):
            continue
            
        path = os.path.join(directory, filename)
        img = Image.open(path).convert("RGBA")
        bbox = img.getbbox()
        
        if bbox:
            left, upper, right, lower = bbox
            w = right - left
            h = lower - upper
            
            # Cắt lấy phần nhân vật
            char_img = img.crop(bbox)
            
            # Tạo khung mới rỗng hoàn toàn trong suốt
            new_img = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
            
            # Tính toán vị trí dán vào khung mới:
            # - Tâm theo trục X
            # - Căn đáy theo trục Y
            paste_x = center_x - (w // 2)
            paste_y = bottom_y - h
            
            new_img.paste(char_img, (paste_x, paste_y))
            new_img.save(path)
            print(f"Fixed {filename}: w={w}, h={h}")

if __name__ == "__main__":
    fix_alignment()
