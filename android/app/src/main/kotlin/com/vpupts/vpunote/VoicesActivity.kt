package com.vpupts.vpunote

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import vpunote.Strings
import vpunote.Strings.L
import vpunote.VoiceCatalog
import vpunote.VoiceOption

/// 读出来的声音 (Settings › 我说 · 面对面): the Mac's voice picker (VoicePicker.swift) as a page.
/// Each language remembers its own voice (`speakVoices`); the list is the service's voices for the
/// language (the service's speech voices), then the phone's, each with 试听. Opened on the language they speak
/// now (the `id` extra); the pill switches language.
class VoicesActivity : Activity() {
    private lateinit var ui: Ui
    private lateinit var page: LinearLayout
    private var unobserve: (() -> Unit)? = null
    private var shown: String? = null
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = Ui(this)
        language = intent.getStringExtra("id") ?: vpunote.listen.theirLanguage
        page = ui.stack(0).apply { setPadding(ui.dp(16), ui.dp(4), ui.dp(16), ui.dp(24)) }
        setContentView(ui.screen(this, L("读出来的声音"), back = true, ground = Palette.ground, body = page))
    }

    override fun onResume() { super.onResume(); unobserve = vpunote.observe(::build); shown = null; build() }
    override fun onPause() { super.onPause(); unobserve?.invoke(); unobserve = null }

    private val now get() = vpunote.listen.theirLanguage

    private fun build() {
        val app = vpunote
        val client = app.client
        val chosen = app.voiceId(language)
        val key = listOf(language, chosen, app.previewing, client.keyState, client.provider.id, client.speaks, app.settings.speakVoices, Strings.language).joinToString("|")
        if (key == shown) return
        shown = key
        page.removeAllViews()

        // "每种语言各记一个声音" for the language they speak now, "对方说…时用" for another, and the pill.
        val current = VoiceCatalog.key(language) == VoiceCatalog.key(now)
        val head = ui.row(10,
            ui.helper(if (current) L("每种语言各记一个声音") else Strings.LF("对方说%@时用", VoiceCatalog.title(language))).apply { layoutParams = ui.weight() },
            pill(VoiceCatalog.title(language) + if (current) L("（当前）") else ""))
        head.setPadding(ui.dp(4), ui.dp(4), 0, ui.dp(10))
        page.addView(head)

        val rows = VoiceCatalog.options(client.config()).map { voiceRow(it, chosen) }
        page.addView(ui.group(rows))
        page.addView(ui.helper(footnote(), Palette.tertiary).apply { setPadding(ui.dp(4), ui.dp(10), ui.dp(4), 0) })
    }

    /// The language menu: the Mac's tabs, the one they speak now marked.
    private fun pill(label: String) = ui.row(6,
        ui.text(label, 14f, Palette.ink, medium = true, lines = 1),
        ui.icon(R.drawable.i_chevron_down, 16, Palette.tertiary)).apply {
        setPadding(ui.dp(14), ui.dp(8), ui.dp(12), ui.dp(8))
        minimumHeight = ui.dp(40)
        contentDescription = label
        ui.tappable(this, 20, ui.rounded(Palette.card, 20, stroke = Palette.line)) {
            val nowKey = VoiceCatalog.key(now)
            ui.choose(this@VoicesActivity, L("读出来的声音"),
                      VoiceCatalog.languages.map { (k, t) -> k to t + if (k == nowKey) L("（当前）") else "" },
                      VoiceCatalog.key(language)) { language = if (it == "zh") "zh-CN" else it; shown = null; build() }
        }
    }

    private fun voiceRow(v: VoiceOption, chosen: String): View {
        val app = vpunote
        val selected = v.id == chosen
        val usable = v.id == VoiceCatalog.PHONE || app.client.speaks
        val row = ui.listRow(name(v), if (usable) VoicesActivity.summary(v) else L("需要 key"),
                             leading = playButton(v.id, selected, usable), trailing = radio(selected), chevron = false) {
            if (!usable) return@listRow
            app.settings.speakVoices[VoiceCatalog.key(language)] = v.id
            app.saveSettings()
            shown = null; build()
        }
        if (selected) row.background = ui.ripple(GradientDrawable().apply { setColor(Palette.accentPale) })
        if (!usable) row.alpha = 0.5f
        row.isSelected = selected
        return row
    }

    /// 试听: a round play button (accent when the voice is the chosen one), a spinner while it plays.
    private fun playButton(id: String, highlighted: Boolean, enabled: Boolean): View {
        val app = vpunote
        val playing = app.previewing == id
        return FrameLayout(this).apply {
            contentDescription = L("试听")
            background = ui.rounded(if (highlighted) Palette.accent else Palette.ground, 18)
            if (playing) addView(ProgressBar(context, null, android.R.attr.progressBarStyleSmall).apply {
                indeterminateTintList = ColorStateList.valueOf(if (highlighted) Palette.ink else Palette.accent)
            }, FrameLayout.LayoutParams(ui.dp(18), ui.dp(18), Gravity.CENTER))
            else addView(ui.icon(R.drawable.i_play, 16, Palette.ink), FrameLayout.LayoutParams(ui.dp(16), ui.dp(16), Gravity.CENTER))
            layoutParams = LinearLayout.LayoutParams(ui.dp(36), ui.dp(36))
            if (enabled && !playing) ui.tappable(this, 18, background) {
                app.previewVoice(id, language) { android.widget.Toast.makeText(this@VoicesActivity, it, android.widget.Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun radio(on: Boolean) = FrameLayout(this).apply {
        background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setStroke(ui.dp(2), if (on) Palette.accent else Palette.outline) }
        if (on) addView(ui.dot(Palette.accent, 10), FrameLayout.LayoutParams(ui.dp(10), ui.dp(10), Gravity.CENTER))
        // listRow sizes its trailing view by content: an empty ring needs its own size.
        minimumWidth = ui.dp(20); minimumHeight = ui.dp(20)
        layoutParams = LinearLayout.LayoutParams(ui.dp(20), ui.dp(20))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private fun footnote(): String {
        val client = vpunote.client
        return if (client.speaks) Strings.LF("%@ 用不了时，自动换手机的声音", client.provider.name) else L("这个服务不出声音，用手机的声音")
    }

    companion object {
        /// The name a voice shows by (the phone's and the user's own are UI text).
        fun name(v: VoiceOption): String = if (v.id == VoiceCatalog.PHONE) L(v.name) else v.name

        /// "女 · 美式" / "本机 · 最快": who it is and where it comes from.
        fun summary(v: VoiceOption): String = when {
            v.id == VoiceCatalog.PHONE -> listOf(L("本机"), L("最快")).joinToString(" · ")
            else -> v.tags.map { L(it) }.joinToString(" · ")
        }
    }
}
