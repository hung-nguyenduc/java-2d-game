import os
from PIL import Image

def analyze_sprites():
    directory = "res/enemy/soldier"
    for filename in sorted(os.listdir(directory)):
        if filename.endswith(".png"):
            path = os.path.join(directory, filename)
            img = Image.open(path)
            bbox = img.getbbox() # (left, upper, right, lower)
            if bbox:
                cx = (bbox[0] + bbox[2]) / 2
                cy = (bbox[1] + bbox[3]) / 2
                w = bbox[2] - bbox[0]
                h = bbox[3] - bbox[1]
                print(f"{filename}: bbox={bbox}, center=({cx:.1f}, {cy:.1f}), size={w}x{h}")
            else:
                print(f"{filename}: empty")

if __name__ == "__main__":
    analyze_sprites()
