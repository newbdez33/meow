import AppKit

// Compose the App Store screenshots: a plain colored canvas, the app name, a title, a caption,
// and the unchanged capture from meowUITests/StoreScreenshotTests in a rounded frame.
// Usage: swift tool/render_store_screenshots.swift design/store-copy.json <captures> <output>
//   <captures>/iphone/<lang>-<name>.png (1320x2868) and <captures>/ipad/<lang>-<name>.png (2064x2752)
//   -> <output>/<device>/<lang>/<order>-<name>.png at the same sizes.
struct Copy: Decodable {
    let appName: String
    let appleLocale: String
    let shots: [[String]]
}

enum RenderError: Error {
    case invalidArguments, missingImage(String), textOverflow(String)
}

guard CommandLine.arguments.count == 4 else { throw RenderError.invalidArguments }
let copyURL = URL(fileURLWithPath: CommandLine.arguments[1])
let sourceRoot = URL(fileURLWithPath: CommandLine.arguments[2])
let outputRoot = URL(fileURLWithPath: CommandLine.arguments[3])
let copies = try JSONDecoder().decode([String: Copy].self, from: Data(contentsOf: copyURL))
let languages = ["en", "zh-Hans", "ja"]

func rgb(_ r: Int, _ g: Int, _ b: Int) -> NSColor {
    NSColor(srgbRed: CGFloat(r) / 255, green: CGFloat(g) / 255, blue: CGFloat(b) / 255, alpha: 1)
}
let ink = rgb(0x2C, 0x23, 0x23)
let paper = rgb(0xFF, 0xFF, 0xFF)
let blush = rgb(0xFF, 0xF6, 0xF7)
let butter = rgb(0xFF, 0xFF, 0xE0)
let coral = rgb(0xE7, 0x6A, 0x66)

func font(_ language: String, size: CGFloat, bold: Bool) -> NSFont {
    let candidates: [String]
    switch language {
    case "ja": candidates = bold ? ["HiraMaruProN-W4", "HiraginoSans-W6"] : ["HiraMaruProN-W4", "HiraginoSans-W3"]
    case "zh-Hans": candidates = bold ? ["STYuanti-SC-Bold", "PingFangSC-Semibold"] : ["STYuanti-SC-Regular", "PingFangSC-Regular"]
    default: candidates = []
    }
    for name in candidates {
        if let f = NSFont(name: name, size: size) { return f }
    }
    let system = NSFont.systemFont(ofSize: size, weight: bold ? .bold : .regular)
    if let rounded = system.fontDescriptor.withDesign(.rounded), let f = NSFont(descriptor: rounded, size: size) { return f }
    return system
}

@discardableResult
func text(_ value: String, language: String, rect: CGRect, size: CGFloat,
          minimum: CGFloat, bold: Bool, color: NSColor) throws -> CGFloat {
    let paragraph = NSMutableParagraphStyle()
    paragraph.lineBreakMode = .byWordWrapping
    paragraph.lineSpacing = size * 0.08
    var fitted = size
    while fitted >= minimum {
        let attributed = NSAttributedString(string: value, attributes: [
            .font: font(language, size: fitted, bold: bold),
            .foregroundColor: color,
            .paragraphStyle: paragraph,
        ])
        let bounds = attributed.boundingRect(
            with: CGSize(width: rect.width, height: .greatestFiniteMagnitude),
            options: [.usesLineFragmentOrigin, .usesFontLeading]
        )
        if ceil(bounds.height) <= rect.height && ceil(bounds.width) <= rect.width {
            attributed.draw(with: rect, options: [.usesLineFragmentOrigin, .usesFontLeading])
            return fitted
        }
        fitted -= 2
    }
    throw RenderError.textOverflow("\(language): \(value)")
}

func render(language: String, copy: Copy, device: String, order: Int, shot: [String]) throws -> String {
    let ipad = device == "ipad"
    let width = ipad ? 2064 : 1320
    let height = ipad ? 2752 : 2868
    let size = CGSize(width: width, height: height)
    let source = "\(device)/\(language)-\(shot[2]).png"
    guard let image = NSImage(contentsOf: sourceRoot.appendingPathComponent(source)) else {
        throw RenderError.missingImage(source)
    }
    let context = CGContext(data: nil, width: width, height: height, bitsPerComponent: 8,
                            bytesPerRow: width * 4, space: CGColorSpace(name: CGColorSpace.sRGB)!,
                            bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue)!
    NSGraphicsContext.saveGraphicsState()
    defer { NSGraphicsContext.restoreGraphicsState() }
    context.translateBy(x: 0, y: CGFloat(height))
    context.scaleBy(x: 1, y: -1)
    NSGraphicsContext.current = NSGraphicsContext(cgContext: context, flipped: true)
    let dark = order % 3 == 2
    (dark ? coral : order % 3 == 1 ? butter : blush).setFill()
    NSBezierPath(rect: CGRect(origin: .zero, size: size)).fill()
    let foreground = dark ? paper : ink
    let accent = dark ? paper : coral
    let margin: CGFloat = ipad ? 156 : 104
    let textWidth = CGFloat(width) - 2 * margin
    try text(copy.appName, language: language,
             rect: CGRect(x: margin, y: ipad ? 70 : 76, width: textWidth, height: 88),
             size: ipad ? 48 : 38, minimum: 26, bold: true, color: accent.withAlphaComponent(dark ? 0.85 : 1))
    try text(shot[0], language: language,
             rect: CGRect(x: margin, y: ipad ? 202 : 192, width: textWidth, height: ipad ? 302 : 272),
             size: ipad ? 124 : 100, minimum: ipad ? 94 : 76, bold: true, color: foreground)
    try text(shot[1], language: language,
             rect: CGRect(x: margin, y: ipad ? 518 : 490, width: textWidth, height: ipad ? 160 : 148),
             size: ipad ? 61 : 49, minimum: ipad ? 48 : 39, bold: false, color: foreground.withAlphaComponent(0.88))
    let imageHeight: CGFloat = ipad ? 1940 : 2098
    let imageWidth = imageHeight * image.size.width / image.size.height
    let imageRect = CGRect(x: (CGFloat(width) - imageWidth) / 2,
                           y: CGFloat(height) - imageHeight - 74, width: imageWidth, height: imageHeight)
    let frame = imageRect.insetBy(dx: -14, dy: -14)
    ink.setFill()
    NSBezierPath(roundedRect: frame, xRadius: ipad ? 40 : 56, yRadius: ipad ? 40 : 56).fill()
    context.saveGState()
    NSBezierPath(roundedRect: imageRect, xRadius: ipad ? 26 : 42, yRadius: ipad ? 26 : 42).addClip()
    image.draw(in: imageRect, from: .zero, operation: .sourceOver, fraction: 1,
               respectFlipped: true, hints: [.interpolation: NSImageInterpolation.high])
    context.restoreGState()
    let file = "\(device)/\(language)/\(order + 1)-\(shot[2]).png"
    let destination = outputRoot.appendingPathComponent(file)
    try FileManager.default.createDirectory(at: destination.deletingLastPathComponent(), withIntermediateDirectories: true)
    let bitmap = NSBitmapImageRep(cgImage: context.makeImage()!)
    try bitmap.representation(using: .png, properties: [:])!.write(to: destination)
    return file
}

for device in ["iphone", "ipad"] {
    for language in languages {
        guard let copy = copies[language] else { continue }
        for (order, shot) in copy.shots.enumerated() {
            print(try render(language: language, copy: copy, device: device, order: order, shot: shot))
        }
    }
}
