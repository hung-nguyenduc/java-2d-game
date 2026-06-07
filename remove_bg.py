import os
from rembg import remove
from PIL import Image

def process_image(input_path, output_path):
    print(f"Processing: {input_path}")
    input_image = Image.open(input_path)
    output_image = remove(input_image)
    output_image.save(output_path)

def main():
    directory = "res/enemy/soldier"
    for filename in os.listdir(directory):
        if filename.endswith(".png"):
            input_path = os.path.join(directory, filename)
            # Ghi đè trực tiếp lên file cũ
            process_image(input_path, input_path)

if __name__ == "__main__":
    main()
