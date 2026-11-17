import SwiftUI

/// Victoria Pacific University's identity, read off its logo: a navy-tinted ground and ink, navy
/// as the primary colour (tint, selected states, links) and the logo gold for "live". Gold is too
/// pale to be text on white, so text uses navy (`accentText`, 10:1).
enum Ink {
    static let ground = Color(red: 0xF2 / 255, green: 0xF3 / 255, blue: 0xF9 / 255)
    static let ink = Color(red: 0x16 / 255, green: 0x1B / 255, blue: 0x3A / 255)
    /// Navy #2D397E: buttons, switches, the app's tint.
    static let primary = Color(red: 0x2D / 255, green: 0x39 / 255, blue: 0x7E / 255)
    /// The logo gold, a shade deeper so a small dot reads on white: what is live.
    static let accent = Color(red: 0xD9 / 255, green: 0xA1 / 255, blue: 0x3B / 255)
    /// Accent text on white: navy, ≥ 4.5:1 (the gold is not).
    static let accentText = Color(red: 0x2D / 255, green: 0x39 / 255, blue: 0x7E / 255)
    static let secondary = Color(red: 0x56 / 255, green: 0x5B / 255, blue: 0x78 / 255)
    static let tertiary = Color(red: 0x66 / 255, green: 0x6B / 255, blue: 0x8A / 255)
    /// Hairlines between rows.
    static let line = Color(red: 0xE2 / 255, green: 0xE4 / 255, blue: 0xEE / 255)
    /// The selected tab's pill: navy, faint.
    static let accentWash = Color(red: 0x2D / 255, green: 0x39 / 255, blue: 0x7E / 255).opacity(0.10)
    /// 已连接.
    static let ok = Color(red: 0x2F / 255, green: 0x9E / 255, blue: 0x5B / 255)
    /// Numbers that order the notes, and links: a navy a shade lighter than the primary.
    static let link = Color(red: 0x34 / 255, green: 0x42 / 255, blue: 0x9A / 255)
}

/// 说话人 1, 2, 3…: the design canvas's chips (blue, orange…), each a pale fill with a dark ink of the
/// same hue that passes 4.5:1 on it and on white. The Mac's `SpeakerColor` uses other hues.
enum SpeakerInk {
    private static let pairs: [(ink: UInt32, tint: UInt32)] = [
        (0x1F4E8C, 0xDCE7F7), (0x8A3A16, 0xF6E2D6), (0x1E6B45, 0xDDEFE4), (0x5B3A8C, 0xE9E1F5), (0x3F434C, 0xE7E9EE),
    ]
    private static func pair(_ number: Int) -> (ink: UInt32, tint: UInt32) { pairs[(max(1, number) - 1) % pairs.count] }
    static func ink(_ number: Int) -> Color { Color(hex: pair(number).ink) }
    /// The chip behind a speaker's name.
    static func tint(_ number: Int) -> Color { Color(hex: pair(number).tint) }
}

extension Color {
    init(hex: UInt32) {
        self.init(red: Double(hex >> 16 & 0xFF) / 255, green: Double(hex >> 8 & 0xFF) / 255, blue: Double(hex & 0xFF) / 255)
    }
}
