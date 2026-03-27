# ÔN THI NHANH — TÓM TẮT TOÀN BỘ ỨNG DỤNG

Tài liệu này dành để ôn tập nhanh trước kiểm tra. Mỗi phần giải thích ngắn gọn mục đích, luồng hoạt động, và các điểm cần chú ý của từng thành phần.

Để đọc chi tiết từng hàm, xem `README_FUNCTIONS.md`.
Để hiểu cấu trúc folder và tổng quan, xem `README_CAU_TRUC.md`.

---

# TỔNG QUAN NHANH

App có **8 Activity**, **9 Fragment**, **7 Adapter**, **5 Model**, **6 Utils**.

Người dùng có 2 loại: **User** (5 tab: Home, Ngân hàng câu hỏi, Giỏ ôn tập, Lịch sử, Tài khoản) và **Admin** (2 tab: Bảng điều khiển, Tài khoản).

Toàn bộ dữ liệu từ server được lấy qua **Volley**. Dữ liệu local (session, giỏ câu hỏi, cài đặt nhắc nhở) được lưu bằng **SharedPreferences**.

---

# LUỒNG KHỞI ĐỘNG APP

Khi mở app, Android khởi động `LoginActivity` (khai báo LAUNCHER trong AndroidManifest).

Ngay trong `onCreate()`, LoginActivity kiểm tra `sessionManager.isLoggedIn()`. Nếu đã có token từ lần trước → gọi `openMain()` ngay, không hiển thị form. Nếu chưa → hiển thị form để đăng nhập.

Sau khi đăng nhập thành công, token/email/role được lưu vào SharedPreferences qua `sessionManager.saveSession()`. Rồi gọi `openMain()` để chuyển sang `MainActivity` và gọi `finish()` để đóng LoginActivity (không cho Back lại).

---

# LOGINACTIVITY — 7 HÀM

`onCreate()` khởi tạo form, kiểm tra session, setup listener cho 2 nút.

`validateLoginInput(email, password)` kiểm tra email (rỗng? đúng format?) và password (rỗng? đủ 6 ký tự?). Trả về boolean. Đặc điểm quan trọng: dùng flag `valid = false` thay vì `return false` ngay, để hiển thị tất cả lỗi cùng lúc.

`doLogin(email, password)` gọi `setLoading(true)`, tạo JSONObject payload, tạo JsonObjectRequest POST đến `/api/auth/login`, trong callback success thì gọi `sessionManager.saveSession()` rồi `openMain()`, trong callback error thì gọi `mapAuthError()` để lấy message hiển thị Toast.

`setLoading(boolean loading)` disable/enable button và show/hide ProgressBar.

`mapAuthError(statusCode, data)` chuyển status code thành message. Status `-1` là lỗi mạng (không có networkResponse). Status `401` là sai email/password. Còn lại đọc message từ response body.

`stripQuotes(text)` xóa dấu nháy kép nếu server trả về `"Error message"` dạng có quotes.

`openMain()` gọi `startActivity(MainActivity)` rồi `finish()`.

---

# REGISTERACTIVITY — 4 HÀM

Tương tự LoginActivity nhưng khác ở chỗ: dùng `StringRequest` thay vì `JsonObjectRequest` (vì API đăng ký trả về text, không phải JSON). Validate thêm `username` và `confirmPassword`. Sau thành công chỉ gọi `finish()` để quay về LoginActivity, không gọi `openMain()`.

---

# MAINACTIVITY — 4 HÀM

`onCreate()` đọc role từ SessionManager. Nếu Admin thì xóa menu XML và build menu mới bằng code (`getMenu().clear()` rồi `getMenu().add()`), sau đó switch sang AdminFragment. Nếu User thì giữ menu XML 5 tab, gọi `setupCartBadge()`, switch sang HomeFragment. Setup `setOnItemSelectedListener` cho bottom nav.

`switchFragment(fragment)` thay Fragment trong `fragmentContainer` bằng `getSupportFragmentManager().beginTransaction().replace().commit()`.

`setupCartBadge()` tạo `BadgeDrawable` màu đỏ gắn vào tab Cart. Gọi `refreshCartBadge()` để set số ngay từ đầu.

`refreshCartBadge()` là hàm `public`. Đọc `StudyCartManager.getCartCount()`, nếu > 0 thì hiện số, nếu bằng 0 thì ẩn badge. Được gọi bởi: `onResume()` của MainActivity, và các Fragment khi thay đổi giỏ.

---

# EXAMCONFIRMACTIVITY — 6 HÀM

Màn hình giả lập xác nhận thanh toán trước khi thi. Không thực sự xử lý tiền, chỉ delay 2 giây rồi gọi API tạo đề.

`prefillUserInfo()` điền sẵn email từ SessionManager và tên tự động tạo bằng `deriveNameFromEmail()`.

`deriveNameFromEmail(email)` tách phần trước `@`, split bằng `.` hoặc `_`, capitalize từng phần. Ví dụ: `"nguyen.van.a@gmail.com"` → `"Nguyen Van A"`. Logic này xuất hiện giống nhau ở cả ExamConfirmActivity và ProfileFragment.

`validateInput()` kiểm tra họ tên, email, SĐT.

`processPayment()` disable nút, show ProgressBar, rồi dùng `Handler(Looper.getMainLooper()).postDelayed(() -> { generateExam(); }, 2000)` để delay 2 giây.

`generateExam()` POST `/api/exam/generate`, lưu `generatedExamId` và `generatedQuestionsJson = questions.toString()`, rồi gọi `showConfirmation()`.

`showConfirmation()` ẩn form, hiện card xác nhận với chi tiết. Setup nút "Bắt đầu thi" → Intent sang ExamTakingActivity với extras `EXTRA_EXAM_ID` và `EXTRA_QUESTIONS_JSON`, rồi `finish()`.

`getSelectedPaymentMethod()` đọc RadioGroup và trả về chuỗi `"VNPay"`, `"ZaloPay"`, hoặc `"MoMo"`.

---

# EXAMTAKINGACTIVITY — 5 HÀM

Lưu ý: Activity này KHÔNG dùng ViewBinding. Dùng `setContentView(R.layout.activity_exam_taking)` và `findViewById()`.

Dữ liệu đầu vào qua Intent: `EXTRA_EXAM_ID` (int) và `EXTRA_QUESTIONS_JSON` (String — JSONArray các câu hỏi).

Lưu đáp án trong `JSONObject selectedAnswers` với key là questionId dạng String, value là `"A"/"B"/"C"/"D"`.

`onCreate()` parse `EXTRA_QUESTIONS_JSON` thành `JSONArray questions`. Khởi tạo tất cả View bằng `findViewById()`. Setup listener cho button và RadioGroup (khi chọn đáp án thì `hideError()`). Gọi `renderQuestion()` để hiển thị câu đầu.

`renderQuestion()` hiển thị câu tại `currentIndex`: set text câu hỏi và 4 đáp án, `rgAnswers.clearCheck()` rồi restore đáp án đã chọn nếu có trong `selectedAnswers`, set text nút là "Tiếp theo" hoặc "Nộp bài" tùy vị trí.

`onNextClicked()` lấy đáp án qua `getSelectedOption()`, nếu rỗng thì `showError()` và return. Nếu có thì `selectedAnswers.put(String.valueOf(questionId), selected)`. Nếu là câu cuối thì `submitExam()`, còn lại thì `currentIndex++` rồi `renderQuestion()`.

`getSelectedOption()` đọc `rgAnswers.getCheckedRadioButtonId()` và trả về `"A"/"B"/"C"/"D"` hoặc `""`.

`submitExam()` tạo payload `{ examId, answers }`, POST `/api/exam/submit`. Callback success: truyền kết quả qua Intent sang ExamResultActivity rồi `finish()`.

`showError(message)` hiển thị text lỗi và chạy animation `R.anim.shake` lên cả `tvErrorMessage` và `rgAnswers`.

---

# EXAMRESULTACTIVITY — 3 HÀM

Nhận qua Intent: `EXTRA_EXAM_ID`, `EXTRA_SCORE`, `EXTRA_TOTAL_QUESTIONS`, `EXTRA_PASSED`, `EXTRA_FAILED_IMPORTANT`.

`displaySummary()` hiển thị điểm số (`score/totalQuestions`), badge Đậu hoặc Trượt, và cảnh báo nếu `failedByImportantQuestion == true` (trượt vì sai câu điểm liệt dù điểm đủ).

`loadExamDetail()` GET `/api/exam/detail/:examId` để xem chi tiết từng câu đúng/sai.

`formatDetail(response)` parse mảng câu hỏi trong response và build thành String dễ đọc để hiển thị.

---

# QUESTIONDETAILACTIVITY — 4 HÀM

Nhận toàn bộ dữ liệu qua Intent Extras, không gọi API.

`loadQuestion()` đọc tất cả Extras, tạo đối tượng `AdminQuestion`, hiển thị nội dung và 4 đáp án, hiện badge "Câu điểm liệt" nếu `isImportant`, gọi `highlightCorrectAnswer()`.

`highlightCorrectAnswer(correct)` xác định TextView nào chứa đáp án đúng, tạo `GradientDrawable` màu xanh với viền xanh đậm và apply làm background của TextView đó.

`setupCartButton()` gọi `updateCartButtonState()` để set trạng thái ban đầu. Click listener toggle: nếu `isInCart` thì `removeQuestion()`, nếu không thì `addQuestion()`, rồi gọi `updateCartButtonState()` lại.

`updateCartButtonState()` đọc `cartManager.isInCart(questionId)`: nếu trong giỏ thì text đỏ "Xóa khỏi giỏ", nếu chưa thì text xanh "Thêm vào giỏ".

---

# DRIVINGSCHOOLMAPACTIVITY — 5 HÀM

Implement `OnMapReadyCallback` và `DrivingSchoolAdapter.OnSchoolActionListener`.

`onCreate()` gọi `getDrivingSchools()` lấy 8 trường hard-coded, setup RecyclerView, gọi `mapFragment.getMapAsync(this)` — bất đồng bộ.

`onMapReady(GoogleMap map)` thêm marker cho 8 trường, dùng `LatLngBounds.Builder` để fit tất cả marker vào khung camera.

`onSchoolClick(school)` zoom camera đến trường được chọn với `animateCamera(newLatLngZoom(position, 15))`.

`onCallClick(school)` mở app gọi điện với `Intent(ACTION_DIAL, Uri.parse("tel:..."))`.

`onDirectionClick(school)` mở Google Maps navigation. Nếu không cài Google Maps thì mở web browser với URL Google Maps.

`getDrivingSchools()` trả về List 8 DrivingSchool với tọa độ GPS thực tế (Hà Nội, HCM, Đà Nẵng, Cần Thơ, Hải Phòng, Bình Dương, Đồng Nai, Huế).

---

# HOMEFRAGMENT — 6 HÀM

`onViewCreated()` setup 3 thứ: nút tạo đề thi → ExamConfirmActivity, card bản đồ → DrivingSchoolMapActivity, gọi `setupReminderCard()`.

`setupReminderCard()` đọc trạng thái nhắc nhở hiện tại, set Switch, setup listener. Chú ý: listener dùng `if (!buttonView.isPressed()) return` để tránh bị trigger khi code set `setChecked()`.

`requestNotificationPermissionAndEnable()` kiểm tra Android version: nếu >= 13 (TIRAMISU) và chưa có quyền `POST_NOTIFICATIONS` thì launch permission launcher; nếu Android < 13 hoặc đã có quyền thì gọi thẳng `enableReminder()`.

`enableReminder()` gọi `notificationHelper.scheduleReminder(hour, minute)` và cập nhật UI.

`showTimePicker()` hiện `TimePickerDialog`, khi chọn xong gọi `notificationHelper.scheduleReminder(hourOfDay, minute)`.

`updateReminderStatus()` cập nhật text `tvReminderStatus` và ẩn/hiện `btnSetTime`.

---

# QUESTIONBANKFRAGMENT — 7 HÀM

Implement `QuestionBankAdapter.OnQuestionBankListener`.

`onViewCreated()` khởi tạo `StudyCartManager`, tạo adapter với `new QuestionBankAdapter(this, getBookmarkedIds())`, setup RecyclerView, gọi `setupFilters()` và `loadQuestions()`.

`onResume()` gọi `adapter.updateBookmarks(getBookmarkedIds())` để cập nhật lại icon bookmark nếu user vừa bookmark từ màn hình khác.

`setupFilters()` lắng nghe ChipGroup, cập nhật `currentFilter` và gọi `applyFilter()`.

`loadQuestions()` GET `/api/question?page=1&pageSize=200`. Override `parseNetworkResponse` để đọc UTF-8. Gọi `parseQuestions(response)` khi thành công.

`parseQuestions(response)` đọc array từ key `"items"` hoặc `"questions"` (fallback). Parse từng object thành `AdminQuestion`. Gọi `applyFilter()`.

`applyFilter()` lọc `allQuestions` theo `currentFilter` (all/important/bookmarked), cập nhật counter và `adapter.submitList(filtered)`.

`getBookmarkedIds()` lấy danh sách câu hỏi từ `cartManager.getCartItems()` và extract `questionId` thành `Set<Integer>`.

`onQuestionClick(question)` tạo Intent sang QuestionDetailActivity với tất cả Extras.

`onBookmarkToggle(question, addToCart)` add hoặc remove qua `StudyCartManager`, gọi `adapter.updateBookmarks()`, gọi `updateCartBadge()`, nếu đang filter "bookmarked" thì gọi `applyFilter()` để re-filter.

`updateCartBadge()` cast `getActivity()` thành `MainActivity` rồi gọi `refreshCartBadge()`.

---

# STUDYCARTFRAGMENT — 6 HÀM

Implement `StudyCartAdapter.OnCartItemListener`.

`onResume()` gọi `loadCart()` để refresh khi user quay lại từ QuestionDetailActivity.

`loadCart()` đọc từ `cartManager.getCartItems()`, gọi `updateSummary()`, nếu rỗng hiện empty state và disable nút Luyện tập (alpha 0.5), nếu có thì `adapter.submitList(items)`.

`updateSummary(items)` set `tvCartCount` và đếm câu điểm liệt để set `tvImportantCount`.

`confirmClearAll()` hiện AlertDialog. Nếu xác nhận: `cartManager.clearCart()`, `loadCart()`, `updateCartBadge()`.

`startPractice()` kiểm tra `cartManager.getCartCount() == 0` rồi chuyển sang `ExamConfirmActivity`.

`onRemoveItem(question)` xóa câu hỏi khỏi giỏ rồi `loadCart()` và `updateCartBadge()`.

`onItemClick(question)` mở `QuestionDetailActivity`.

---

# PROFILEFRAGMENT — 4 HÀM

`onViewCreated()` lấy email và role từ SessionManager, gọi `deriveNameFromEmail()` để tạo tên, gọi `extractInitial()` để lấy ký tự đầu cho avatar. Set `tvDisplayName`, `tvRole`, `tvEmailValue`, `tvAvatarInitial`. Setup click cho `btnLogout`.

`deriveNameFromEmail(email)` tách email, split bằng `[._]`, capitalize từng phần. Cùng logic với ExamConfirmActivity.

`extractInitial(name)` lấy `charAt(0)` và `toUpperCase()`.

`callLogout(sessionManager)` POST `/api/auth/logout` dùng `StringRequest`. Dù thành công hay lỗi đều gọi `sessionManager.clear()` rồi `openLogin()`. Lý do handle cả lỗi: phải đảm bảo user thoát được khỏi app dù server có vấn đề.

`openLogin()` tạo Intent sang LoginActivity với flags `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` để xóa toàn bộ back stack.

---

# ADMINFRAGMENT — 5 HÀM

`onViewCreated()` kiểm tra role, nếu không phải Admin thì return. Gọi `setupViewPager()` và `loadDashboardStats()`. Setup FAB click.

`setupViewPager()` tạo 3 sub-fragment, tạo `AdminViewPagerAdapter`, add 3 fragment vào adapter, set adapter cho ViewPager2, đăng ký `OnPageChangeCallback` để lưu `currentTabPosition`, dùng `TabLayoutMediator` để sync tab title.

`loadDashboardStats()` GET `/api/admin/stats`. Response gồm `totalUsers`, `totalQuestions`, `totalImportantQuestions`, `totalExams`. Gọi `updateDashboardUI()`.

`updateDashboardUI(stats)` set 4 TextView số liệu.

`onFabClicked()` switch theo `currentTabPosition`: 0 → `usersTabFragment.showCreateUserDialog()`, 1 → `questionsTabFragment.showCreateQuestionDialog()`, 2 → Toast "Không thể tạo".

`getSessionManager()`, `getApiClient()`, `refreshDashboard()` là 3 hàm `public` để sub-fragment truy cập. Sub-fragment gọi: `((AdminFragment) getParentFragment()).getSessionManager()`.

---

# ADMINEXAMSTABFRAGMENT — 4 HÀM

`onViewCreated()` lấy sessionManager và apiClient từ `(AdminFragment) getParentFragment()`. Tạo adapter, set listener `this` (implement `OnExamActionListener`). Gọi `loadExams()`.

`loadExams()` dùng `JsonArrayRequest` (khác các nơi khác dùng `JsonObjectRequest`) vì API trả về array thẳng, không bọc trong object. Parse response bằng `parseExams()`.

`parseExams(JSONArray response)` loop qua array, tạo từng `AdminExam` với `obj.optInt("examId")`, `obj.optInt("userId")`, `obj.optString("username", "User #" + userId)`, v.v.

`onViewExam(AdminExam exam)` tạo Intent sang `ExamResultActivity` với các Extras từ model.

`onDeleteExam(AdminExam exam)` inflate `DialogConfirmDeleteBinding`, tạo `AlertDialog` với view đó, setup 2 nút. Khi xác nhận → `deleteExam(exam.getExamId())`.

`deleteExam(examId)` DELETE `/api/exam/:id`. Callback success: Toast, `loadExams()`, và `((AdminFragment) getParentFragment()).refreshDashboard()`.

---

# SESSIONMANAGER

SharedPreferences tên `"motorcycle_session"`. Keys: `jwt_token`, `email`, `role`.

`saveSession(email, token, role)` lưu cả 3 giá trị bằng `preferences.edit().putString().apply()`.

`isLoggedIn()` đọc token và trả về `token != null && !token.isEmpty()`.

`clear()` gọi `preferences.edit().clear().apply()` để xóa toàn bộ.

---

# STUDYCARTMANAGER

SharedPreferences tên `"study_cart"`. Key `cart_items` lưu JSONArray dạng String.

`addQuestion(question)` gọi `getCartItems()`, kiểm tra trùng (loop qua list, so sánh `questionId`), nếu chưa có thì add và `saveCart()`.

`removeQuestion(questionId)` gọi `getCartItems()`, `list.removeIf(q -> q.getQuestionId() == questionId)`, `saveCart()`.

`clearCart()` gọi `preferences.edit().remove(KEY_CART_ITEMS).apply()`.

`isInCart(questionId)` đọc list, loop và so sánh questionId.

`getCartCount()` trả về `getCartItems().size()`.

`getCartItems()` đọc String từ SharedPreferences, parse thành JSONArray, loop và tạo `AdminQuestion` cho từng object.

`saveCart(items)` serialize List thành JSONArray rồi lưu bằng `preferences.edit().putString()`.

---

# NOTIFICATIONHELPER

`createNotificationChannels()` được gọi trong constructor. Tạo channel với ID `"study_reminder"`, importance HIGH (có âm thanh, pop-up). Chỉ cần tạo 1 lần, Android tự nhớ.

`scheduleReminder(hour, minute)` lưu settings vào SharedPreferences. Tạo `PendingIntent` trỏ đến `StudyReminderReceiver`. Tính `Calendar` cho thời điểm tiếp theo (nếu đã qua trong ngày thì ngày mai). Gọi `AlarmManager.setRepeating(RTC_WAKEUP, time, INTERVAL_DAY, pendingIntent)`.

`cancelReminder()` set `reminder_enabled = false`, gọi `alarmManager.cancel(pendingIntent)`.

`showStudyReminder()` đọc `StudyCartManager.getCartCount()` để tạo nội dung động. Build `NotificationCompat` và gọi `NotificationManagerCompat.from(context).notify()`.

---

# STUDYREMINDERRECEIVER

BroadcastReceiver. Khi AlarmManager đến giờ, hệ thống gọi `onReceive()`. Receiver chỉ làm một việc: `new NotificationHelper(context).showStudyReminder()`.

Phải khai báo trong AndroidManifest để hệ thống biết Receiver này tồn tại.

---

# APICLIENT

Singleton. `getInstance(context)` trả về instance duy nhất, tạo mới nếu chưa có.

`getRequestQueue()` trả về `RequestQueue` để add request.

`endpoint(path)` ghép base URL (từ `strings.xml`) với path.

`authHeaders(sessionManager)` trả về Map `{ "Content-Type": "application/json", "Authorization": "Bearer <token>" }`.

---

# CÁC ĐIỂM DỄ BỊ HỎI

**Tại sao ExamTakingActivity không dùng ViewBinding?**
Nó dùng `setContentView(R.layout.activity_exam_taking)` và `findViewById()`. Đây là cách cũ hơn nhưng vẫn hoạt động.

**Admin menu ở đâu?**
Admin không dùng file `bottom_nav_menu.xml`. Khi role là Admin, MainActivity gọi `getMenu().clear()` rồi build menu bằng code `getMenu().add()`.

**Làm thế nào Fragment con lấy sessionManager?**
Sub-fragment của Admin (AdminUsersTabFragment, AdminQuestionsTabFragment, AdminExamsTabFragment) gọi `(AdminFragment) getParentFragment()` để lấy reference Fragment cha, rồi gọi `parent.getSessionManager()` và `parent.getApiClient()`.

**Tại sao logout xử lý cả khi API lỗi?**
Để đảm bảo user luôn thoát được. Dù server có lỗi, `sessionManager.clear()` vẫn được gọi.

**Validation tại sao không return sớm khi gặp lỗi đầu tiên?**
Để hiển thị tất cả lỗi cùng lúc thay vì từng lỗi một. Dùng flag `boolean valid` và chỉ `return valid` ở cuối.

**Tại sao override `parseNetworkResponse` trong Volley?**
Để đọc response dạng UTF-8 thay vì encoding mặc định, đảm bảo tiếng Việt hiển thị đúng.

**StudyCartManager lưu dữ liệu ở đâu?**
SharedPreferences tên `"study_cart"`, key `"cart_items"`, lưu JSONArray serialize thành String. Không cần internet, không cần server.

**Badge số lượng trên tab Cart hoạt động thế nào?**
`MainActivity` tạo `BadgeDrawable` và giữ reference. Các Fragment gọi `((MainActivity) getActivity()).refreshCartBadge()` mỗi khi giỏ thay đổi.

**Nhắc nhở hoạt động kể cả khi app đóng không?**
Có. AlarmManager là system service, hoạt động độc lập với app. Khi đến giờ, hệ thống gọi `StudyReminderReceiver.onReceive()` dù app có đang chạy hay không.

**PageSize khác nhau ở đâu?**
QuestionBankFragment dùng `pageSize=200` để lấy đủ câu cho ngân hàng câu hỏi. AdminQuestionsTabFragment dùng `pageSize=50` vì chỉ cần hiển thị một trang để quản lý.

---

# LUỒNG DỮ LIỆU ĐẦY ĐỦ

Luồng thi thử từ User:

```
HomeFragment
    → Click "Tạo đề thi"
    → ExamConfirmActivity
        → Điền thông tin, chọn phương thức thanh toán
        → Click "Thanh toán"
        → delay 2 giây
        → POST /api/exam/generate
        → Nhận {examId, questions[]}
        → lưu generatedQuestionsJson = questions.toString()
        → Click "Bắt đầu thi"
        → Intent sang ExamTakingActivity với EXTRA_EXAM_ID và EXTRA_QUESTIONS_JSON
    → ExamTakingActivity
        → Parse EXTRA_QUESTIONS_JSON thành JSONArray
        → renderQuestion() câu 1
        → User chọn đáp án → onNextClicked() → lưu vào selectedAnswers
        → ... lặp đến câu cuối ...
        → onNextClicked() câu cuối → submitExam()
        → POST /api/exam/submit với {examId, answers:{...}}
        → Nhận {score, totalQuestions, passed, failedByImportantQuestion}
        → Intent sang ExamResultActivity với các extra
        → finish()
    → ExamResultActivity
        → Hiển thị điểm, badge Đậu/Trượt
        → Nếu click "Xem chi tiết" → GET /api/exam/detail/:id
```

Luồng bookmark câu hỏi:

```
QuestionBankFragment
    → GET /api/question?page=1&pageSize=200
    → Hiển thị danh sách với icon bookmark
    → User click icon bookmark
    → onBookmarkToggle(question, addToCart=true)
    → cartManager.addQuestion(question)
    → adapter.updateBookmarks(getBookmarkedIds())  ← icon đổi thành "đã bookmark"
    → updateCartBadge()  ← số badge tăng lên
    
    HOẶC
    
    → User click vào câu hỏi
    → onQuestionClick(question)
    → QuestionDetailActivity
        → Hiển thị chi tiết
        → User click nút "Thêm vào giỏ"
        → cartManager.addQuestion(question)
        → updateCartButtonState()  ← nút đổi thành "Xóa khỏi giỏ"
        → [Khi quay về QuestionBankFragment]
        → onResume() → adapter.updateBookmarks(getBookmarkedIds())  ← icon đồng bộ lại
```

Luồng Admin CRUD bài thi:

```
AdminFragment
    → GET /api/admin/stats  ← hiện 4 con số thống kê
    → setupViewPager()  ← tạo 3 sub-fragment
    → [Tab Bài thi được chọn]
    
AdminExamsTabFragment
    → onViewCreated()
    → (AdminFragment) getParentFragment() → lấy sessionManager và apiClient
    → loadExams()
    → GET /api/exam/admin/all-exams  (JsonArrayRequest)
    → parseExams(JSONArray) → List<AdminExam>
    → adapter.submitList(exams)
    
    → User click "Xóa"
    → onDeleteExam(exam)  ← callback từ adapter
    → inflate DialogConfirmDeleteBinding
    → AlertDialog.Builder.setView(dialogBinding.getRoot()).create().show()
    → User click "Xác nhận"
    → deleteExam(examId)
    → DELETE /api/exam/:id
    → Thành công: loadExams() + getParentFragment().refreshDashboard()
    → refreshDashboard() gọi loadDashboardStats() → cập nhật con số tổng bài thi
```
