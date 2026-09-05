"""Build the minimal production art set without modifying immutable design sources.

Generated source PNGs are staged in the ignored design_work directory. The script removes
generation fringes/checker backgrounds, normalizes transparent padding, creates the two bear
colour variants from one exact master, and exports Android-ready WebP files.
"""

from __future__ import annotations

from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter


ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT / "design_work"
ORIGINALS = ROOT / "design" / "originals"
OUTPUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"


def connected_subject(mask: Image.Image) -> Image.Image:
    binary = mask.point(lambda value: 255 if value >= 12 else 0)
    binary = binary.filter(ImageFilter.MaxFilter(5)).filter(ImageFilter.MinFilter(5))
    selected = binary.copy()
    ImageDraw.floodfill(selected, (selected.width // 2, selected.height // 2), 128, thresh=0)
    selected = selected.point(lambda value: 255 if value == 128 else 0)

    outside = selected.copy()
    ImageDraw.floodfill(outside, (0, 0), 128, thresh=0)
    filled = outside.point(lambda value: 0 if value == 128 else 255)
    return filled.filter(ImageFilter.GaussianBlur(0.8))


def saturation_cutout(image: Image.Image) -> Image.Image:
    rgb = image.convert("RGB")
    hsv = np.asarray(rgb.convert("HSV"))
    saturation = Image.fromarray(hsv[:, :, 1], "L")
    alpha = connected_subject(saturation)
    result = rgb.convert("RGBA")
    result.putalpha(alpha)
    return result


def neutral_background_cutout(image: Image.Image) -> Image.Image:
    """Remove a connected white/gray preview backdrop while retaining enclosed highlights."""
    rgb = image.convert("RGB")
    hsv = np.asarray(rgb.convert("HSV"))
    candidate = Image.fromarray(np.where(hsv[:, :, 1] < 24, 255, 0).astype(np.uint8), "L").copy()
    for corner in ((0, 0), (candidate.width - 1, 0), (0, candidate.height - 1), (candidate.width - 1, candidate.height - 1)):
        ImageDraw.floodfill(candidate, corner, 128, thresh=0)
    alpha = candidate.point(lambda value: 0 if value == 128 else 255).filter(ImageFilter.GaussianBlur(0.8))
    result = rgb.convert("RGBA")
    result.putalpha(alpha)
    return result


def luminous_cutout(image: Image.Image) -> Image.Image:
    """Extract multiple disconnected bright candy components from a dark generated backdrop."""
    rgb = image.convert("RGB")
    hsv = np.asarray(rgb.convert("HSV"))
    foreground = (hsv[:, :, 1] >= 52) & (hsv[:, :, 2] >= 108)
    binary = Image.fromarray(np.where(foreground, 255, 0).astype(np.uint8), "L")
    binary = binary.filter(ImageFilter.MaxFilter(5)).filter(ImageFilter.MinFilter(3))
    outside = binary.copy()
    ImageDraw.floodfill(outside, (0, 0), 128, thresh=0)
    alpha = outside.point(lambda value: 0 if value == 128 else 255).filter(ImageFilter.GaussianBlur(0.9))
    result = rgb.convert("RGBA")
    result.putalpha(alpha)
    return result


def checker_frame_cutout(image: Image.Image) -> Image.Image:
    """Remove a baked preview checkerboard from an empty UI frame.

    Image generation previews can flatten transparency into a nearly neutral checkerboard.
    The candy artwork is saturated (or appreciably darker), so the two large neutral regions
    can be selected from the outside corners and the known empty centre without erasing the
    enclosed cream icing or white specular highlights.
    """
    rgb = image.convert("RGB")
    hsv = np.asarray(rgb.convert("HSV"))
    saturation = hsv[:, :, 1]
    value = hsv[:, :, 2]
    foreground = (saturation >= 7) | (value <= 239)
    mask = Image.fromarray(np.where(foreground, 255, 0).astype(np.uint8), "L")
    mask = mask.filter(ImageFilter.MaxFilter(3)).filter(ImageFilter.MinFilter(3))

    selected = mask.copy()
    seeds = (
        (0, 0),
        (selected.width - 1, 0),
        (0, selected.height - 1),
        (selected.width - 1, selected.height - 1),
        (selected.width // 2, selected.height // 2),
    )
    for seed in seeds:
        if selected.getpixel(seed) == 0:
            ImageDraw.floodfill(selected, seed, 128, thresh=0)

    alpha = selected.point(lambda value: 0 if value == 128 else 255)
    alpha = alpha.filter(ImageFilter.GaussianBlur(0.7))
    result = rgb.convert("RGBA")
    result.putalpha(alpha)
    return result


def save_frame(image: Image.Image, name: str, target_width: int = 1024) -> None:
    rgba = image.convert("RGBA")
    bbox = rgba.getchannel("A").point(lambda value: 255 if value > 8 else 0).getbbox()
    if bbox is None:
        raise ValueError("Frame has no visible pixels")
    left, top, right, bottom = bbox
    padding = max(4, round((bottom - top) * 0.025))
    crop = rgba.crop(
        (
            max(0, left - padding),
            max(0, top - padding),
            min(rgba.width, right + padding),
            min(rgba.height, bottom + padding),
        )
    )
    target_height = round(crop.height * target_width / crop.width)
    crop.resize((target_width, target_height), Image.Resampling.LANCZOS).save(
        OUTPUT / name,
        "WEBP",
        lossless=True,
        method=6,
    )


def clean_existing_alpha(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    rgba.putalpha(connected_subject(rgba.getchannel("A")))
    return rgba


def recolor(image: Image.Image, hue: int, saturation_scale: float = 1.0) -> Image.Image:
    rgba = image.convert("RGBA")
    hsv = np.asarray(rgba.convert("RGB").convert("HSV")).copy()
    meaningful_colour = hsv[:, :, 1] > 7
    hsv[:, :, 0][meaningful_colour] = hue
    hsv[:, :, 1] = np.clip(hsv[:, :, 1].astype(np.float32) * saturation_scale, 0, 255).astype(np.uint8)
    recolored = Image.fromarray(hsv, "HSV").convert("RGBA")
    recolored.putalpha(rgba.getchannel("A"))
    return recolored


def recolor_saturated_accents(image: Image.Image, hue: int) -> Image.Image:
    """Shift only the saturated jelly rim while preserving pale fills and white highlights."""
    rgba = image.convert("RGBA")
    hsv = np.asarray(rgba.convert("RGB").convert("HSV")).copy()
    accents = hsv[:, :, 1] >= 62
    hsv[:, :, 0][accents] = hue
    hsv[:, :, 1][accents] = np.clip(
        hsv[:, :, 1][accents].astype(np.float32) * 1.06,
        0,
        255,
    ).astype(np.uint8)
    recolored = Image.fromarray(hsv, "HSV").convert("RGBA")
    recolored.putalpha(rgba.getchannel("A"))
    return recolored


def normalize_sprite(image: Image.Image, size: int = 256, padding_ratio: float = 0.055) -> Image.Image:
    rgba = image.convert("RGBA")
    bbox = rgba.getchannel("A").point(lambda value: 255 if value > 8 else 0).getbbox()
    if bbox is None:
        raise ValueError("Sprite has no visible pixels")
    left, top, right, bottom = bbox
    width, height = right - left, bottom - top
    side = int(max(width, height) * (1 + padding_ratio * 2))
    center_x = (left + right) // 2
    center_y = (top + bottom) // 2
    square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    crop_left = center_x - side // 2
    crop_top = center_y - side // 2
    source_box = (
        max(0, crop_left),
        max(0, crop_top),
        min(rgba.width, crop_left + side),
        min(rgba.height, crop_top + side),
    )
    crop = rgba.crop(source_box)
    paste_x = source_box[0] - crop_left
    paste_y = source_box[1] - crop_top
    square.alpha_composite(crop, (paste_x, paste_y))
    return square.resize((size, size), Image.Resampling.LANCZOS)


def save_sprite(image: Image.Image, name: str, size: int = 256) -> None:
    normalized = normalize_sprite(image, size=size)
    normalized.save(OUTPUT / name, "WEBP", lossless=True, method=6)


def cover_resize(image: Image.Image, target: tuple[int, int]) -> Image.Image:
    target_width, target_height = target
    scale = max(target_width / image.width, target_height / image.height)
    resized = image.resize(
        (round(image.width * scale), round(image.height * scale)),
        Image.Resampling.LANCZOS,
    )
    left = (resized.width - target_width) // 2
    top = (resized.height - target_height) // 2
    return resized.crop((left, top, left + target_width, top + target_height))


def masked_launcher_icon(art: Image.Image, size: int, *, round_icon: bool) -> Image.Image:
    """Create a legacy icon with the safe padding expected by pre-adaptive launchers."""
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    inset = max(2, round(size * 0.075))
    content_size = size - inset * 2
    content = art.resize((content_size, content_size), Image.Resampling.LANCZOS)
    mask = Image.new("L", (content_size, content_size), 0)
    draw = ImageDraw.Draw(mask)
    if round_icon:
        draw.ellipse((0, 0, content_size - 1, content_size - 1), fill=255)
    else:
        radius = max(2, round(content_size * 0.18))
        draw.rounded_rectangle((0, 0, content_size - 1, content_size - 1), radius=radius, fill=255)
    content.putalpha(mask)
    canvas.alpha_composite(content, (inset, inset))
    return canvas


def build() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)

    green_bear = clean_existing_alpha(Image.open(WORK / "green_bear_generated.png"))
    save_sprite(green_bear, "gummy_green_bear.webp")
    save_sprite(recolor(green_bear, hue=213, saturation_scale=1.08), "gummy_purple_bear.webp")
    save_sprite(recolor(green_bear, hue=0, saturation_scale=1.12), "gummy_red_bear.webp")

    star = saturation_cutout(Image.open(WORK / "green_star_generated.png"))
    save_sprite(star, "gummy_green_star.webp")

    heart = clean_existing_alpha(Image.open(WORK / "orange_heart_generated.png"))
    save_sprite(heart, "gummy_orange_heart.webp")

    bomb = saturation_cutout(Image.open(WORK / "pink_bomb_generated.png"))
    save_sprite(bomb, "gummy_pink_bomb.webp")

    home_button = clean_existing_alpha(Image.open(WORK / "home_button_generated.png"))
    save_sprite(home_button, "ui_home_button.webp")

    back_button = clean_existing_alpha(Image.open(WORK / "back_button_generated.png"))
    save_sprite(back_button, "ui_back_button.webp")

    music_icon = clean_existing_alpha(Image.open(WORK / "music_icon_generated.png"))
    save_sprite(music_icon, "ui_music_icon.webp")

    sound_icon = luminous_cutout(Image.open(WORK / "sound_icon_generated.png"))
    save_sprite(sound_icon, "ui_sound_icon.webp")

    button_frame = checker_frame_cutout(
        Image.open(WORK / "ui_frames" / "button_frame_pink_generated.png")
    )
    save_frame(button_frame, "ui_button_frame.webp")

    panel_frame = checker_frame_cutout(
        Image.open(WORK / "ui_frames" / "panel_frame_generated.png")
    )
    save_frame(panel_frame, "ui_panel_frame.webp")

    ui_sheet = Image.open(ORIGINALS / "gummy_ui_reference.png").convert("RGB")
    # Supplied item 12: glossy circular purple control, extracted without altering the sheet.
    round_button = saturation_cutout(ui_sheet.crop((338, 132, 444, 240)))
    save_sprite(round_button, "ui_round_button.webp")

    # Supplied items 13 and 15: distinct glossy tall preview frames for Hold and Next.
    hold_frame = recolor_saturated_accents(
        saturation_cutout(ui_sheet.crop((446, 136, 548, 242)),),
        hue=190,
    )
    save_frame(hold_frame, "ui_hold_frame.webp", target_width=512)
    next_frame = saturation_cutout(ui_sheet.crop((652, 136, 752, 242)))
    save_frame(next_frame, "ui_next_frame.webp", target_width=512)

    victory_bear = clean_existing_alpha(Image.open(WORK / "victory_bear_generated.png"))
    save_sprite(victory_bear, "gummy_victory_bear.webp", size=512)

    loss_bear = Image.open(WORK / "loss_bear_final.png").convert("RGBA")
    save_sprite(loss_bear, "gummy_loss_bear.webp", size=512)

    menu = cover_resize(Image.open(WORK / "candy_land_generated.png").convert("RGB"), (1080, 1920))
    menu.save(OUTPUT / "bg_candy_land.webp", "WEBP", quality=88, method=6)

    sheet = Image.open(ORIGINALS / "background_reference.png").convert("RGB")
    # Exact bg-03 panel bounds in the immutable 853×588 reference sheet.
    sprinkles = sheet.crop((553, 28, 806, 574))
    sprinkles = cover_resize(sprinkles, (1080, 1920))
    sprinkles.save(OUTPUT / "bg_sprinkles.webp", "WEBP", quality=90, method=6)

    promo = Image.open(ORIGINALS / "promo_reference.png").convert("RGB")
    # Clean square app-icon composition in the top-left of the supplied promo sheet.
    icon = promo.crop((24, 23, 225, 224)).resize((512, 512), Image.Resampling.LANCZOS)
    icon.save(OUTPUT / "app_icon_art.webp", "WEBP", quality=94, method=6)
    for folder, size in {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }.items():
        destination = ROOT / "app" / "src" / "main" / "res" / folder
        destination.mkdir(parents=True, exist_ok=True)
        masked_launcher_icon(icon, size, round_icon=False).save(
            destination / "ic_launcher.png", "PNG", optimize=True
        )
        masked_launcher_icon(icon, size, round_icon=True).save(
            destination / "ic_launcher_round.png", "PNG", optimize=True
        )


if __name__ == "__main__":
    build()
