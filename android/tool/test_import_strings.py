import re
import unittest

import import_strings as s


class ImportStringsTest(unittest.TestCase):
    def test_every_language_has_the_app_name_and_27_captions(self):
        for lproj in s.LANGS:
            xml = s.build(lproj)
            self.assertIn('<string name="app_name">', xml, lproj)
            self.assertEqual(27, len(re.findall(r'<string name="t\d\d">', xml)), lproj)

    def test_android_wording_replaces_apple_wording(self):
        for lproj in s.LANGS:
            xml = s.build(lproj)
            self.assertNotIn("Apple", xml, lproj)
            self.assertNotIn("App Store", xml, lproj)
            self.assertIn("Google", xml, lproj)

    def test_values_are_escaped_for_android(self):
        self.assertEqual("I\\'m %1$s &amp; you", s.to_android("I'm %@ & you"))
        self.assertEqual("say \\\"hi\\\"", s.to_android('say \\"hi\\"'))


if __name__ == "__main__":
    unittest.main()
