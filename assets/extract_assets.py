import cv2
import numpy as np
from PIL import Image
import rembg
import os

def main():
    img_path = 'c:/git/s3explorer/assets/logo_guide.png'
    # Load image using OpenCV
    img = cv2.imread(img_path)
    
    # Convert to grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    
    # Threshold to find non-white areas (the blue icons and splash)
    # Background is white/light, so we invert it.
    _, thresh = cv2.threshold(gray, 240, 255, cv2.THRESH_BINARY_INV)
    
    # Find contours
    contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    # Filter contours by area to find the icons
    min_area = 1000  # min area to consider
    icons = []
    for cnt in contours:
        x, y, w, h = cv2.boundingRect(cnt)
        if w * h > min_area:
            icons.append((x, y, w, h))
            
    # Sort by area descending
    icons.sort(key=lambda x: x[2]*x[3], reverse=True)
    
    # The largest should be the Splash Screen (tall rectangle)
    # The second largest should be the 1024dp icon (square)
    # Let's save them
    idx = 0
    for (x, y, w, h) in icons[:5]: # top 5
        print(f"Found region: x={x}, y={y}, w={w}, h={h}")
        crop = img[y:y+h, x:x+w]
        cv2.imwrite(f'c:/git/s3explorer/assets/extracted_{idx}.png', crop)
        idx += 1
        
    # Assuming icons[1] is the 1024dp icon (square-ish) or icons[0] is the 1024dp and splash is icons[1]
    # We can check aspect ratio
    square_icon = None
    splash_bg = None
    for (x, y, w, h) in icons[:5]:
        aspect = float(w)/h
        if 0.9 < aspect < 1.1 and w > 200:
            square_icon = (x, y, w, h)
        elif aspect < 0.9 and w > 200:
            splash_bg = (x, y, w, h)
            
    if square_icon:
        x, y, w, h = square_icon
        crop = img[y:y+h, x:x+w]
        cv2.imwrite('c:/git/s3explorer/assets/ic_logo_highres.png', crop)
        
        # Use rembg on this crop
        # cv2 uses BGR, PIL uses RGB, rembg handles PIL or bytes.
        crop_pil = Image.fromarray(cv2.cvtColor(crop, cv2.COLOR_BGR2RGB))
        out_pil = rembg.remove(crop_pil)
        out_pil.save('c:/git/s3explorer/assets/ic_logo_transparent.png')
        print("Successfully extracted and removed background for the logo.")
        
    if splash_bg:
        x, y, w, h = splash_bg
        crop = img[y:y+h, x:x+w]
        cv2.imwrite('c:/git/s3explorer/assets/ic_splash_bg.png', crop)
        print("Successfully extracted splash background.")

if __name__ == "__main__":
    main()
