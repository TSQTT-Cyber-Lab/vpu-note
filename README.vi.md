<p align="center">
  <img src="windows/src/VPUNote/Assets/logo.png" width="220" alt="Victoria Pacific University">
</p>

<h1 align="center">VPUNote</h1>

<p align="center">
  Phụ đề song ngữ trực tiếp, biên bản cuộc họp tự động và phiên dịch thời gian thực cho các cuộc họp trực tuyến.<br>
  Dành cho Windows 10/11 64-bit.
</p>

<p align="center"><a href="README.md">English</a> · <a href="README.vi.md">Tiếng Việt</a> · <a href="README.zh-CN.md">简体中文</a> · <a href="README.ja.md">日本語</a> · <a href="README.ko.md">한국어</a></p>

## VPUNote làm được gì

VPUNote là một ứng dụng chạy **trên máy của bạn**, đứng cạnh phần mềm họp (Zoom, Microsoft Teams, Google Meet, Webex, Tencent Meeting…), không thay thế nó. Có hai việc chính:

| Việc | Cách làm |
|---|---|
| **Hiểu người khác nói** | Nghe âm thanh phát ra từ máy bạn, hiện phụ đề dịch ngay trên màn hình của bạn và viết biên bản cuộc họp (tóm tắt, chủ đề, kết luận, việc cần làm). |
| **Nói để người khác hiểu** | Bạn giữ một phím và nói tiếng mẹ đẻ. VPUNote dịch sang ngôn ngữ của họ và phát giọng đọc vào cuộc họp. |

Ngôn ngữ giao diện: Tiếng Việt (mặc định), English, Русский, 简体中文, 日本語, 한국어. Ngôn ngữ nghe và dịch gồm cả sáu ngôn ngữ trên.

## Người khác tham gia như thế nào?

**Họ không cần làm gì cả.** VPUNote không có phòng họp riêng, không có mã tham gia và không có tài khoản. Cuộc họp vẫn diễn ra trên Zoom/Teams/Meet như bình thường, còn VPUNote chỉ giúp riêng bạn.

```
        Đối tác (nói tiếng Anh)
                │  qua Zoom / Teams / Meet
                ▼
   ┌──────── Máy của bạn ─────────┐
   │ Loa ──► VPUNote nghe ──► phụ đề và biên bản tiếng Việt (chỉ bạn thấy)
   │                              │
   │ Bạn nói tiếng Việt, giữ Ctrl phải
   │   └─► VPUNote dịch ─► giọng tiếng Anh ─► micro ảo (VB-Cable)
   └──────────────┬───────────────┘
                  ▼  Zoom/Teams/Meet gửi micro ảo đi như micro của bạn
        Đối tác nghe giọng tiếng Anh trên thiết bị của họ
```

1. **Bạn nghe đối tác:** đối tác nói tiếng Anh, âm thanh phát ra loa máy bạn, VPUNote hiện phụ đề tiếng Việt trên màn hình của bạn.
2. **Đối tác nghe bạn:** bạn giữ **Ctrl phải** và nói tiếng Việt, VPUNote dịch sang tiếng Anh rồi phát giọng đọc vào **micro ảo VB-Cable**. Trong Zoom/Teams, bạn chọn micro ảo này làm micro. Phần mềm họp gửi giọng tiếng Anh đó đi, nên đối tác nghe nó **trên thiết bị của chính họ, qua phần mềm họp**.

### Có phải đăng nhập hay xác thực gì không?

- **Trong VPUNote: không.** Không có tài khoản, không có mã cuộc họp. Thứ duy nhất bạn nhập là **API key** của dịch vụ AI (xem bên dưới), được lưu trên máy bạn, mã hoá bằng tài khoản Windows.
- **Trong cuộc họp:** việc mời người, mật khẩu hay phòng chờ do Zoom/Teams/Meet quản lý như bình thường.
- **Đối tác cần cài gì?** Không cần gì. Họ chỉ nghe giọng bạn qua phần mềm họp.

### Đối tác có xem được phụ đề của tôi không?

Không tự động. Phụ đề và biên bản chỉ hiện trên máy bạn. Muốn họ xem, bạn có thể **chia sẻ màn hình** (xem mục "Cửa sổ ghi chú và chia sẻ màn hình" bên dưới) hoặc gửi biên bản sau cuộc họp. Bản Windows hiện không có tính năng chia sẻ thành đường dẫn.

## Cài đặt trên Windows

**Yêu cầu:** Windows 10 (bản 2004 trở lên) hoặc Windows 11, 64-bit; có internet.

1. Mở trang [Releases](https://github.com/TSQTT-Cyber-Lab/vpu-note/releases/latest) và tải file `VPUNote-<phiên bản>-Windows-x64.exe` (máy dùng chip Snapdragon/ARM64 tải bản `-Windows-ARM64.exe` nếu có). Bạn có thể đối chiếu mã SHA-256 với file `checksums.txt` ở cùng trang:
   ```powershell
   Get-FileHash .\VPUNote-*-Windows-x64.exe -Algorithm SHA256
   ```
2. Chép file ra ổ đĩa của máy (ví dụ màn hình nền), **không chạy trực tiếp từ thư mục mạng**.
3. Bấm đúp để chạy. Ứng dụng chưa được ký số nên Windows SmartScreen có thể chặn: bấm **More info** (Thông tin thêm), rồi **Run anyway** (Vẫn chạy).
4. Trình cài đặt sẽ cài VPUNote cho **tài khoản của bạn**, không cần quyền quản trị, rồi mở ứng dụng. Từ đó bạn mở VPUNote từ menu Start, hoặc từ biểu tượng ở khay hệ thống (góc phải thanh tác vụ, có thể nằm trong mũi tên `^`).

| Nội dung | Vị trí |
|---|---|
| Chương trình | `%LOCALAPPDATA%\Programs\VPUNote\VPUNote.exe` |
| Dữ liệu (cài đặt, key, biên bản họp) | `%APPDATA%\VPUNote` |

**Nâng cấp:** tải bản mới và chạy; nếu bản mới hơn, trình cài đặt sẽ hỏi có thay thế bản đang cài không. Biên bản và cài đặt được giữ nguyên. Nếu báo lỗi ghi đè file, hãy thoát VPUNote ở khay hệ thống rồi chạy lại.

**Gỡ cài đặt:** Settings › Apps › Installed apps › VPUNote › Uninstall. Biên bản họp mặc định được giữ lại.

## Thiết lập dịch vụ AI (bắt buộc, một lần)

VPUNote gửi âm thanh và văn bản tới một dịch vụ AI để nhận dạng giọng nói, dịch và viết biên bản. Bạn cần một API key.

| Dịch vụ | Cách lấy key | Ghi chú |
|---|---|---|
| **Google Gemini** | Tạo key tại [aistudio.google.com/apikey](https://aistudio.google.com/apikey) (key dạng `AQ.…` hoặc `AIza…`) | Có gói miễn phí (giới hạn lượt gọi). Không có giọng đọc AI, dùng giọng có sẵn của Windows. |
| **OpenRouter** | Tạo key tại [openrouter.ai/settings/keys](https://openrouter.ai/settings/keys) (dạng `sk-or-…`) | Trả phí theo lượng dùng. Có giọng đọc AI tự nhiên. |
| **Khác (tương thích OpenAI)** | Nhập địa chỉ và key của dịch vụ của bạn | Dịch vụ cần nhận được âm thanh đầu vào. Máy chủ trong mạng nội bộ có thể không cần key. |

Cách nhập: ở bước đầu của hướng dẫn khi mở lần đầu, hoặc vào **Cài đặt › Dịch vụ AI**, chọn dịch vụ, dán key rồi bấm **Lưu và kiểm tra**. Khi cả bốn mục (kết nối, key, biên bản và dịch, nghe bên kia) có dấu ✓ là dùng được. VPUNote tự chọn model phù hợp mà key của bạn dùng được.

Khi chưa có key hoặc AI chưa dùng được, phần **dịch văn bản** vẫn chạy bằng Google Dịch (miễn phí); nhận dạng giọng nói và biên bản thì cần AI.

## Hướng dẫn dùng khi họp

### Chỉ để hiểu người khác nói (không cần micro ảo)

1. Bấm **Bắt đầu nghe** ở biểu tượng khay hệ thống, hoặc ở cửa sổ chính.
2. Vào cuộc họp như thường lệ. Phụ đề và biên bản hiện ở **cửa sổ ghi chú** nổi trên màn hình. Cửa sổ này không giành focus nên bạn vẫn gõ phím ở ứng dụng khác bình thường.
3. Chọn ngôn ngữ dịch ở **Cài đặt › Ngôn ngữ › Dịch sang**. Mặc định VPUNote tự nhận ra ngôn ngữ đối tác đang nói.
4. Kết thúc: bấm **Kết thúc**. Biên bản được lưu trong danh sách cuộc họp để xem lại, tìm kiếm và xuất ra Markdown.

Nếu cuộc họp có người ngồi cùng phòng, bấm **Bật micro** để ghi cả giọng người trong phòng.

### Để người khác nghe bạn bằng ngôn ngữ của họ (Phiên dịch trực tiếp)

**Chuẩn bị một lần:**

1. Vào **Cài đặt › Phiên dịch trực tiếp**, bật công tắc ở góc trên bên phải.
2. Ở mục **Đưa bản dịch vào cuộc họp**, bấm **Cài đặt** để cài **VB-Cable**, micro ảo miễn phí. Windows sẽ hỏi xác nhận quyền cài driver.
3. Chọn **Ngôn ngữ tôi nói** (ví dụ Tiếng Việt) và **Ngôn ngữ người tham gia trực tuyến nghe thấy** (ví dụ English, hoặc "Ngôn ngữ bên kia nói" để theo ngôn ngữ đối tác).
4. Trong Zoom/Teams/Meet: **Settings › Audio**, đặt **Microphone** là **CABLE Output (VB-Audio Virtual Cable)**.

**Khi họp:**

- Giữ **Ctrl phải**, nói, thả phím ra. VPUNote dịch rồi phát giọng đọc vào cuộc họp. Phím nói có thể đổi ở **Cài đặt › Phiên dịch trực tiếp** nếu bàn phím không có Ctrl phải.
- Dùng nút **Nghe thử** để nghe thử giọng đọc trước khi họp.

**Lưu ý:**

- **Độ trễ:** khoảng 1 đến 3 giây từ lúc bạn dứt câu đến lúc đối tác nghe.
- **Giọng đọc:** với Gemini, giọng đọc là giọng có sẵn của Windows, nên máy cần cài giọng của ngôn ngữ đích (Settings › Time & language › Speech). OpenRouter có giọng AI.
- **Tiếng vọng:** nên đeo tai nghe, để giọng đối tác từ loa không bị thu ngược lại vào micro.
- **Chỉ phát bản dịch:** VPUNote không bao giờ phát câu chưa được dịch vào cuộc họp. Nếu dịch lỗi, câu đó bị bỏ qua.
- **Đối tác nghe giọng đọc, không phải giọng thật của bạn.**

### Cửa sổ ghi chú và chia sẻ màn hình

Cửa sổ ghi chú mặc định **không hiện khi bạn chia sẻ màn hình**, nên người khác không thấy phụ đề của bạn. Muốn họ thấy, bật **Cài đặt › Riêng tư và khởi động › Cho bên kia thấy khi chia sẻ màn hình**; khi đó cửa sổ hiện thêm nhãn "Bên kia nhìn thấy".

## Quyền riêng tư

- Âm thanh và văn bản của cuộc họp được gửi tới **dịch vụ AI bạn chọn** (Google Gemini, OpenRouter hoặc máy chủ của bạn). Hãy báo trước cho người tham gia nếu cuộc họp có nội dung nhạy cảm.
- Phần dịch dự phòng gửi văn bản tới Google Dịch, trừ khi bạn dùng dịch vụ tự chọn và bật **Chỉ gửi âm thanh và văn bản đến dịch vụ của bạn**.
- API key được mã hoá bằng tài khoản Windows và nằm trong `%APPDATA%\VPUNote`, không ghi vào nhật ký hay file xuất.
- Biên bản họp lưu trên máy bạn, ở thư mục dữ liệu nói trên.

## Xử lý sự cố

| Hiện tượng | Cách xử lý |
|---|---|
| SmartScreen chặn khi chạy | Bấm **More info › Run anyway**. Ứng dụng chưa được ký số. |
| Chạy file mới mà vẫn thấy giao diện cũ | Dùng bản có số build lớn hơn bản đang cài. Nếu vẫn không, gỡ VPUNote trong Settings › Apps rồi chạy lại. |
| "Key này không dùng được" | Key dán thiếu ký tự, đã bị xoá hoặc thuộc dịch vụ khác. Tạo key mới và dán đủ. |
| "Mô hình không dùng được" | Cập nhật lên bản mới nhất, sau đó vào **Cài đặt › Dịch vụ AI › Kiểm tra lại**. |
| "Không truy cập được dịch vụ" | Kiểm tra mạng hoặc proxy; ở một số nơi cần bật proxy hệ thống để dùng Google/OpenRouter. |
| Không có phụ đề khi họp | Kiểm tra máy không tắt tiếng; thanh âm lượng ở bước thử âm thanh phải nhảy khi có tiếng. VPUNote nghe âm thanh phát ra từ máy, không nghe qua micro. |
| Đối tác không nghe thấy bản dịch | Kiểm tra đã cài VB-Cable và micro trong phần mềm họp là **CABLE Output**, không phải micro thật. |
| Đối tác nghe vọng hoặc lặp | Đeo tai nghe; tắt micro thật trong phần mềm họp. |

## Dành cho nhà phát triển

Bản Windows viết bằng .NET 10 và WPF, không dùng gói NuGet nào. Cấu trúc thư mục, cách build và kiểm thử nằm trong [windows/README.md](windows/README.md). Release tự động: mỗi lần đẩy thay đổi trong `windows/` lên nhánh phát triển, GitHub Actions build và đăng một bản pre-release mới.

Trang chủ: [tbd.edu.vn](https://tbd.edu.vn).
