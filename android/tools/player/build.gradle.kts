// A test helper, not shipped: plays a WAV as another app would (USAGE_MEDIA), so the emulator has
// something VPUNote can capture. `adb shell am start -n com.vpupts.vpunote.player/.Play -d file:///sdcard/Music/en.wav`
plugins { id("com.android.application") }
android {
    namespace = "com.vpupts.vpunote.player"
    compileSdk = 36
    defaultConfig { applicationId = "com.vpupts.vpunote.player"; minSdk = 29; targetSdk = 36; versionCode = 1; versionName = "1" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { jvmToolchain(21) }
