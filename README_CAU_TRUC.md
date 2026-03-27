# CẤU TRÚC DỰ ÁN — HỆ THỐNG THI LÝ THUYẾT LÁI XE MÔ TÔ

---

## DỰ ÁN NÀY LÀ GÌ?

Đây là ứng dụng Android viết bằng Java, cho phép người dùng ôn tập và thi thử lý thuyết lái xe mô tô. Ứng dụng có hai loại tài khoản: User thường (thi thử, xem lịch sử, ôn bài) và Admin (quản lý câu hỏi, người dùng, bài thi).

Backend là một REST API riêng biệt, ứng dụng Android này chỉ là Frontend gọi các API đó.

---

## THÔNG TIN KỸ THUẬT CƠ BẢN

Ngôn ngữ: Java (không phải Kotlin)

Android SDK: minSdk = 31 (tức là chỉ chạy trên Android 12 trở lên), targetSdk = 36

Build system: Gradle với file cấu hình `.kts` (Kotlin DSL)

Java version: 11

Encoding: UTF-8 bắt buộc (vì có tiếng Việt), được cấu hình trong build.gradle.kts

---

## CÁC THƯ VIỆN ĐANG DÙNG VÀ LÝ DO

**Volley 1.2.1** — Thư viện gửi HTTP request. Khi app cần lấy dữ liệu từ server hoặc gửi dữ liệu lên, đây là thứ được dùng. Volley tự xử lý trên background thread và gọi callback trên main thread, nên không cần lo về threading.

**Gson 2.10.1** — Thư viện serialize/deserialize JSON. Trong dự án này chủ yếu dùng `org.json` (built-in của Android), Gson có mặt nhưng không phải thành phần chính.

**Material Design Components** — Bộ UI của Google. Cung cấp các component như `MaterialButton`, `MaterialCardView`, `TextInputLayout`, `BottomNavigationView`, `TabLayout`, `ChipGroup`, `BadgeDrawable`, `FloatingActionButton`. Tất cả những gì bạn thấy trên màn hình đều từ thư viện này.

**RecyclerView 1.3.2** — Component hiển thị danh sách cuộn. Mọi danh sách trong app (lịch sử thi, câu hỏi, người dùng...) đều dùng RecyclerView.

**ViewBinding** — Không phải thư viện ngoài mà là tính năng của Android. Khi bật `viewBinding = true` trong build.gradle.kts, Android tự sinh ra một class Java cho mỗi file XML layout. Ví dụ file `activity_login.xml` sẽ sinh ra class `ActivityLoginBinding`. Dùng class này để truy cập View thay vì `findViewById`, giúp tránh lỗi NullPointerException và type-safe.

**Google Maps SDK 19.0.0 + Play Services Location 21.3.0** — Dùng trong màn hình bản đồ trường lái xe. Hiển thị bản đồ tương tác và các marker vị trí.

**AlarmManager + NotificationCompat** — Có sẵn trong Android SDK. Dùng để đặt lịch nhắc nhở học tập hàng ngày vào giờ người dùng chọn.

**Fragment 1.7.1** — Component cho phép tái sử dụng phần UI trong Activity. ViewPager2 và BottomNavigation đều quản lý các Fragment.

---

## CẤU TRÚC THƯ MỤC

```
app/src/main/
├── java/com/example/motorcycletheory/
│   ├── activities/       ← 8 màn hình Activity
│   ├── fragments/        ← 9 Fragment (màn hình con)
│   ├── adapters/         ← 7 Adapter cho RecyclerView
│   ├── models/           ← 5 class dữ liệu (POJO)
│   ├── network/          ← 1 class quản lý HTTP
│   └── utils/            ← 6 class tiện ích
│
└── res/
    ├── anim/             ← Animation XML
    ├── drawable/         ← Shapes, backgrounds, badges
    ├── layout/           ← Tất cả file giao diện XML
    ├── menu/             ← Menu bottom navigation
    └── values/           ← Colors, strings, themes
```

---

## FOLDER `activities/` — 8 MÀN HÌNH CHÍNH

Activity là đơn vị màn hình của Android. Mỗi Activity chiếm toàn màn hình và có vòng đời riêng (onCreate, onResume, onDestroy...). Khi mở một Activity mới bằng `startActivity(intent)`, màn hình cũ vẫn còn trên back stack, người dùng có thể bấm Back để quay lại. Nếu gọi thêm `finish()` sau `startActivity()`, màn hình cũ bị đóng và không quay lại được.

**LoginActivity.java** — Màn hình đầu tiên khi mở app (khai báo là LAUNCHER trong AndroidManifest). Xử lý đăng nhập: validate input, gọi API, lưu token, chuyển sang MainActivity. Nếu đã có token trong SharedPreferences thì bỏ qua form và chuyển thẳng sang MainActivity.

**RegisterActivity.java** — Màn hình đăng ký tài khoản mới. Validate 4 field (username, email, password, confirmPassword), gọi API đăng ký. Sau khi thành công gọi `finish()` để quay về LoginActivity.

**MainActivity.java** — Màn hình chính, là container chứa các Fragment. Không hiển thị nội dung trực tiếp mà chỉ quản lý BottomNavigationView và vùng chứa Fragment (`fragmentContainer`). Khi user chọn tab, MainActivity thay Fragment tương ứng vào `fragmentContainer`. Admin và User thấy các tab khác nhau: User thấy 5 tab (Home, Ngân hàng câu hỏi, Giỏ ôn tập, Lịch sử, Tài khoản), Admin chỉ thấy 2 tab (Bảng điều khiển, Tài khoản).

**ExamConfirmActivity.java** — Màn hình xác nhận thông tin trước khi thi. Hiển thị form điền họ tên, email, SĐT và chọn phương thức thanh toán giả (VNPay/ZaloPay/MoMo). Sau khi nhấn thanh toán, delay 2 giây giả lập xử lý, rồi gọi API tạo đề thi, sau đó chuyển sang ExamTakingActivity.

**ExamTakingActivity.java** — Màn hình làm bài thi. Nhận danh sách câu hỏi dạng JSON string qua Intent Extra. Hiển thị từng câu một, cho phép chọn đáp án (RadioButton A/B/C/D), lưu đáp án vào `JSONObject selectedAnswers`, khi đến câu cuối thì submit bài. Lưu ý: Activity này dùng `findViewById` trực tiếp, không dùng ViewBinding.

**ExamResultActivity.java** — Màn hình kết quả bài thi. Nhận điểm số, trạng thái đậu/trượt qua Intent Extra. Có thể gọi API để xem chi tiết từng câu đúng/sai.

**QuestionDetailActivity.java** — Màn hình xem chi tiết một câu hỏi. Nhận toàn bộ dữ liệu câu hỏi qua Intent Extras (không gọi API riêng). Hiển thị nội dung, 4 đáp án, highlight đáp án đúng bằng màu xanh lá. Có nút thêm/xóa câu hỏi khỏi giỏ ôn tập.

**DrivingSchoolMapActivity.java** — Màn hình bản đồ các trường sát hạch lái xe. Hiển thị Google Maps với 8 markers và danh sách các trường bên dưới. Dữ liệu 8 trường là hard-coded trong code, không lấy từ API.

---

## FOLDER `fragments/` — 9 FRAGMENT (MÀN HÌNH CON)

Fragment là thành phần UI tái sử dụng, tồn tại bên trong một Activity. Fragment có vòng đời riêng nhưng phụ thuộc vào vòng đời của Activity chứa nó. Trong dự án này, tất cả Fragment của User và Admin đều được MainActivity chứa.

Điểm quan trọng: Fragment dùng ViewBinding phải set `binding = null` trong `onDestroyView()` để tránh memory leak, vì Fragment có thể bị destroy view nhưng object Fragment vẫn còn sống.

**HomeFragment.java** — Tab đầu tiên của User. Có 3 khu vực: nút tạo đề thi (dẫn đến ExamConfirmActivity), card bản đồ trường lái xe (dẫn đến DrivingSchoolMapActivity), và card nhắc nhở học tập với Switch bật/tắt và nút chọn giờ nhắc.

**QuestionBankFragment.java** — Tab ngân hàng câu hỏi. Tải 200 câu từ API. Có ChipGroup để lọc theo 3 kiểu: tất cả, chỉ câu điểm liệt, chỉ câu đã bookmark. Nhấn vào một câu hỏi sẽ mở QuestionDetailActivity. Nhấn icon bookmark sẽ thêm/xóa câu hỏi trong giỏ ôn tập thông qua StudyCartManager.

**StudyCartFragment.java** — Tab giỏ ôn tập. Hiển thị danh sách câu hỏi đã bookmark, lưu local trong SharedPreferences. Có nút xóa từng câu, xóa tất cả, và nút "Luyện tập" để mở ExamConfirmActivity. Khi giỏ rỗng, nút Luyện tập bị disable (alpha 0.5).

**HistoryFragment.java** — Tab lịch sử thi. Gọi API lấy danh sách các bài thi đã làm và hiển thị bằng RecyclerView.

**ProfileFragment.java** — Tab tài khoản. Hiển thị tên (tạo từ email), vai trò, avatar chữ cái đầu. Nút đăng xuất gọi API logout rồi xóa session local.

**AdminFragment.java** — Tab bảng điều khiển dành cho Admin. Hiển thị 4 con số thống kê (tổng users, câu hỏi, câu điểm liệt, bài thi) từ API. Bên dưới là ViewPager2 chứa 3 sub-fragment. Có FloatingActionButton (FAB) để tạo mới (tùy tab đang chọn). AdminFragment cũng đóng vai trò "cha" cung cấp `sessionManager` và `apiClient` cho 3 sub-fragment con thông qua `getSessionManager()` và `getApiClient()`.

**AdminUsersTabFragment.java** — Sub-fragment trong AdminFragment, tab "Người dùng". CRUD đầy đủ cho user qua API `/api/admin/users`.

**AdminQuestionsTabFragment.java** — Sub-fragment trong AdminFragment, tab "Câu hỏi". CRUD đầy đủ cho câu hỏi qua API `/api/question`.

**AdminExamsTabFragment.java** — Sub-fragment trong AdminFragment, tab "Bài thi". Chỉ xem và xóa bài thi (không tạo). Khi xóa xong sẽ gọi `parent.refreshDashboard()` để cập nhật số liệu ở AdminFragment.

---

## FOLDER `adapters/` — 7 ADAPTER CHO RECYCLERVIEW

Adapter là cầu nối giữa dữ liệu (List) và RecyclerView (danh sách hiển thị). Pattern chuẩn gồm 3 thành phần:

1. **Adapter class** — Giữ List dữ liệu, implement RecyclerView.Adapter
2. **ViewHolder class** (lồng trong Adapter) — Giữ reference các View trong 1 item, có method `bind(model)`
3. **Interface callback** (lồng trong Adapter) — Để Fragment/Activity nhận sự kiện click từ item

Cách hoạt động: RecyclerView gọi `onCreateViewHolder()` để tạo ViewHolder mới (inflate XML item), rồi gọi `onBindViewHolder()` để bind dữ liệu vào ViewHolder tại mỗi vị trí. ViewHolder giúp cache reference View, tránh phải `findViewById` lại mỗi lần scroll.

**HistoryAdapter.java** — Hiển thị danh sách lịch sử thi. Mỗi item dùng `item_history.xml`. Bind model `ExamHistory` vào: số thứ tự bài, điểm, badge Đậu/Trượt, ngày thi.

**QuestionBankAdapter.java** — Hiển thị danh sách câu hỏi trong ngân hàng. Implement `OnQuestionBankListener` với 2 callback: `onQuestionClick()` khi nhấn vào câu hỏi, `onBookmarkToggle()` khi nhấn icon bookmark. Có method `updateBookmarks(Set<Integer>)` để cập nhật trạng thái icon bookmark.

**StudyCartAdapter.java** — Hiển thị danh sách câu hỏi trong giỏ ôn tập. Implement `OnCartItemListener` với 2 callback: `onRemoveItem()` khi nhấn nút xóa, `onItemClick()` khi nhấn vào item.

**DrivingSchoolAdapter.java** — Hiển thị danh sách trường lái xe. Implement `OnSchoolActionListener` với 3 callback: `onCallClick()` gọi điện, `onDirectionClick()` chỉ đường, `onSchoolClick()` zoom map.

**AdminUserAdapter.java** — Hiển thị danh sách users cho Admin. Implement `OnUserActionListener` với 2 callback: `onEditUser()` và `onDeleteUser()`.

**AdminQuestionAdapter.java** — Hiển thị danh sách câu hỏi cho Admin. Implement `OnQuestionActionListener` với 3 callback: `onViewQuestion()`, `onEditQuestion()`, `onDeleteQuestion()`.

**AdminExamAdapter.java** — Hiển thị danh sách bài thi cho Admin. Implement `OnExamActionListener` với 2 callback: `onViewExam()` và `onDeleteExam()`.

---

## FOLDER `models/` — 5 CLASS DỮ LIỆU (POJO)

Model là class Java thuần (Plain Old Java Object) chỉ chứa các field và getter/setter. Không có logic, không có API call. Dùng để truyền dữ liệu giữa các tầng: parse từ JSON ra model, rồi truyền model vào Adapter để hiển thị.

**ExamHistory.java** — Dữ liệu một bài thi đã hoàn thành. Gồm: `examId`, `score`, `totalQuestions`, `passed` (boolean), `date` (String), `duration` (int — thời gian làm bài tính giây).

**AdminUser.java** — Dữ liệu một người dùng trong hệ thống. Gồm: `userId`, `username`, `email`, `role` (chuỗi "User" hoặc "Admin").

**AdminQuestion.java** — Dữ liệu một câu hỏi. Gồm: `questionId`, `content`, `answerA`, `answerB`, `answerC`, `answerD`, `correctAnswer` (chuỗi "A"/"B"/"C"/"D"), `categoryId` (int), `isImportant` (boolean — câu điểm liệt). Đây là model được dùng nhiều nhất, xuất hiện ở QuestionBank, StudyCart, QuestionDetail, AdminQuestions.

**AdminExam.java** — Dữ liệu một bài thi trong hệ thống (nhìn từ góc Admin). Gồm: `examId`, `userId`, `username` (của người thi), `score`, `totalQuestions`, `isPassed` (boolean), `examDate` (String).

**DrivingSchool.java** — Dữ liệu một trường sát hạch lái xe. Gồm: `id`, `name`, `address`, `phone`, `latitude` (double), `longitude` (double), `rating` (float). Dữ liệu 8 trường được hard-code trực tiếp trong `DrivingSchoolMapActivity.getDrivingSchools()`.

---

## FOLDER `network/` — QUẢN LÝ HTTP

**ApiClient.java** — Class duy nhất trong folder này. Implement Singleton Pattern, nghĩa là chỉ có đúng 1 instance tồn tại trong toàn app.

Lý do dùng Singleton: Volley `RequestQueue` tốn tài nguyên để tạo, nếu tạo mới mỗi lần sẽ lãng phí. Singleton đảm bảo `RequestQueue` được tạo 1 lần và tái sử dụng.

ApiClient cung cấp 3 thứ:
- `getRequestQueue()` — Trả về RequestQueue để add request vào
- `endpoint(path)` — Ghép base URL với path. VD: `endpoint("/api/auth/login")` → `"http://10.0.2.2:5000/api/auth/login"`
- `authHeaders(sessionManager)` — Tạo Map header gồm `Content-Type: application/json` và `Authorization: Bearer <token>`

Base URL được đọc từ `res/values/strings.xml` với key `api_base_url`. Khi test trên emulator dùng `http://10.0.2.2:5000` vì `10.0.2.2` là địa chỉ localhost của máy host.

---

## FOLDER `utils/` — 6 CLASS TIỆN ÍCH

**SessionManager.java** — Quản lý trạng thái đăng nhập. Lưu token JWT, email, role vào SharedPreferences tên `"motorcycle_session"`. Các Fragment và Activity dùng class này để lấy token khi gọi API, lấy role để quyết định hiển thị gì.

**StudyCartManager.java** — Quản lý giỏ ôn tập. Lưu danh sách câu hỏi đã bookmark vào SharedPreferences tên `"study_cart"` dưới dạng JSON string (một JSONArray). Khi cần hiển thị giỏ, đọc JSON string, parse thành List. Đây là lưu trữ hoàn toàn local, không cần internet.

**NotificationHelper.java** — Xử lý toàn bộ logic liên quan đến thông báo nhắc nhở. Tạo Notification Channel, lên lịch báo thức hàng ngày bằng AlarmManager, hiển thị notification với nội dung động (đếm số câu trong giỏ).

**StudyReminderReceiver.java** — BroadcastReceiver đơn giản, chỉ có 3 dòng. Khi AlarmManager đến giờ sẽ gửi broadcast, Receiver này nhận và gọi `NotificationHelper.showStudyReminder()`.

**AdminViewPagerAdapter.java** — Adapter dành riêng cho ViewPager2 trong AdminFragment. Extend `FragmentStateAdapter`. Quản lý danh sách Fragment và title của 3 tab: Users, Questions, Exams.

**SimpleTextAdapter.java** — Adapter đơn giản cho Spinner hoặc danh sách text.

---

## FOLDER `res/layout/` — CÁC FILE GIAO DIỆN XML

Mỗi Activity có 1 file layout, mỗi Fragment có 1 file layout. Ngoài ra còn có layout cho từng item trong RecyclerView và các dialog popup.

Layout của Activity: `activity_login.xml`, `activity_register.xml`, `activity_main.xml`, `activity_exam_confirm.xml`, `activity_exam_taking.xml`, `activity_exam_result.xml`, `activity_question_detail.xml`, `activity_driving_school_map.xml`

Layout của Fragment: `fragment_home.xml`, `fragment_question_bank.xml`, `fragment_study_cart.xml`, `fragment_history.xml`, `fragment_profile.xml`, `fragment_admin.xml`

Layout của Admin sub-tabs: `admin_tab_users.xml`, `admin_tab_questions.xml`, `admin_tab_exams.xml`

Layout của từng item trong RecyclerView: `item_history.xml`, `item_question_bank.xml`, `item_study_cart.xml`, `item_driving_school.xml`, `item_admin_user.xml`, `item_admin_question.xml`, `item_admin_exam.xml`

Layout của Dialog popup: `dialog_user.xml`, `dialog_question.xml`, `dialog_view_question.xml`, `dialog_confirm_delete.xml`

---

## FOLDER `res/values/` — GIÁ TRỊ DÙNG CHUNG

**strings.xml** — Chứa tất cả chuỗi text trong app. Quan trọng nhất là `api_base_url` chứa địa chỉ server. Việc lưu tất cả text ở đây giúp dễ thay đổi ngôn ngữ và tránh hard-code text trong code Java.

**colors.xml** — Định nghĩa các màu sắc như `brand_green`, `brand_dark`, `danger_red`, `correct_answer_bg`, `white`. Khi cần đổi màu toàn app chỉ sửa 1 chỗ này.

**themes.xml** — Định nghĩa theme Material Design cho toàn app (font, màu primary, màu secondary...).

---

## FOLDER `res/drawable/` — HÌNH ẢNH VÀ SHAPES

Các file drawable đáng chú ý:

`badge_pass.xml` và `badge_fail.xml` — Shape hình viên thuốc (pill shape) màu xanh/đỏ, dùng làm background cho text "ĐẬU" và "TRƯỢT".

`badge_important.xml` — Shape màu cam/vàng cho nhãn "Câu điểm liệt".

`badge_category.xml` — Shape cho nhãn danh mục câu hỏi.

`circle_avatar.xml` — Shape hình tròn làm background cho avatar chữ cái đầu trong ProfileFragment.

`ic_back.xml` — Icon mũi tên quay lại.

---

## FOLDER `res/anim/`

`shake.xml` — Animation rung ngang. Được dùng trong ExamTakingActivity khi user nhấn "Tiếp theo" mà chưa chọn đáp án, để thu hút sự chú ý vào thông báo lỗi.

---

## LUỒNG ĐIỀU HƯỚNG TỔNG QUAN

Khi mở app lần đầu hoặc chưa đăng nhập:

```
[Khởi động app]
        |
        v
LoginActivity (LAUNCHER — màn hình đầu tiên)
        |
        |-- Đã có token? ---> MainActivity (bỏ qua form)
        |
        |-- Chưa đăng nhập ---> Hiện form
                |
                |-- Click Đăng ký ---> RegisterActivity
                |                           |
                |                           v
                |               Đăng ký xong ---> back về LoginActivity
                |
                |-- Nhập email/password + Click Đăng nhập
                        |
                        v
                   API /api/auth/login
                        |
                        v
                   Lưu token, email, role vào SharedPreferences
                        |
                        v
                   MainActivity + finish() LoginActivity
```

Từ MainActivity với role User:

```
MainActivity (5 tab dưới cùng)
        |
        |-- Tab Home (HomeFragment)
        |       |-- Tạo đề thi ---> ExamConfirmActivity ---> ExamTakingActivity ---> ExamResultActivity
        |       |-- Bản đồ -----> DrivingSchoolMapActivity
        |       |-- Nhắc nhở --> AlarmManager (background, không chuyển màn hình)
        |
        |-- Tab Ngân hàng câu hỏi (QuestionBankFragment)
        |       |-- Click câu hỏi ---> QuestionDetailActivity
        |       |-- Bookmark -------> StudyCartManager (local, không chuyển màn hình)
        |
        |-- Tab Giỏ ôn tập (StudyCartFragment) [có badge đỏ]
        |       |-- Click câu hỏi ---> QuestionDetailActivity
        |       |-- Luyện tập ------> ExamConfirmActivity
        |
        |-- Tab Lịch sử (HistoryFragment) [chỉ xem]
        |
        |-- Tab Tài khoản (ProfileFragment)
                |-- Đăng xuất ---> API logout ---> LoginActivity
```

Từ MainActivity với role Admin:

```
MainActivity (2 tab dưới cùng)
        |
        |-- Tab Bảng điều khiển (AdminFragment)
        |       |   [Thống kê 4 con số từ API]
        |       |
        |       |-- Tab Users (AdminUsersTabFragment)
        |       |       CRUD: Xem list + Dialog tạo/sửa + Confirm xóa
        |       |
        |       |-- Tab Questions (AdminQuestionsTabFragment)
        |       |       CRUD: Xem list + Dialog tạo/sửa + Xem chi tiết + Confirm xóa
        |       |
        |       |-- Tab Exams (AdminExamsTabFragment)
        |               Xem list + Mở ExamResultActivity + Confirm xóa
        |
        |-- Tab Tài khoản (ProfileFragment) [giống User]
```

---

## CÁCH API ĐƯỢC GỌI — QUY TRÌNH CHUNG

Mọi API call trong app đều theo cùng 1 pattern:

```java
// Bước 1: Lấy ApiClient singleton và tạo URL
ApiClient apiClient = ApiClient.getInstance(context);
String url = apiClient.endpoint("/api/exam/generate");
// url = "http://10.0.2.2:5000/api/exam/generate"

// Bước 2: Tạo Request object
JsonObjectRequest request = new JsonObjectRequest(
    Request.Method.POST,  // HTTP method
    url,                  // URL đã tạo ở trên
    payload,              // JSON body gửi lên (null nếu GET)
    response -> {         // Callback thành công, chạy trên Main Thread
        // Xử lý response
    },
    error -> {            // Callback thất bại
        if (error.networkResponse == null) {
            // Lỗi mạng: không kết nối được
        } else {
            int code = error.networkResponse.statusCode;
            // 401: Hết hạn token, 400: Dữ liệu sai, 500: Lỗi server
        }
    }
) {
    // Bước 3 (nếu cần token): Override getHeaders()
    @Override
    public Map<String, String> getHeaders() {
        return ApiClient.authHeaders(sessionManager);
        // Trả về {"Content-Type": "application/json", "Authorization": "Bearer xxx"}
    }

    // Bước 4 (nếu có tiếng Việt): Override parseNetworkResponse để dùng UTF-8
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        String json = new String(response.data, StandardCharsets.UTF_8);
        return Response.success(new JSONObject(json), ...);
    }
};

// Bước 5: Đưa request vào hàng đợi để thực thi
apiClient.getRequestQueue().add(request);
```

---

## CÁCH LƯUTRU DỮ LIỆU LOCAL

App dùng SharedPreferences ở 3 chỗ khác nhau, mỗi chỗ có file riêng (tên khác nhau):

SharedPreferences tên `"motorcycle_session"` — Dùng bởi `SessionManager`. Lưu: `jwt_token` (chuỗi token), `email` (email người dùng), `role` (chuỗi "User" hoặc "Admin"). Tồn tại cho đến khi logout.

SharedPreferences tên `"study_cart"` — Dùng bởi `StudyCartManager`. Lưu: `cart_items` (một JSON array serialized thành String). Mỗi item trong array là một object JSON chứa toàn bộ thông tin câu hỏi. Tồn tại cho đến khi xóa thủ công.

SharedPreferences tên `"notification_prefs"` — Dùng bởi `NotificationHelper`. Lưu: `reminder_enabled` (boolean), `reminder_hour` (int, mặc định 20), `reminder_minute` (int, mặc định 0). Tồn tại cho đến khi người dùng tắt nhắc nhở.

---

## CÁC API ENDPOINTS VÀ AI GỌI CHÚNG

**Không cần token:**
- `POST /api/auth/login` → LoginActivity.doLogin()
- `POST /api/auth/signup` → RegisterActivity.doSignup()

**Cần token (User):**
- `POST /api/auth/logout` → ProfileFragment.callLogout()
- `POST /api/exam/generate` → ExamConfirmActivity.generateExam()
- `POST /api/exam/submit` → ExamTakingActivity.submitExam()
- `GET /api/exam/history` → HistoryFragment.loadHistory()
- `GET /api/exam/detail/:id` → ExamResultActivity.loadExamDetail()
- `GET /api/question?page=1&pageSize=200` → QuestionBankFragment.loadQuestions()

**Cần token Admin:**
- `GET /api/admin/stats` → AdminFragment.loadDashboardStats()
- `GET /api/admin/users` → AdminUsersTabFragment.loadUsers()
- `POST /api/admin/users` → AdminUsersTabFragment.createUser()
- `PUT /api/admin/users/:id` → AdminUsersTabFragment.updateUser()
- `DELETE /api/admin/users/:id` → AdminUsersTabFragment.deleteUser()
- `GET /api/question?page=1&pageSize=50` → AdminQuestionsTabFragment.loadQuestions()
- `POST /api/question` → AdminQuestionsTabFragment.createQuestion()
- `PUT /api/question/:id` → AdminQuestionsTabFragment.updateQuestion()
- `DELETE /api/question/:id` → AdminQuestionsTabFragment.deleteQuestion()
- `GET /api/exam/admin/all-exams` → AdminExamsTabFragment.loadExams()
- `DELETE /api/exam/:id` → AdminExamsTabFragment.deleteExam()

---

## LƯU Ý QUAN TRỌNG KHI KIỂM TRA

**ExamTakingActivity không dùng ViewBinding** — Activity này dùng `setContentView(R.layout.activity_exam_taking)` và `findViewById()` thay vì ViewBinding. Đây là điểm khác biệt với tất cả Activity/Fragment còn lại.

**Admin menu build bằng code, không phải XML** — Khi role là Admin, MainActivity xóa menu cũ (`getMenu().clear()`) rồi add từng item bằng code (`getMenu().add()`). User thì dùng menu XML `bottom_nav_menu.xml`.

**AdminExamsTabFragment lấy sessionManager từ cha** — Fragment con không tự tạo SessionManager mà gọi `((AdminFragment) getParentFragment()).getSessionManager()`. AdminFragment đóng vai trò provider cho 3 sub-fragment.

**Logout xử lý cả khi API lỗi** — Trong ProfileFragment, dù API logout thành công hay thất bại, vẫn gọi `sessionManager.clear()` và chuyển về LoginActivity. Lý do: dù server có lỗi, vẫn phải đảm bảo user thoát được khỏi app.

**StudyCartManager tự check trùng lặp** — Khi `addQuestion()`, nó loop qua danh sách hiện tại, nếu đã có questionId đó thì không thêm nữa. Đảm bảo không bị trùng trong giỏ.
