import math
from PIL import Image, ImageDraw, ImageFilter

def draw_nexus_logo():
    size = 1024
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    cx, cy = size / 2, size / 2

    # Outer Shield / Hexagonal crest points
    # Hexagonal geometry with angular bottom tip
    shield_pts = [
        (cx, 80),          # top center peak
        (size - 120, 220), # top right
        (size - 120, 680), # mid right
        (cx, 950),         # bottom tip
        (120, 680),        # mid left
        (120, 220),        # top left
    ]

    # Glow layer behind shield
    glow_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_img)
    glow_draw.polygon(shield_pts, fill=(230, 57, 70, 70))
    glow_img = glow_img.filter(ImageFilter.GaussianBlur(radius=35))
    img.paste(glow_img, (0, 0), glow_img)

    # Outer Shield Border (Deep crimson to cyber orange)
    draw.polygon(shield_pts, fill=(22, 22, 30, 255), outline=(220, 38, 38, 255))
    
    # Inner border lines
    for offset, alpha, col in [(8, 200, (234, 88, 12)), (18, 150, (255, 107, 53))]:
        inner_pts = [
            (cx, 80 + offset * 1.5),
            (size - 120 - offset, 220 + offset * 0.5),
            (size - 120 - offset, 680 - offset * 0.8),
            (cx, 950 - offset * 1.5),
            (120 + offset, 680 - offset * 0.8),
            (120 + offset, 220 + offset * 0.5),
        ]
        draw.polygon(inner_pts, outline=(col[0], col[1], col[2], alpha))

    # Inner Shield Core fill (Dark obsidian carbon)
    core_pts = [
        (cx, 120),
        (size - 145, 245),
        (size - 145, 660),
        (cx, 910),
        (145, 660),
        (145, 245),
    ]
    draw.polygon(core_pts, fill=(13, 14, 21, 255))

    # Center Graphic: The NEXUS ARENA Emblem
    # A powerful geometric 'N' and 'A' fusion (Apex Chevron + Lightning Nexus)
    
    # Left Pillar (Crimson red)
    p_left = [
        (260, 300),
        (350, 240),
        (350, 760),
        (260, 700),
    ]
    draw.polygon(p_left, fill=(220, 38, 38, 255))

    # Left Pillar Highlight edge
    draw.line([(350, 240), (350, 760)], fill=(255, 100, 100, 255), width=6)

    # Right Pillar (Cyber flame orange)
    p_right = [
        (size - 350, 240),
        (size - 260, 300),
        (size - 260, 700),
        (size - 350, 760),
    ]
    draw.polygon(p_right, fill=(249, 115, 22, 255))

    # Right Pillar Highlight edge
    draw.line([(size - 350, 240), (size - 350, 760)], fill=(255, 180, 100, 255), width=6)

    # Diagonal Nexus Slash (Connecting N through center)
    p_slash = [
        (350, 240),
        (430, 240),
        (size - 260, 700),
        (size - 340, 700),
    ]
    draw.polygon(p_slash, fill=(185, 28, 28, 255))

    # Central Radiant Apex Chevron (The 'A' crown & Nexus Core)
    p_apex = [
        (cx, 280),
        (cx + 140, 520),
        (cx + 70, 520),
        (cx, 400),
        (cx - 70, 520),
        (cx - 140, 520),
    ]
    draw.polygon(p_apex, fill=(255, 255, 255, 255))

    # Apex glowing center crossbar
    p_cross = [
        (cx - 95, 560),
        (cx + 95, 560),
        (cx + 75, 610),
        (cx - 75, 610),
    ]
    draw.polygon(p_cross, fill=(255, 107, 53, 255))

    # Diamond Core Jewel in center
    p_core = [
        (cx, 650),
        (cx + 45, 710),
        (cx, 770),
        (cx - 45, 710),
    ]
    draw.polygon(p_core, fill=(255, 255, 255, 255))

    # Top Crown Arrowhead / Apex Peak
    p_crown = [
        (cx, 150),
        (cx + 70, 220),
        (cx, 200),
        (cx - 70, 220),
    ]
    draw.polygon(p_crown, fill=(255, 183, 3, 255))

    return img

def export_assets():
    import os
    logo_1024 = draw_nexus_logo()
    
    sizes = [512, 256, 128, 64, 48, 32, 16]
    res_images = {}
    
    for s in sizes:
        res_images[s] = logo_1024.resize((s, s), Image.Resampling.LANCZOS)
    
    # Save master images
    out_dirs = [
        r"C:\Users\PAUL\Downloads\Java Project\desktop\src\main\resources\icons",
        r"C:\Users\PAUL\Downloads\Java Project\public-web\icons",
        r"C:\Users\PAUL\Downloads\Java Project\public-web",
        r"C:\Users\PAUL\Downloads\Java Project"
    ]
    for d in out_dirs:
        os.makedirs(d, exist_ok=True)
    
    # Save to desktop resources
    desktop_icons = r"C:\Users\PAUL\Downloads\Java Project\desktop\src\main\resources\icons"
    res_images[256].save(os.path.join(desktop_icons, "app-logo-256.png"), format="PNG")
    res_images[128].save(os.path.join(desktop_icons, "app-logo-128.png"), format="PNG")
    res_images[64].save(os.path.join(desktop_icons, "app-logo-64.png"), format="PNG")
    res_images[32].save(os.path.join(desktop_icons, "app-logo-32.png"), format="PNG")
    res_images[16].save(os.path.join(desktop_icons, "app-logo-16.png"), format="PNG")
    res_images[256].save(os.path.join(desktop_icons, "app-logo.png"), format="PNG")
    
    # Save Windows .ico containing multiple resolutions
    ico_path = os.path.join(desktop_icons, "app-logo.ico")
    res_images[256].save(ico_path, format="ICO", sizes=[(256, 256), (128, 128), (64, 64), (48, 48), (32, 32), (16, 16)])

    # Also save to public-web for website favicon and logo
    web_dir = r"C:\Users\PAUL\Downloads\Java Project\public-web"
    res_images[256].save(os.path.join(web_dir, "app-logo.png"), format="PNG")
    res_images[32].save(os.path.join(web_dir, "favicon.png"), format="PNG")
    res_images[32].save(os.path.join(web_dir, "favicon.ico"), format="ICO", sizes=[(32, 32), (16, 16)])
    
    # Root dir copy for project
    root_dir = r"C:\Users\PAUL\Downloads\Java Project"
    res_images[256].save(os.path.join(root_dir, "app-logo.png"), format="PNG")
    res_images[256].save(os.path.join(root_dir, "app-logo.ico"), format="ICO", sizes=[(256, 256), (128, 128), (64, 64), (48, 48), (32, 32), (16, 16)])
    
    print("✓ Professional Nexus Arena logos successfully generated in all standard resolutions!")

if __name__ == "__main__":
    export_assets()
