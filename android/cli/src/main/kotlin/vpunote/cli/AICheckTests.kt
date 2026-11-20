// AI 服务检查 (AICheck.kt): the Mac's AICheckTests, plus the port's fourth step (hearing) and the
// client's check and passive results against a fake transport. No network.
package vpunote.cli

import vpunote.*
import vpunote.AICheck.Reason
import vpunote.AICheck.State
import vpunote.AICheck.StepID

object AICheckTests {
    val tests: List<Pair<String, () -> Unit>> = listOf(
        "aiCheckEachFailureStopsAtItsStep" to ::classify,
        "aiCheckAFailedStepSkipsTheRestAndAReplyProvesTheOnesBefore" to ::failSkips,
        "aiCheckASuccessClearsAnEarlierFailure" to ::passClears,
        "aiCheckHeardAudioSaysNothingAboutTheTextModel" to ::hearIsNotText,
        "aiCheckBusyPassesAndNeedsNobody" to ::busyPasses,
        "aiCheckKeyPrefixesTellWhoseKeyItIs" to ::prefixes,
        "aiCheckBalanceIsTheSmallerOfKeyLimitAndCredits" to ::balance,
        "aiCheckListedModelsReadsEveryID" to ::listed,
        "aiCheckMessagesNameTheServiceAndTheProxyHintFollowsTheProxy" to ::messages,
        "aiCheckAllFourStepsPassOnAGoodService" to ::clientPasses,
        "aiCheckAModelTheServiceNoLongerListsIsMissing" to ::clientNoModel,
        "aiCheckTheHearCatalogCountsOnlyWhenItSaysWhatHears" to ::clientHearCatalog,
        "aiCheckAManagementKeyIsNeverSent" to ::clientMgmtKey,
        "aiCheckOpenRouterReadsItsBalance" to ::clientBalance,
        "aiCheckUnreachableAsksTheProxyAndTheNetworkLooksAgain" to ::clientUnreachable,
        "aiCheckRealRequestsKeepTheStepsCurrent" to ::clientPassive,
        "aiCheckSimulatedReasonsForScreenshots" to ::simulated,
        "aiCheckOnlyWhatTheUserMustFixIsSaidBeyondSettings" to ::blocking,
    )

    private fun check(ok: Boolean, what: String) { if (!ok) throw AssertionError(what) }
    private fun <T> equal(expected: T, actual: T, what: String) { if (expected != actual) throw AssertionError("$what: expected «$expected», got «$actual»") }
    private fun http(code: Int) = VPUNoteException(VPUNoteException.Kind.Http, code)

    fun classify() {
        equal(AICheck.Failure(StepID.Reach, Reason.Unreachable, null), AICheck.classify(VPUNoteException(VPUNoteException.Kind.Network), StepID.Text), "network")
        equal(AICheck.Failure(StepID.Reach, Reason.Unreachable, null), AICheck.classify(VPUNoteException(VPUNoteException.Kind.Timeout), StepID.Hear), "timeout")
        equal(AICheck.Failure(StepID.Key, Reason.InvalidKey, null), AICheck.classify(VPUNoteException(VPUNoteException.Kind.InvalidKey), StepID.Text), "401")
        equal(AICheck.Failure(StepID.Key, Reason.NoCredit, 402), AICheck.classify(http(402), StepID.Text), "402")
        equal(AICheck.Failure(StepID.Text, Reason.NoModel, 404), AICheck.classify(http(404), StepID.Text), "text 404")
        equal(AICheck.Failure(StepID.Text, Reason.NoModel, 400), AICheck.classify(http(400), StepID.Text), "text 400")
        equal(AICheck.Failure(StepID.Text, Reason.Busy, null), AICheck.classify(VPUNoteException(VPUNoteException.Kind.RateLimited), StepID.Text), "429")
        equal(AICheck.Failure(StepID.Key, Reason.ServerError, 503), AICheck.classify(http(503), StepID.Key), "5xx")
        equal(null, AICheck.classify(VPUNoteException(VPUNoteException.Kind.Hijacked), StepID.Text), "hijacked")
        // The hear model: the same codes mean it takes no audio, not that it is missing.
        for (code in listOf(400, 404, 415, 422)) equal(AICheck.Failure(StepID.Hear, Reason.Deaf, code), AICheck.classify(http(code), StepID.Hear), "hear $code")
        equal(AICheck.Failure(StepID.Text, Reason.ServerError, 415), AICheck.classify(http(415), StepID.Text), "text 415")
        equal(AICheck.Failure(StepID.Hear, Reason.Busy, null), AICheck.classify(VPUNoteException(VPUNoteException.Kind.RateLimited), StepID.Hear), "hear 429")
    }

    fun failSkips() {
        val c = AICheck().fail(StepID.Key, Reason.InvalidKey)
        equal(State.Ok(), c[StepID.Reach], "reach proven")
        equal(State.Fail(Reason.InvalidKey), c[StepID.Key], "key")
        equal(State.Skipped, c[StepID.Text], "text skipped")
        equal(State.Skipped, c[StepID.Hear], "hear skipped")
        equal(StepID.Key, c.failure?.step, "failure")
        equal(State.Untested, AICheck().fail(StepID.Key, Reason.MgmtKey, reached = false)[StepID.Reach], "nothing sent")
    }

    fun passClears() {
        val c = AICheck().fail(StepID.Reach, Reason.Unreachable).copy(proxy = AICheck.ProxyHint.None).pass(StepID.Text, 1.5)
        equal(null, c.failure, "cleared")
        check(c[StepID.Reach] == State.Ok() && c[StepID.Text] == State.Ok(1.5), "proven")
        equal(State.Untested, c[StepID.Hear], "hearing open again")
        equal(null, c.proxy, "no proxy hint")
    }

    fun hearIsNotText() {
        // Heard audio proves the address, the key and the hear model, never the text model.
        val c = AICheck().fail(StepID.Reach, Reason.Unreachable).pass(StepID.Hear, 0.9)
        equal(State.Untested, c[StepID.Text], "text unproven")
        equal(State.Ok(0.9), c[StepID.Hear], "hear")
        // A deaf reply doesn't make the text model look fine either, nor clear its own failure.
        val d = AICheck().fail(StepID.Hear, Reason.Deaf, 400)
        equal(State.Untested, d[StepID.Text], "text still unproven")
        val m = AICheck().fail(StepID.Text, Reason.NoModel, 404).pass(StepID.Hear, 1.0)
        equal(Reason.NoModel, m.failure?.reason, "a missing text model stays missing")
    }

    fun blocking() {
        // The Mac's AIServiceStrip.blocking, plus the port's deaf hear model: a key, the credit, a
        // model or the settings. The network, a busy service and a server error fix themselves.
        val needs = setOf(Reason.InvalidKey, Reason.MgmtKey, Reason.NoCredit, Reason.NoModel, Reason.NoJSON, Reason.BadAddress, Reason.Deaf)
        for (r in Reason.values()) {
            val b = AICheck.simulated(r).blocking("Ling", "openai/gpt-x", "google/omni-y")
            equal(r in needs, b != null, "blocking $r")
        }
        equal(null, AICheck().blocking("Ling", "t", "h"), "nothing known yet")
        equal("Ling 的 key 用不了" to "修好之前听不了", AICheck.simulated(Reason.InvalidKey).blocking("Ling", "t", "h"), "key")
        equal("ZenMux 上找不到 gpt-x 这个模型。" to "字幕照常，纪要先停下", AICheck.simulated(Reason.NoModel).blocking("ZenMux", "openai/gpt-x", "h"), "short model, notes only")
        equal("omni-y 听不了声音。", AICheck.simulated(Reason.Deaf).blocking("ZenMux", "t", "google/omni-y")?.first, "deaf")
        // A heard segment after a bad key clears it: back to normal.
        equal(null, AICheck.simulated(Reason.InvalidKey).pass(StepID.Hear, 1.0).blocking("Ling", "t", "h"), "fixed")
    }

    fun busyPasses() {
        val c = AICheck().pass(StepID.Text, null).with(StepID.Text, State.Busy).copy(at = 1)
        check(c.passed && c.busy && !c.needsUser, "busy passes")
    }

    fun prefixes() {
        equal("openrouter" to false, AICheck.owner("sk-or-v1-abc"), "OpenRouter")
        equal("zenmux" to false, AICheck.owner(" sk-payg-abc\n"), "ZenMux")
        equal("zenmux" to true, AICheck.owner("sk-mgmt-abc"), "management")
        equal(null, AICheck.owner("sk-abc"), "Ling or anyone")
    }

    fun balance() {
        val key = Json.parseObject("""{"data":{"limit_remaining":1.72}}""")
        val credits = Json.parseObject("""{"data":{"total_credits":10,"total_usage":3.92}}""")
        equal(1.72, AICheck.balance(key, credits), "smaller")
        check(Math.abs(AICheck.balance(Json.parseObject("""{"data":{"limit_remaining":null}}"""), credits)!! - 6.08) < 0.001, "no key limit")
        equal(1.72, AICheck.balance(key, null), "key only")
        equal(null, AICheck.balance(null, null), "nothing")
        val c = AICheck(balance = 0.3)
        check(c.lowBalance && c.needsUser, "low")
        check(c.balanceLine("OpenRouter")!!.contains("0.30"), "says how much")
    }

    fun listed() {
        equal(setOf("inclusionai/ling-3.0-flash", "qwen/qwen3.8-flash"),
              AICheck.listedModels("""{"data":[{"id":"inclusionai/ling-3.0-flash"},{"id":"qwen/qwen3.8-flash"}]}"""), "ids")
        equal(null, AICheck.listedModels("{}"), "none")
        equal(null, AICheck.listedModels("""{"data":[]}"""), "empty")
        equal(setOf("q/omni"), AICheck.listedHearing("""{"data":[{"id":"q/omni","input_modalities":["text","audio"]},{"id":"q/text","architecture":{"input_modalities":["text"]}}]}"""), "audio in")
        equal(null, AICheck.listedHearing("""{"data":[{"id":"qwen3:14b"}]}"""), "says nothing about modalities")
    }

    fun messages() {
        var c = AICheck().fail(StepID.Reach, Reason.Unreachable)
        check(c.message("ZenMux", "zenmux.ai", "m", "h")!!.contains("zenmux.ai"), "host")
        equal(null, c.proxyLine("ZenMux", "zenmux.ai"), "no hint before it's known")
        c = c.copy(proxy = AICheck.ProxyHint.SystemOn)
        check(c.proxyLine("ZenMux", "zenmux.ai")!!.contains("zenmux.ai"), "proxy on: the host")
        c = c.copy(proxy = AICheck.ProxyHint.None)
        check(c.proxyLine("ZenMux", "zenmux.ai")!!.contains("ZenMux"), "no proxy: the service")
        check(AICheck().fail(StepID.Text, Reason.NoModel, 404).message("ZenMux", "zenmux.ai", "x/y", "h")!!.contains("x/y"), "text model")
        check(AICheck().fail(StepID.Hear, Reason.Deaf, 400).message("ZenMux", "zenmux.ai", "x/y", "a/deaf")!!.contains("a/deaf"), "hear model")
        equal(null, AICheck().fail(StepID.Key, Reason.InvalidKey).copy(proxy = AICheck.ProxyHint.None).proxyLine("ZenMux", "zenmux.ai"), "not the network")
    }

    // MARK: The client

    private class Fake(val respond: (String, String) -> Transport.Response) : Transport {
        val sent = mutableListOf<String>()
        override fun send(url: String, method: String, body: String?, headers: Map<String, String>, timeoutMs: Int): Transport.Response {
            synchronized(sent) { sent.add(url) }
            return respond(url, body ?: "")
        }
    }

    private fun reply(content: String) = Transport.Response(200, Json.write(mapOf(
        "choices" to listOf(mapOf("message" to mapOf("content" to content), "finish_reason" to "stop"))), pretty = false))

    private val MODELS = Json.write(mapOf("data" to listOf(
        mapOf("id" to "inclusionai/ling-3.0-flash", "input_modalities" to listOf("text"), "output_modalities" to listOf("text")),
        mapOf("id" to "qwen/qwen3.8-omni-flash", "input_modalities" to listOf("text", "audio"), "output_modalities" to listOf("text")),
        mapOf("id" to "x/text-only", "input_modalities" to listOf("text"), "output_modalities" to listOf("text")))), pretty = false)

    private fun service(url: String, body: String) = when {
        url.endsWith("models") -> Transport.Response(200, MODELS)
        "input_audio" in body -> reply("NONE")
        else -> reply("OK")
    }

    private fun makeClient(fake: Transport, s: AppSettings, key: String = "sk-payg-test-0000"): VPUNoteClient {
        val k = AIKey(PlainProtector, service = { s.aiProvider })
        k.remove()
        k.save(key)
        return VPUNoteClient(k, fake, config = s::aiConfig)
    }

    fun clientPasses() {
        val client = makeClient(Fake(::service), AppSettings(aiProvider = "zenmux"))
        equal(KeyState.Ok, client.check(), "ok")
        val c = client.aiCheck
        check(c.passed && c.failure == null, "passed: ${c.summary}")
        check(c.steps.all { AICheck.isOk(it.state) }, "every step: ${c.summary}")
        check((c[StepID.Text] as State.Ok).seconds != null && (c[StepID.Hear] as State.Ok).seconds != null, "timed")
        equal(true, client.hears, "hears")
    }

    fun clientNoModel() {
        val s = AppSettings(aiProvider = "zenmux", aiModels = mutableMapOf("zenmux" to "gone/model"))
        val client = makeClient(Fake(::service), s)
        client.check()
        equal(AICheck.Failure(StepID.Text, Reason.NoModel, null), client.aiCheck.failure, "absent from /models")
        equal(State.Skipped, client.aiCheck[StepID.Hear], "hearing not reached")
        equal(KeyState.Ok, client.keyState, "the key itself works")
    }

    fun clientHearCatalog() {
        // Answers the silent clip, but the service says it takes no audio in.
        val s = AppSettings(aiProvider = "zenmux", aiHearModels = mutableMapOf("zenmux" to "x/text-only"))
        val client = makeClient(Fake(::service), s)
        client.check()
        equal(Reason.Deaf, client.aiCheck.failure?.reason, "deaf by the catalog")
        equal(false, client.hears, "can't hear")
        // A custom service that lists models without saying what they take: the probe decides.
        val custom = AppSettings(aiProvider = "custom", aiCustomBase = "https://x.example/v1", aiModels = mutableMapOf("custom" to "qwen3:14b"),
                                 aiHearModels = mutableMapOf("custom" to "qwen3-omni"))
        val plain = makeClient(Fake { url, body -> if (url.endsWith("models")) Transport.Response(200, """{"data":[{"id":"qwen3:14b"},{"id":"qwen3-omni"}]}""") else service(url, body) }, custom, key = "sk-x-0000")
        plain.check()
        check(plain.aiCheck.passed && plain.aiCheck.failure == null, "passes: ${plain.aiCheck.summary}")
    }

    fun clientMgmtKey() {
        val fake = Fake(::service)
        val client = makeClient(fake, AppSettings(aiProvider = "zenmux"), key = "sk-mgmt-0000")
        equal(KeyState.Invalid, client.check(), "refused")
        equal(Reason.MgmtKey, client.aiCheck.failure?.reason, "management")
        equal(State.Untested, client.aiCheck[StepID.Reach], "nothing sent")
        check(fake.sent.isEmpty(), "no request")
    }

    fun clientBalance() {
        val fake = Fake { url, body ->
            when {
                url.endsWith("/key") -> Transport.Response(200, """{"data":{"limit_remaining":0.4}}""")
                url.endsWith("/credits") -> Transport.Response(200, """{"data":{"total_credits":5,"total_usage":1}}""")
                url.endsWith("models") -> Transport.Response(200, """{"data":[{"id":"inclusionai/ling-3.1-flash","architecture":{"input_modalities":["text"]}},{"id":"qwen/qwen3.8-omni-flash","architecture":{"input_modalities":["audio","text"]}}]}""")
                else -> service(url, body)
            }
        }
        val client = makeClient(fake, AppSettings(aiProvider = "openrouter"), key = "sk-or-v1-0000")
        client.check()
        equal(0.4, client.aiCheck.balance, "the smaller")
        check(client.aiCheck.passed && client.aiCheck.lowBalance && client.aiCheck.needsUser, "passed, but low: ${client.aiCheck.summary}")
    }

    fun clientUnreachable() {
        var down = true
        val client = makeClient(Fake { url, body -> if (down) throw java.io.IOException("no route") else service(url, body) }, AppSettings(aiProvider = "zenmux"))
        client.proxyHint = { AICheck.ProxyHint.None }
        equal(KeyState.Unreachable, client.check(), "unreachable")
        equal("reach=fail(unreachable) key=skipped text=skipped hear=skipped proxy=none", client.aiCheck.summary.substringBefore(" passive"), "steps")
        check(client.aiCheck.proxyLine(client.serviceName, client.serviceHost) != null, "the hint")
        down = false
        client.networkChanged()
        check(client.aiCheck.passed, "looked again: ${client.aiCheck.summary}")
        // Only after an unreachable result: a good service isn't checked on every network change.
        val calls = (client.aiCheck.at)
        client.networkChanged()
        equal(calls, client.aiCheck.at, "no check")
    }

    fun clientPassive() {
        var answer: (String) -> Transport.Response = { reply("OK") }
        val client = makeClient(Fake { url, body -> if (url.endsWith("models")) Transport.Response(200, MODELS) else answer(body) }, AppSettings(aiProvider = "zenmux"))
        client.check()
        // Heard audio in a meeting: the hear step, from a real request.
        answer = { Transport.Response(400, "{}") }
        try { client.chat("c", "i", Priority.Translate, model = "qwen/qwen3.8-omni-flash", audio = byteArrayOf(1)) } catch (_: VPUNoteException) {}
        equal(Reason.Deaf, client.aiCheck.failure?.reason, "deaf from a meeting")
        check(client.aiCheck.passive, "passive")
        // The notes: a 429 is busy, not a failure; then a good reply clears it.
        answer = { Transport.Response(429, "{}") }
        try { client.chat("c", "i", Priority.Translate) } catch (_: VPUNoteException) {}
        answer = { reply("fine") }
        client.chat("c", "i", Priority.Notes) // waits out the pause after the 429
        equal(State.Ok::class, client.aiCheck[StepID.Text]::class, "text proven")
        // The network goes in the middle of a meeting.
        answer = { throw java.io.IOException("down") }
        try { client.chat("c", "i", Priority.Translate) } catch (_: VPUNoteException) {}
        equal(Reason.Unreachable, client.aiCheck.failure?.reason, "unreachable")
        // A server error once isn't worth repainting the settings for.
        answer = { reply("back") }
        client.chat("c", "i", Priority.Translate)
        answer = { Transport.Response(503, "{}") }
        try { client.chat("c", "i", Priority.Translate) } catch (_: VPUNoteException) {}
        check(client.aiCheck.failure?.reason != Reason.ServerError, "ignored")
    }

    fun simulated() {
        for (r in Reason.values()) {
            val c = AICheck.simulated(r)
            if (r == Reason.Busy) check(c.busy && c.failure == null, "busy") else equal(r, c.failure?.reason, "$r")
            equal(r, AICheck.reasonNamed(AICheck.lower(r.name)), "named ${r.name}")
        }
        equal(Reason.NoJSON, AICheck.reasonNamed("noJSON"), "noJSON")
    }
}
