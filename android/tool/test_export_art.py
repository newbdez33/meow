import unittest

from PIL import Image

import export_art as art


def fixture(width, height, margin):
    """A transparent canvas with an opaque red block inset by `margin` on every side."""
    image = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    block = Image.new("RGBA", (width - 2 * margin, height - 2 * margin), (220, 40, 40, 255))
    image.paste(block, (margin, margin))
    return image


class ExportArtTest(unittest.TestCase):
    def test_padded_square_keeps_the_subject_size_relative_to_the_canvas(self):
        out = art.padded_square(fixture(100, 100, 25), 200)
        self.assertEqual((200, 200), out.size)
        self.assertEqual(0, out.getpixel((10, 10))[3])       # margin stays transparent
        self.assertEqual(255, out.getpixel((100, 100))[3])   # subject stays opaque

    def test_padded_square_centres_a_non_square_image(self):
        out = art.padded_square(fixture(100, 50, 0), 100)
        self.assertEqual((100, 100), out.size)
        self.assertEqual(0, out.getpixel((50, 5))[3])
        self.assertEqual(255, out.getpixel((50, 50))[3])

    def test_trimmed_square_fills_the_canvas_with_the_subject(self):
        out = art.trimmed_square(fixture(200, 100, 40), 128)
        self.assertEqual((128, 128), out.size)
        self.assertEqual(255, out.getpixel((64, 64))[3])
        self.assertGreater(out.getpixel((6, 64))[3], 200)    # trimmed: the subject reaches near the edge
        self.assertEqual(0, out.getpixel((64, 2))[3])        # padded to a square on the short axis

    def test_white_backgrounds_are_keyed_out(self):
        image = Image.new("RGBA", (10, 10), (255, 255, 255, 255))
        image.putpixel((5, 5), (0, 0, 0, 255))
        out = art.key_white(image)
        self.assertEqual(0, out.getpixel((0, 0))[3])
        self.assertEqual(255, out.getpixel((5, 5))[3])

    def test_cat_source_falls_back_to_the_ios_file(self):
        self.assertTrue(str(art.cat_source(1, ios_only=True)).endswith("c01.imageset/c01@2x.png"))


if __name__ == "__main__":
    unittest.main()
