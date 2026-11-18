# 知了 Windows 版

VPUNote 的 Windows 版：把电脑播放的声音变成双语字幕和实时纪要，存成和 Mac 版同一格式的会议记录，连同会议录音（`system.m4a`，Windows 自带的 AAC 编码），会后在会议记录里回放；同声传译（按住右 Ctrl 说话，没有这个键可以在 设置 › 同声传译 里换一个；翻译后的声音经 VB-Cable 虚拟麦克风进会议）；说话人识别（按声音标出说话人 1、2…，声纹模型是 WeSpeaker ResNet34-LM，CC BY 4.0，用 `scripts/compress-voiceprint.py` 压成 int8，6.7 MB，由系统自带的 Windows ML 在本机运行）。方案和取舍见 [docs/windows-plan.md](../docs/windows-plan.md)。

- 抓声音：WASAPI 进程回环，排除知了自己（Windows 10 2004 起）。
- 识别：Ming（`Ming-3.0-audio-flash`）分段识别，一次请求同时返回语言、原文和译文；需要 Ling key。用不了 Ling 的可以在 设置 › AI 服务 换成 OpenRouter、ZenMux 或兼容 OpenAI 的地址：听交给它能输入音频的模型（同样的提示词，字幕晚 1–2 秒），纪要和翻译用它的文字模型，同传声音用它的声音模型，没有时用 Windows 的声音。
- 改正听错的词：和 Mac 版同样的规则和提示词（读音相近才直接改，拼音用 `src/VPUNote.Core/Data/pinyin.tsv`，由 Apple 的 ICU/CLDR 汉字转拼音一次生成；新名字先问；词表 `vocabulary.json` 和 Mac 同格式）。Ming 一次给出原文和译文，没有单独的文字翻译请求可搭，所以开着时每句话会多发一次文字请求。没有声音也能试：调试管道命令 `silentlisten zh-CN`，再 `heard <一句话>`。
- 纪要、记录格式、提示词、多语言文案都和 Mac 版一致；文案的英日韩翻译直接用 `Resources/*.lproj/Localizable.strings`，Windows 独有的在 `src/VPUNote/Strings/`。
- 界面语言：越南语（默认）、英语、俄语、简体中文、日语、韩语；`Languages.Hidden` 可暂时隐藏某种语言。越南语和俄语的整套文案在 `src/VPUNote/Strings/{vi,ru}.lproj/Windows.strings`（Mac 没有这两种语言）。
- 新版本：启动时和每 4 小时查一次 GitHub Releases（`UpdateChecker`），只认带 Windows 安装包的发布；有新版本时托盘图标出现红点、每个版本发一次通知（正在听时等会议结束），托盘面板和设置 › 关于也会提示。点“安装”由知了自己下载这台电脑对应架构的 `VPUNote-<版本>-Windows-x64.exe` / `-ARM64.exe`（1.1.0 只认 `VPUNote-Setup.exe` 这两个旧名字，发布时一并附上）、对照 `checksums.txt` 校验后安装并重新打开，不经过浏览器，所以 SmartScreen 不拦。开发版只在设了 `VPUNOTE_UPDATE_FEED` 或用 `updatefeed` 调试命令时才查。
- 存到文件夹：设置 › 隐私与启动 › 会后存一份。会议整理好后存成一个 Markdown 文件（文件名和开头的 frontmatter 和 Mac 版一样，`Tests/fixtures/folder-export` 是约定），默认放在最近打开的 Obsidian 库里的 `VPUNote` 文件夹，没有就是 文档\VPUNote；记了哪些写在记录旁边的 `FolderExport.json`。写入先写同一文件夹里的临时文件再改名。写不进去时（Defender 的受控文件夹访问，或文件夹本身的权限）设置里和会议记录里各有一行；文件夹在文档、桌面等受保护的位置时多一个“打开 Windows 安全中心”，这时 Windows 自己会发通知，知了不再发，其他位置会后发一次。调试管道命令：`export folder <路径>`、`export all`、`export`（状态）。
- 分享：会议记录里的“分享…”把纪要（或纪要和双语全文）变成 tbd.edu.vn 上的链接，分享文档和 Mac 版逐字节相同；上传前服务器检查有没有密钥等敏感内容。设备密钥是 Ed25519（.NET 没有自带，`VPUNote.Core/Ed25519.cs` 按 RFC 8032 实现，用 RFC 的测试向量验证），它和每个链接的管理令牌、密码都用 DPAPI 封存在 `share-device.json` 和 `Shares\` 里，不进会议记录、导出和日志。

## 目录

| 路径 | 内容 |
|---|---|
| `src/VPUNote.Core` | 不依赖界面的引擎（`net10.0`，Mac 上也能编译、运行、测试） |
| `src/VPUNote` | Windows 应用：托盘、纪要窗 / 字幕条、会议记录、设置、引导（WPF，代码写界面，不用 XAML） |
| `src/VPUNote.Cli` | 用 WAV 文件跑完整流程、检查会议记录 |
| `tests/VPUNote.Tests` | 单元测试（控制台程序，不用测试框架） |
| `scripts/` | 构建、测试、文案检查、声纹模型转换 |

## 命令

```bash
scripts/install-dotnet.sh          # 一次：.NET 10 SDK 装到 ~/.dotnet，不用 sudo（Mac 上）
scripts/test.sh                    # 单元测试
python3 scripts/strings.py         # 查缺翻译的文案（--write 追加 TODO）
scripts/build.sh                   # 开发版：build/VPUNote-win-x64/、build/VPUNote-win-arm64/
RELEASE=1 scripts/build.sh         # 发布版：build/VPUNote-<版本>-Windows-x64.exe
```

Windows 上可以直接用 `dotnet publish src/VPUNote -c Release -r win-x64 -o out`，GitHub Actions 也是这样构建的（`.github/workflows/vpunote-windows-release.yml`）。

图标 `src/VPUNote/Assets/vpunote.ico` 已提交，构建时不会重新生成；`sample.wav` 由 `Resources/sample/sample.m4a` 生成。

## 验证

- 单元测试：`scripts/test.sh`（或 `dotnet run --project tests/VPUNote.Tests`）。
- 在 Windows 上运行开发版（`VPUNote Dev.cmd`），确认：纪要窗不抢焦点；截图和屏幕共享里看不到纪要窗（除非打开“共享屏幕时让对方看到”）；字幕跟得上。
- 需要真人和真机：麦克风、VB-Cable、蓝牙、真实屏幕共享。

## 设计要点

- 界面语言：越南语（默认）、英语、俄语、简体中文、日语、韩语；`Languages.Hidden` 可暂时隐藏某种语言。
- 文案：中文是键，`Resources/*.lproj/Localizable.strings` 提供英、日、韩译文，`src/VPUNote/Strings/` 放 Windows 独有的文案（越南语、俄语在那里）。
- AI 调用全部经过 `AIClient`；密钥只存在 DPAPI 加密的文件里，不进日志和导出。
