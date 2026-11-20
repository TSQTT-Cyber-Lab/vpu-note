package vpunote

/// A voice 我说 can read with (Voices.swift's VoiceOption): one of the AI service's speech voices,
/// or the phone's own text-to-speech. `id` is what settings keep: "cloud:Kore", "phone".
data class VoiceOption(val id: String, val name: String, /** Chinese UI keys, localized where shown. */ val tags: List<String>)

/// Which voices to offer for each language the other side may hear (VoiceCatalog in Voices.swift).
/// The phone's own voice follows, so there is always one without a key.
object VoiceCatalog {
    const val PHONE = "phone"
    const val CLOUD = "cloud:"

    /// The picker's languages (key, title), as on the Mac. The key is the code without region.
    val languages = listOf("en" to "English", "zh" to "中文", "ja" to "日本語", "ko" to "한국어",
                           "es" to "Español", "fr" to "Français", "de" to "Deutsch", "pt" to "Português")

    fun key(code: String): String = code.substringBefore('-').ifEmpty { "en" }

    fun title(code: String): String = languages.firstOrNull { it.first == key(code) }?.second ?: code

    /// The service's own speech voices. They are the same in every language; a service with no speech
    /// model offers none.
    fun remote(config: AIConfig): List<VoiceOption> =
        if (!config.hasVoices) emptyList()
        else config.voices.map { VoiceOption(CLOUD + it.id, it.id, listOf(it.gender).filter { g -> g.isNotEmpty() }) }

    val phone = VoiceOption(PHONE, "手机的声音", listOf("本机"))

    fun options(config: AIConfig): List<VoiceOption> = remote(config) + phone

    /// What a language uses until the user picks: the service's first voice once it can speak,
    /// else the phone's (the Mac's defaultID).
    fun defaultId(config: AIConfig, speaks: Boolean): String =
        (if (speaks) remote(config).firstOrNull()?.id else null) ?: PHONE

    /// The chosen voice for a language, or its default (AppModel.voiceID). A choice the service in
    /// use can't speak (any after the key is gone, or after switching to a service without voices)
    /// falls back to the default, and comes back with that service.
    fun chosen(voices: Map<String, String>, language: String, config: AIConfig, speaks: Boolean): String {
        val fallback = defaultId(config, speaks)
        val id = voices[key(language)] ?: return fallback
        return when {
            id == PHONE -> id
            speaks && remote(config).any { it.id == id } -> id
            else -> fallback
        }
    }

    fun option(id: String, config: AIConfig): VoiceOption? = options(config).firstOrNull { it.id == id }

    /// A short line to try a voice with, in the language it will speak.
    fun sample(language: String): String = when (key(language)) {
        "zh" -> "谢谢大家。我们来看一下上线时间表，并确定谁负责麦克风测试。"
        "ja" -> "皆さん、ありがとうございます。発売スケジュールを確認しましょう。"
        "ko" -> "감사합니다. 출시 일정을 함께 확인해 보겠습니다."
        "es" -> "Gracias a todos. Revisemos el calendario de lanzamiento."
        "fr" -> "Merci à tous. Regardons ensemble le calendrier de lancement."
        "de" -> "Danke an alle. Schauen wir uns den Zeitplan für den Start an."
        "pt" -> "Obrigado a todos. Vamos ver o cronograma de lançamento."
        else -> "Thanks everyone. Let's look at the launch timeline and decide who owns the microphone tests."
    }
}
