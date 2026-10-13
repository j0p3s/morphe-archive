from PIL import Image, ImageDraw, ImageFont
import os

font_path = "/usr/share/fonts/liberation/LiberationSans-Bold.ttf"
text = "YTritik"
font_size = 70
width = 250
height = 80

def create_header(text, font_path, color, output_path):
    img = Image.new('RGBA', (width, height), (255, 255, 255, 0))
    d = ImageDraw.Draw(img)
    try:
        font = ImageFont.truetype(font_path, font_size)
    except IOError:
        font = ImageFont.load_default()
    
    bbox = d.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    x = (width - text_width) / 2
    y = (height - text_height) / 2 - bbox[1]
    
    d.text((x, y), text, fill=color, font=font)
    img.save(output_path)

out_dir = "patches/src/main/resources/change-header/drawable-xxxhdpi"
os.makedirs(out_dir, exist_ok=True)

create_header(text, font_path, (0, 0, 0, 255), f"{out_dir}/morphe_header_custom_light.png")
create_header(text, font_path, (255, 255, 255, 255), f"{out_dir}/morphe_header_custom_dark.png")
