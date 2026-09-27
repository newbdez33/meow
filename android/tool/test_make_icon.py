import unittest

from PIL import Image

import make_icon as icon


def synthetic_icon(size=64):
    """Plain green with a white square 'cat' in the middle, like the real icon."""
    image = Image.new("RGBA", (size, size), (136, 204, 136, 255))
    cat = Image.new("RGBA", (size // 2, size // 2), (255, 255, 255, 255))
    image.paste(cat, (size // 4, size // 4))
    return image


class MakeIconTest(unittest.TestCase):
    def test_keyed_cat_makes_the_green_transparent_and_keeps_the_cat(self):
        image = synthetic_icon()
        out = icon.keyed_cat(image, (136, 204, 136))
        self.assertEqual(0, out.getpixel((2, 2))[3])
        self.assertEqual(255, out.getpixel((32, 32))[3])

    def test_foreground_anchors_the_cat_to_the_bottom_inside_a_108dp_canvas(self):
        cat = icon.keyed_cat(synthetic_icon(), (136, 204, 136))
        out = icon.foreground(cat, 432)
        self.assertEqual((432, 432), out.size)
        self.assertEqual(0, out.getpixel((216, 20))[3])       # top margin is empty
        self.assertEqual(255, out.getpixel((216, 300))[3])    # the cat is in the lower middle

    def test_round_mask_clears_the_corners(self):
        out = icon.round_mask(synthetic_icon(96))
        self.assertEqual(0, out.getpixel((1, 1))[3])
        self.assertEqual(255, out.getpixel((48, 48))[3])


if __name__ == "__main__":
    unittest.main()
