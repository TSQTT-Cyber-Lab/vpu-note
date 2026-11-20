// 读出来的声音 (Voices.kt): the catalog, default and fallback rules, and what goes to the speech model.
// No network: VPUNoteClient runs against a fake transport.
package vpunote.cli

import vpunote.*
import java.io.ByteArrayInputStream

object VoiceTests {
    val tests: List<Pair<String, () -> Unit>> = listOf(
        "voiceCatalogIsTheServicesAndThePhones" to ::catalog,
        "voiceDefaultIsTheServicesFirstElseThePhones" to ::defaults,
        "voiceAChoiceTheServiceCantSpeakFallsBack" to ::fallback,
        "voiceChoicesSurviveARestart" to ::settingsRoundTrip,
        "voiceSpeechSendsTheChosenVoice" to ::speechBody,
    )

    private fun check(ok: Boolean, what: String) { if (!ok) throw AssertionError(what) }
    private fun <T> equal(expected: T, actual: T, what: String) { if (expected != actual) throw AssertionError("$what: expected «$expected», got «$actual»") }

    private val gemini = AppSettings().aiConfig()
    private val openrouter = AppSettings(aiProvider = "openrouter").aiConfig()

    fun catalog() {
        equal(listOf("phone"), VoiceCatalog.options(gemini).map { it.id }, "Gemini has no speech model here: the phone's voice")
        equal(listOf("cloud:Kore", "cloud:Aoede", "cloud:Charon", "cloud:Puck", "phone"), VoiceCatalog.options(openrouter).map { it.id }, "OpenRouter's")
        equal(VoiceCatalog.options(openrouter), VoiceCatalog.options(openrouter), "same list for every language")
        val noVoice = AppSettings(aiProvider = "custom", aiCustomBase = "https://x.example/v1", aiModels = mutableMapOf("custom" to "m")).aiConfig()
        equal(listOf("phone"), VoiceCatalog.options(noVoice).map { it.id }, "no speech model")
        equal("中文", VoiceCatalog.title("zh-CN"), "one Chinese key")
        equal("en", VoiceCatalog.key(""), "empty code is English")
    }

    fun defaults() {
        equal("phone", VoiceCatalog.defaultId(gemini, speaks = true), "Gemini: the phone's")
        equal("cloud:Kore", VoiceCatalog.defaultId(openrouter, speaks = true), "OpenRouter's first")
        equal("phone", VoiceCatalog.defaultId(openrouter, speaks = false), "no key: the phone's")
    }

    fun fallback() {
        val chosen = mapOf("en" to "cloud:Aoede", "zh" to "phone")
        equal("cloud:Aoede", VoiceCatalog.chosen(chosen, "en", openrouter, speaks = true), "kept")
        equal("phone", VoiceCatalog.chosen(chosen, "zh-CN", openrouter, speaks = true), "the phone's, chosen")
        equal("phone", VoiceCatalog.chosen(chosen, "en", gemini, speaks = true), "OpenRouter's voice after switching to Gemini")
        equal("cloud:Aoede", VoiceCatalog.chosen(chosen, "en", openrouter, speaks = true), "and back with OpenRouter")
        equal("phone", VoiceCatalog.chosen(chosen, "en", openrouter, speaks = false), "no key: the phone's")
        equal("cloud:Kore", VoiceCatalog.chosen(emptyMap(), "en-US", openrouter, speaks = true), "never chosen")
        equal("cloud:Kore", VoiceCatalog.chosen(mapOf("en" to "clone"), "en", openrouter, speaks = true), "a voice the app no longer offers falls back")
    }

    fun settingsRoundTrip() {
        val s = AppSettings()
        s.speakVoices["en"] = "cloud:Aoede"
        s.speakVoices["zh"] = "phone"
        s.save()
        equal(mapOf("en" to "cloud:Aoede", "zh" to "phone"), AppSettings.load().speakVoices.toMap(), "speakVoices")
        check(Json.write(s.toJson()).contains("\"speakVoices\""), "the Mac's key")
    }

    fun speechBody() {
        val bodies = mutableListOf<String>()
        val fake = object : Transport {
            override fun send(url: String, method: String, body: String?, headers: Map<String, String>, timeoutMs: Int) = Transport.Response(200, "{}")
            override fun stream(url: String, body: String, headers: Map<String, String>, timeoutMs: Int): Transport.Stream {
                bodies.add(body)
                val chunk = java.util.Base64.getEncoder().encodeToString(byteArrayOf(1, 0))
                return Transport.Stream(200, "text/event-stream", ByteArrayInputStream("data: {\"audio\": \"$chunk\"}\ndata: {\"type\": \"speech.audio.done\"}".toByteArray()))
            }
        }
        val s = AppSettings(aiProvider = "openrouter")
        val k = AIKey(PlainProtector, service = { s.aiProvider })
        k.remove(); k.save("sk-or-test-0000000000")
        val client = VPUNoteClient(k, fake, config = s::aiConfig)
        var got = 0
        client.speech("Hello", voice = "Aoede") { got += it.size }
        check(got > 0, "some audio came back")
        val b = Json.parse(bodies[0]).obj()!!
        equal("Aoede", b["voice"], "the chosen voice is sent")
        check("language_id" !in b, "no language_id outside Ming")
    }
}
