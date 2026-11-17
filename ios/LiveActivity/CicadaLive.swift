import ActivityKit
import SwiftUI
import WidgetKit

// The CicadaLive extension: draws ListenActivity (ios/Sources/Cicada/ListenActivity.swift) on the
// lock screen and in the Dynamic Island. Built by ios/scripts/build.sh into PlugIns/CicadaLive.appex.

@main
struct CicadaLiveBundle: WidgetBundle {
    var body: some Widget { ListenLiveActivity() }
}

private enum LiveInk {
    static let ink = Color(red: 0x16 / 255, green: 0x1B / 255, blue: 0x3A / 255)
    /// Navy for text on the light lock screen card; the gold is for dots and the dark Island.
    static let primary = Color(red: 0x2D / 255, green: 0x39 / 255, blue: 0x7E / 255)
    static let accent = Color(red: 0xE6 / 255, green: 0xBC / 255, blue: 0x66 / 255)
    static let grey = Color(red: 0x56 / 255, green: 0x5B / 255, blue: 0x78 / 255)
    static let ground = Color(red: 0xF2 / 255, green: 0xF3 / 255, blue: 0xF9 / 255)
}

private struct LiveLogo: View {
    let size: CGFloat
    private static let image = Bundle.main.path(forResource: "logo", ofType: "png").flatMap(UIImage.init(contentsOfFile:))

    var body: some View {
        Group {
            if let image = Self.image { Image(uiImage: image).resizable().scaledToFit() } else { LiveInk.primary }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: size / 4, style: .continuous))
    }
}

/// The elapsed time, counting up from the start, ticked by the system.
private struct Elapsed: View {
    let since: Date
    var body: some View {
        Text(timerInterval: since...Date.distantFuture, countsDown: false)
            .monospacedDigit()
    }
}

struct ListenLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: ListenActivity.self) { context in
            // Lock screen: the logo, 正在听 · time, the languages, and the newest line.
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 10) {
                    LiveLogo(size: 34)
                    VStack(alignment: .leading, spacing: 1) {
                        HStack(spacing: 6) {
                            Circle().fill(LiveInk.accent).frame(width: 7, height: 7)
                            Text("正在听").font(.footnote.weight(.semibold)).foregroundStyle(LiveInk.primary)
                            Elapsed(since: context.attributes.startedAt).font(.footnote.weight(.semibold)).foregroundStyle(LiveInk.primary)
                        }
                        Text(verbatim: "\(String(localized: "会议室")) · \(context.attributes.pair)")
                            .font(.subheadline.weight(.semibold)).foregroundStyle(LiveInk.ink)
                    }
                    Spacer()
                    if context.state.people >= 2 {
                        Text(String(localized: "\(context.state.people) 人在说")).font(.caption).foregroundStyle(LiveInk.grey)
                    }
                }
                if !context.state.line.isEmpty {
                    Text(context.state.line).font(.callout).foregroundStyle(LiveInk.ink).lineLimit(2)
                } else {
                    Text("开始说话后，字幕会出现在这里").font(.callout).foregroundStyle(LiveInk.grey)
                }
            }
            .padding(16)
            .activityBackgroundTint(LiveInk.ground.opacity(0.92))
            .activitySystemActionForegroundColor(LiveInk.ink)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    HStack(spacing: 8) {
                        LiveLogo(size: 26)
                        Text("正在听").font(.footnote.weight(.semibold)).foregroundStyle(LiveInk.accent)
                    }
                    .padding(.leading, 4)
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Elapsed(since: context.attributes.startedAt).font(.footnote.weight(.semibold))
                        .foregroundStyle(.white).frame(maxWidth: 64, alignment: .trailing).padding(.trailing, 4)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(context.state.line.isEmpty ? String(localized: "开始说话后，字幕会出现在这里") : context.state.line)
                            .font(.callout).foregroundStyle(.white).lineLimit(2)
                        Text(verbatim: "\(String(localized: "会议室")) · \(context.attributes.pair)")
                            .font(.caption2).foregroundStyle(.white.opacity(0.6))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 4)
                }
            } compactLeading: {
                HStack(spacing: 5) {
                    Circle().fill(LiveInk.accent).frame(width: 7, height: 7)
                    LiveLogo(size: 20)
                }
            } compactTrailing: {
                Elapsed(since: context.attributes.startedAt)
                    .font(.caption.weight(.semibold)).foregroundStyle(LiveInk.accent)
                    .frame(maxWidth: 46)
            } minimal: {
                Circle().fill(LiveInk.accent).frame(width: 9, height: 9)
            }
            .keylineTint(LiveInk.accent)
        }
    }
}
