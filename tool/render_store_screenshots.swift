import AppKit

// Compose the store screenshots: a colored canvas, a cat sticker, the app name, a title, a caption,
// and the unchanged capture in a rounded frame.
// Usage: swift tool/render_store_screenshots.swift <copy.json> <captures> <output> [device ...]
//   Devices default to "iphone ipad" (App Store, captures from meowUITests/StoreScreenshotTests);
//   "android-phone android-tablet" are the Google Play canvases (captures from the emulator).
//   <captures>/<device>/<lang>-<name>.png -> <output>/<device>/<lang>/<order>-<name>.png at the canvas size:
//   iphone 1320x2868, ipad 2064x2752, android-phone 1242x2208, android-tablet 1600x2560.
struct Copy: Decodable {
    let appName: String
    let appleLocale: String
    let shots: [[String]]
}

struct Layout {
    let width: Int
    let height: Int
    let margin: CGFloat
    let appName: (y: CGFloat, size: CGFloat)
    let sticker: (y: CGFloat, size: CGFloat)
    let title: (y: CGFloat, height: CGFloat, size: CGFloat, minimum: CGFloat)
    let caption: (y: CGFloat, height: CGFloat, size: CGFloat, minimum: CGFloat)
    let imageHeight: CGFloat
    let frameRadius: CGFloat
    let imageRadius: CGFloat
}

let layouts: [String: Layout] = [
    "iphone": Layout(width: 1320, height: 2868, margin: 104, appName: (130, 38), sticker: (48, 224),
                     title: (308, 280, 100, 76), caption: (608, 126, 49, 39),
                     imageHeight: 2020, frameRadius: 56, imageRadius: 42),
    "ipad": Layout(width: 2064, height: 2752, margin: 156, appName: (142, 48), sticker: (44, 260),
                   title: (342, 302, 124, 94), caption: (662, 128, 61, 48),
                   imageHeight: 1850, frameRadius: 40, imageRadius: 26),
    // Google Play refuses screenshots longer than twice their width, hence 9:16 and 10:16.
    "android-phone": Layout(width: 1242, height: 2208, margin: 98, appName: (114, 36), sticker: (34, 200),
                            title: (258, 240, 94, 72), caption: (514, 112, 46, 37),
                            imageHeight: 1480, frameRadius: 52, imageRadius: 40),
    "android-tablet": Layout(width: 1600, height: 2560, margin: 120, appName: (128, 44), sticker: (38, 236),
                             title: (314, 280, 112, 86), caption: (614, 132, 56, 44),
                             imageHeight: 1700, frameRadius: 40, imageRadius: 26),
]

enum RenderError: Error {
    case invalidArguments, unknownDevice(String), missingImage(String), textOverflow(String)
}

guard CommandLine.arguments.count >= 4 else { throw RenderError.invalidArguments }
let copyURL = URL(fileURLWithPath: CommandLine.arguments[1])
let sourceRoot = URL(fileURLWithPath: CommandLine.arguments[2])
let outputRoot = URL(fileURLWithPath: CommandLine.arguments[3])
let devices = CommandLine.arguments.count > 4 ? Array(CommandLine.arguments[4...]) : ["iphone", "ipad"]
let copies = try JSONDecoder().decode([String: Copy].self, from: Data(contentsOf: copyURL))
let languages = ["en", "zh-Hans", "ja"]
let stickerRoot = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
    .appendingPathComponent("../design/store/stickers")
let stickers = ["ginger", "black", "gray", "sleepy"]

func rgb(_ r: Int, _ g: Int, _ b: Int) -> NSColor {
    NSColor(srgbRed: CGFloat(r) / 255, green: CGFloat(g) / 255, blue: CGFloat(b) / 255, alpha: 1)
}
let ink = rgb(0x2C, 0x23, 0x23)
let paper = rgb(0xFF, 0xFF, 0xFF)
let blush = rgb(0xFF, 0xF6, 0xF7)
let butter = rgb(0xFF, 0xFF, 0xE0)
let coral = rgb(0xE7, 0x6A, 0x66)
let lavender = rgb(0xEE, 0xE8, 0xFA)

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
    guard let layout = layouts[device] else { throw RenderError.unknownDevice(device) }
    let width = layout.width
    let height = layout.height
    let size = CGSize(width: width, height: height)
    let source = "\(device)/\(language)-\(shot[2]).png"
    guard let image = NSImage(contentsOf: sourceRoot.appendingPathComponent(source)) else {
        throw RenderError.missingImage(source)
    }
    let stickerName = stickers[order % stickers.count] + ".png"
    guard let sticker = NSImage(contentsOf: stickerRoot.appendingPathComponent(stickerName)) else {
        throw RenderError.missingImage(stickerName)
    }
    let context = CGContext(data: nil, width: width, height: height, bitsPerComponent: 8,
                            bytesPerRow: width * 4, space: CGColorSpace(name: CGColorSpace.sRGB)!,
                            bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue)!
    NSGraphicsContext.saveGraphicsState()
    defer { NSGraphicsContext.restoreGraphicsState() }
    context.translateBy(x: 0, y: CGFloat(height))
    context.scaleBy(x: 1, y: -1)
    NSGraphicsContext.current = NSGraphicsContext(cgContext: context, flipped: true)
    let dark = order % 4 == 2
    [blush, butter, coral, lavender][order % 4].setFill()
    NSBezierPath(rect: CGRect(origin: .zero, size: size)).fill()
    let foreground = dark ? paper : ink
    let accent = dark ? paper : coral
    let margin = layout.margin
    let textWidth = CGFloat(width) - 2 * margin
    sticker.draw(in: CGRect(x: margin - 18, y: layout.sticker.y, width: layout.sticker.size, height: layout.sticker.size),
                 from: .zero, operation: .sourceOver, fraction: 1, respectFlipped: true,
                 hints: [.interpolation: NSImageInterpolation.high])
    let brandX = margin + layout.sticker.size + 30
    try text(copy.appName, language: language,
             rect: CGRect(x: brandX, y: layout.appName.y, width: CGFloat(width) - margin - brandX, height: 88),
             size: layout.appName.size, minimum: 26, bold: true, color: accent.withAlphaComponent(dark ? 0.85 : 1))
    try text(shot[0], language: language,
             rect: CGRect(x: margin, y: layout.title.y, width: textWidth, height: layout.title.height),
             size: layout.title.size, minimum: layout.title.minimum, bold: true, color: foreground)
    try text(shot[1], language: language,
             rect: CGRect(x: margin, y: layout.caption.y, width: textWidth, height: layout.caption.height),
             size: layout.caption.size, minimum: layout.caption.minimum, bold: false, color: foreground.withAlphaComponent(0.88))
    let imageHeight = layout.imageHeight
    let imageWidth = imageHeight * image.size.width / image.size.height
    let imageRect = CGRect(x: (CGFloat(width) - imageWidth) / 2,
                           y: CGFloat(height) - imageHeight - 74, width: imageWidth, height: imageHeight)
    let frame = imageRect.insetBy(dx: -14, dy: -14)
    ink.setFill()
    NSBezierPath(roundedRect: frame, xRadius: layout.frameRadius, yRadius: layout.frameRadius).fill()
    context.saveGState()
    NSBezierPath(roundedRect: imageRect, xRadius: layout.imageRadius, yRadius: layout.imageRadius).addClip()
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

for device in devices {
    for language in languages {
        guard let copy = copies[language] else { continue }
        for (order, shot) in copy.shots.enumerated() {
            print(try render(language: language, copy: copy, device: device, order: order, shot: shot))
        }
    }
}
