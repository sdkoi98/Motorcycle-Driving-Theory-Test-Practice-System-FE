# 📖 TÓM TẮT CÁC HÀM CHÍNH TRONG DỰ ÁN

> **Lưu ý:** Đây là phiên bản tóm tắt. Xem `README_FUNCTIONS.md` để hiểu chi tiết từng hàm.

---

## 🎬 ACTIVITIES - CÁC MÀN HÌNH CHÍNH

### 1. **LoginActivity** - Đăng nhập

| Hàm | Chức năng | Input/Output |
|-----|-----------|--------------|
| `onCreate()` | Khởi tạo màn hình, check đã login chưa | - |
| `validateLoginInput(email, password)` | Validate email/password | `boolean` (valid/invalid) |
| `doLogin(email, password)` | Gọi API `/api/auth/login`, lưu token | - |
| `setLoading(loading)` | Hiển thị/ẩn loading, disable buttons | - |
| `mapAuthError(statusCode, data)` | Convert error thành message dễ hiểu | `String` message |
| `stripQuotes(text)` | Xóa dấu ngoặc kép trong string | `String` |
| `openMain()` | Chuyển đến MainActivity | - |

**Flow:** `onCreate() → validateLoginInput() → doLogin() → openMain()`

---

### 2. **RegisterActivity** - Đăng ký

| Hàm | Chức năng |
|-----|-----------|
| `onCreate()` | Khởi tạo màn hình đăng ký |
| `validateRegisterInput()` | Validate username, email, password, confirmPassword |
| `doSignup()` | Gọi API `/api/auth/signup` |
| `setLoading()` | Hiển thị loading state |
| `mapRegisterError()` | Convert error thành message |

**Điểm khác:** Dùng `StringRequest` thay vì `JsonObjectRequest`

---

### 3. **MainActivity** - Màn hình chính

| Hàm | Chức năng |
|-----|-----------|
| `onCreate()` | Check role (User/Admin), ẩn/hiện tab, setup BottomNavigation |
| `switchFragment(fragment)` | Thay đổi Fragment hiển thị |

**Logic quan trọng:**
```java
if (isAdmin) {
    // Ẩn Home, History → Hiển thị Admin, Profile
} else {
    // Ẩn Admin → Hiển thị Home, History, Profile
}
```

---

### 4. **ExamTakingActivity** - Làm bài thi

| Hàm | Chức năng | Chi tiết |
|-----|-----------|----------|
| `onCreate()` | Nhận examId + questions từ Intent | Parse JSON, setup listeners |
| `renderQuestion()` | Hiển thị câu hỏi tại `currentIndex` | Hiển thị nội dung, 4 đáp án, restore đáp án đã chọn |
| `onNextClicked()` | Xử lý click "Tiếp theo"/"Nộp bài" | Validate → Lưu đáp án → Next/Submit |
| `getSelectedOption()` | Lấy đáp án đã chọn | Return "A", "B", "C", "D" hoặc "" |
| `submitExam()` | Gửi đáp án lên server | POST `/api/exam/submit` → ExamResultActivity |
| `showError(message)` | Hiển thị error với shake animation | UX improvement |
| `hideError()` | Ẩn error message | - |

**Data structure:**
```java
selectedAnswers = {
    "1": "A",    // questionId: selectedAnswer
    "2": "B",
    "3": "C",
    ...
}
```

---

### 5. **ExamResultActivity** - Kết quả bài thi

| Hàm | Chức năng |
|-----|-----------|
| `onCreate()` | Khởi tạo, hiển thị summary |
| `setupToolbar()` | Setup toolbar với nút back |
| `displaySummary()` | Hiển thị điểm, Đậu/Trượt, câu điểm liệt |
| `setupListeners()` | Setup button listeners (View Detail, Back to Home) |
| `loadExamDetail()` | Gọi API `/api/exam/detail/:id` để xem chi tiết |
| `formatDetail(response)` | Format JSON response thành text dễ đọc |

**Flow:** `onCreate() → displaySummary() → (User click "Xem chi tiết") → loadExamDetail() → formatDetail()`

---

## 🧩 FRAGMENTS - CÁC PHẦN MÀN HÌNH

### 1. **HomeFragment** - Tab "Thi thử"

| Hàm | Chức năng |
|-----|-----------|
| `onCreateView()` | Inflate layout |
| `onViewCreated()` | Setup button listener |
| `callGenerateExam()` | Gọi API `/api/exam/generate` → ExamTakingActivity |
| `onDestroyView()` | Clean up (set binding = null) |

**API Response:**
```json
{
    "examId": 123,
    "questions": [ {...}, {...}, ... ]
}
```

---

### 2. **HistoryFragment** - Tab "Lịch sử"

| Hàm | Chức năng |
|-----|-----------|
| `onCreateView()` | Inflate layout |
| `onViewCreated()` | Setup RecyclerView + Adapter, load data |
| `loadHistory()` | Gọi API `/api/exam/history` |
| `showEmptyState(message)` | Hiển thị message khi không có dữ liệu |
| `showHistoryList(data)` | Parse JSON → List<ExamHistory> → Adapter |
| `onDestroyView()` | Clean up |

**Adapter:** `HistoryAdapter` hiển thị danh sách bài thi với badges (Đậu/Trượt)

---

### 3. **ProfileFragment** - Tab "Tài khoản"

| Hàm | Chức năng |
|-----|-----------|
| `onViewCreated()` | Hiển thị email, role, avatar initial |
| `deriveNameFromEmail(email)` | Tạo tên hiển thị từ email |
| `extractInitial(name)` | Lấy chữ cái đầu cho avatar |
| `callLogout(sessionManager)` | Gọi API `/api/auth/logout` → LoginActivity |
| `openLogin()` | Clear session, mở LoginActivity |

**Logic:**
- `test.user@gmail.com` → `Test User` → Avatar "T"

---

### 4. **AdminFragment** - Tab "Bảng điều khiển" (Admin)

| Hàm | Chức năng |
|-----|-----------|
| `onViewCreated()` | Check role, setup ViewPager2 + TabLayout |
| `setupViewPager()` | Add 3 tab: Users, Questions, Exams |
| `loadDashboardStats()` | Gọi API `/api/admin/stats` → Hiển thị thống kê |
| `updateDashboardUI(stats)` | Cập nhật số liệu lên UI |
| `onFabClicked()` | Xử lý click FAB (Floating Action Button) |
| `refreshDashboard()` | Reload stats sau khi CRUD |

**ViewPager2:** Cho phép swipe giữa 3 tab con

---

### 5. **AdminUsersTabFragment** - Tab "Người dùng"

| Hàm | Chức năng |
|-----|-----------|
| `loadUsers()` | GET `/api/admin/users` |
| `parseUsers(response)` | Parse JSON → List<AdminUser> |
| `showCreateUserDialog()` | Hiển thị dialog tạo user |
| `showUserDialog(existingUser)` | Dialog tạo/sửa user (reusable) |
| `createUser(payload)` | POST `/api/admin/users` |
| `updateUser(userId, payload)` | PUT `/api/admin/users/:id` |
| `deleteUser(userId)` | DELETE `/api/admin/users/:id` |
| `sendWriteRequest()` | Helper method gửi request CRUD |
| `onEditUser(user)` | Callback khi click Edit trong RecyclerView |
| `onDeleteUser(user)` | Callback khi click Delete |

**Dialog:** Dùng `DialogUserBinding` (ViewBinding cho dialog_user.xml)

---

### 6. **AdminQuestionsTabFragment** - Tab "Câu hỏi"

| Hàm | Chức năng |
|-----|-----------|
| `loadQuestions()` | GET `/api/question?page=1&pageSize=50` |
| `parseQuestions(items)` | Parse JSON → List<AdminQuestion> |
| `showCreateQuestionDialog()` | Dialog tạo câu hỏi |
| `showQuestionDialog(existingQuestion)` | Dialog tạo/sửa câu hỏi |
| `createQuestion(payload)` | POST `/api/question` |
| `updateQuestion(questionId, payload)` | PUT `/api/question/:id` |
| `deleteQuestion(questionId)` | DELETE `/api/question/:id` |
| `sendWriteRequest()` | Helper method CRUD |
| `onViewQuestion(question)` | Hiển thị dialog xem chi tiết |
| `onEditQuestion(question)` | Callback Edit |
| `onDeleteQuestion(question)` | Callback Delete |

**Dialog:** 3 loại
- Create/Edit: `DialogQuestionBinding`
- View: `DialogViewQuestionBinding`
- Confirm Delete: `DialogConfirmDeleteBinding`

---

### 7. **AdminExamsTabFragment** - Tab "Bài thi"

| Hàm | Chức năng |
|-----|-----------|
| `loadExams()` | GET `/api/exam/admin/all-exams` |
| `parseExams(response)` | Parse JSON → List<AdminExam> |
| `onViewExam(exam)` | Mở ExamResultActivity |
| `onDeleteExam(exam)` | Hiển thị dialog xác nhận xóa |
| `deleteExam(examId)` | DELETE `/api/exam/:id` |

**Lưu ý:** Admin không thể tạo bài thi (FAB bị disable ở tab này)

---

## 🔄 ADAPTERS - RECYCLERVIEW ADAPTERS

### 1. **HistoryAdapter**

**Bind:** `ExamHistory` → `item_history.xml`

| Hàm | Chức năng |
|-----|-----------|
| `submitList(newList)` | Cập nhật danh sách hiển thị |
| `onCreateViewHolder()` | Inflate item layout |
| `onBindViewHolder()` | Bind dữ liệu vào ViewHolder |
| `getItemCount()` | Trả về số lượng items |

**ViewHolder.bind():**
- Set exam number
- Set score
- Set status badge (Đậu/Trượt)
- Set date, duration

---

### 2. **AdminUserAdapter**

**Bind:** `AdminUser` → `item_admin_user.xml`

**Interface:** `OnUserActionListener`
```java
void onEditUser(AdminUser user);
void onDeleteUser(AdminUser user);
```

**Xử lý sự kiện:** Click Edit/Delete → Callback đến Fragment

---

### 3. **AdminQuestionAdapter**

**Bind:** `AdminQuestion` → `item_admin_question.xml`

**Interface:** `OnQuestionActionListener`
```java
void onViewQuestion(AdminQuestion question);
void onEditQuestion(AdminQuestion question);
void onDeleteQuestion(AdminQuestion question);
```

---

### 4. **AdminExamAdapter**

**Bind:** `AdminExam` → `item_admin_exam.xml`

**Interface:** `OnExamActionListener`
```java
void onViewExam(AdminExam exam);
void onDeleteExam(AdminExam exam);
```

---

## 📦 MODELS - DATA OBJECTS

### 1. **ExamHistory**

```java
int examId;
int score;
int totalQuestions;
boolean passed;
String date;
int duration;
```

---

### 2. **AdminUser**

```java
int userId;
String username;
String email;
String role;  // "User" hoặc "Admin"
```

---

### 3. **AdminQuestion**

```java
int questionId;
String content;
String answerA, answerB, answerC, answerD;
String correctAnswer;  // "A", "B", "C", "D"
int categoryId;
boolean isImportant;  // Câu điểm liệt
```

---

### 4. **AdminExam**

```java
int examId;
int userId;
int score;
int totalQuestions;
boolean isPassed;
String examDate;
```

---

## 🌐 NETWORK & UTILS

### **ApiClient** (Singleton)

| Hàm | Chức năng |
|-----|-----------|
| `getInstance(context)` | Lấy/tạo singleton instance |
| `getRequestQueue()` | Trả về Volley RequestQueue |
| `getBaseUrl()` | Trả về base URL từ strings.xml |
| `endpoint(path)` | Tạo full URL (baseUrl + path) |
| `authHeaders(sessionManager)` | Tạo headers với Authorization token |

**Example:**
```java
ApiClient apiClient = ApiClient.getInstance(context);
String url = apiClient.endpoint("/api/exam/generate");
// → "http://10.0.2.2:5000/api/exam/generate"

Map<String, String> headers = ApiClient.authHeaders(sessionManager);
// → { "Content-Type": "application/json", "Authorization": "Bearer <token>" }
```

---

### **SessionManager**

| Hàm | Chức năng |
|-----|-----------|
| `saveSession(email, token, role)` | Lưu session vào SharedPreferences |
| `getToken()` | Lấy JWT token |
| `getEmail()` | Lấy email |
| `getRole()` | Lấy role ("User"/"Admin") |
| `isLoggedIn()` | Check token có tồn tại không |
| `clear()` | Xóa session (logout) |

**Storage:** SharedPreferences với name `"motorcycle_session"`

**Keys:**
- `jwt_token`: JWT token
- `email`: Email người dùng
- `role`: User role

---

### **AdminViewPagerAdapter**

**Mục đích:** Adapter cho ViewPager2 trong AdminFragment

| Hàm | Chức năng |
|-----|-----------|
| `addFragment(fragment, title)` | Thêm Fragment vào danh sách |
| `createFragment(position)` | Trả về Fragment tại vị trí position |
| `getItemCount()` | Số lượng Fragments |
| `getPageTitle(position)` | Trả về title của tab |

---

## 🔑 CÁC KHÁI NIỆM QUAN TRỌNG

### 1. **ViewBinding**

**Lợi ích:**
- Type-safe: Không bị lỗi cast type
- Null-safe: Không bị NullPointerException
- Tự động generate: Không cần viết findViewById()

**Cách dùng:**
```java
// Activity
binding = ActivityLoginBinding.inflate(getLayoutInflater());
setContentView(binding.getRoot());

// Fragment
binding = FragmentHomeBinding.inflate(inflater, container, false);
return binding.getRoot();
```

**Clean up:**
```java
@Override
public void onDestroyView() {
    super.onDestroyView();
    binding = null;  // Tránh memory leak
}
```

---

### 2. **Volley Request Flow**

```
1. Tạo Request object (JsonObjectRequest, StringRequest, ...)
2. Định nghĩa success callback
3. Định nghĩa error callback
4. (Optional) Override headers, parseNetworkResponse, getBody
5. Add request vào RequestQueue
6. Volley tự động:
   - Chạy request trên background thread
   - Parse response
   - Gọi callback trên main thread
```

---

### 3. **RecyclerView Pattern**

```
Data (List<Model>)
    ↓
Adapter.submitList(data)
    ↓
Adapter.onBindViewHolder(holder, position)
    ↓
ViewHolder.bind(model)
    ↓
Set data vào Views (TextView, ImageView, ...)
    ↓
RecyclerView hiển thị
```

---

### 4. **Fragment Lifecycle (Simplified)**

```
onCreateView()           → Inflate layout
    ↓
onViewCreated()          → Setup listeners, load data
    ↓
... (User tương tác)
    ↓
onDestroyView()          → Clean up (set binding = null)
```

---

### 5. **Intent & Extras**

**Truyền data giữa Activities:**

```java
// Activity A (Sender)
Intent intent = new Intent(this, ActivityB.class);
intent.putExtra("key", value);
startActivity(intent);

// Activity B (Receiver)
int value = getIntent().getIntExtra("key", defaultValue);
```

---

### 6. **Dialog Pattern**

```java
// 1. Inflate dialog binding
DialogUserBinding dialogBinding = DialogUserBinding.inflate(getLayoutInflater());

// 2. Setup dialog content
dialogBinding.tvDialogTitle.setText("Title");

// 3. Tạo AlertDialog
AlertDialog dialog = new AlertDialog.Builder(requireContext())
    .setView(dialogBinding.getRoot())
    .create();

// 4. Setup button listeners
dialogBinding.btnSave.setOnClickListener(v -> {
    // Xử lý
    dialog.dismiss();
});

// 5. Show dialog
dialog.show();
```

---

## 📊 API ENDPOINTS SUMMARY

| Endpoint | Method | Mục đích | Auth |
|----------|--------|----------|------|
| `/api/auth/login` | POST | Đăng nhập | ❌ |
| `/api/auth/signup` | POST | Đăng ký | ❌ |
| `/api/auth/logout` | POST | Đăng xuất | ✅ |
| `/api/exam/generate` | POST | Tạo đề thi | ✅ |
| `/api/exam/submit` | POST | Nộp bài | ✅ |
| `/api/exam/history` | GET | Lịch sử thi | ✅ |
| `/api/exam/detail/:id` | GET | Chi tiết bài thi | ✅ |
| `/api/exam/:id` | DELETE | Xóa bài thi | ✅ (Admin) |
| `/api/exam/admin/all-exams` | GET | Tất cả bài thi | ✅ (Admin) |
| `/api/question` | GET | Danh sách câu hỏi | ❌ |
| `/api/question` | POST | Tạo câu hỏi | ✅ (Admin) |
| `/api/question/:id` | PUT | Sửa câu hỏi | ✅ (Admin) |
| `/api/question/:id` | DELETE | Xóa câu hỏi | ✅ (Admin) |
| `/api/admin/stats` | GET | Thống kê | ✅ (Admin) |
| `/api/admin/users` | GET | Danh sách users | ✅ (Admin) |
| `/api/admin/users` | POST | Tạo user | ✅ (Admin) |
| `/api/admin/users/:id` | PUT | Sửa user | ✅ (Admin) |
| `/api/admin/users/:id` | DELETE | Xóa user | ✅ (Admin) |

**Auth:** ✅ = Cần Authorization header

---

## 🎯 LUỒNG DỮ LIỆU CHÍNH

### **User Flow:**

```
Login → MainActivity
    → HomeFragment: Generate Exam
        → ExamTakingActivity: Làm bài
            → ExamResultActivity: Xem kết quả
                → Back to Home
    → HistoryFragment: Xem lịch sử
    → ProfileFragment: Logout
```

---

### **Admin Flow:**

```
Login → MainActivity
    → AdminFragment
        → AdminUsersTabFragment: CRUD Users
        → AdminQuestionsTabFragment: CRUD Questions
        → AdminExamsTabFragment: View/Delete Exams
    → ProfileFragment: Logout
```

---

## 💡 MẸO & BEST PRACTICES

### **1. Error Handling**

```java
// ✅ Good
if (error.networkResponse != null) {
    int status = error.networkResponse.statusCode;
    // Xử lý theo status code
} else {
    // Network error
}

// ❌ Bad: Không check null
int status = error.networkResponse.statusCode;  // NullPointerException
```

---

### **2. JSON Parsing**

```java
// ✅ Good: Dùng optXxx() với default value
String email = response.optString("email", "");
int score = response.optInt("score", 0);

// ❌ Bad: Dùng getXxx() có thể throw exception
String email = response.getString("email");  // JSONException nếu không có key
```

---

### **3. ViewBinding Cleanup**

```java
// ✅ Good: Set null trong onDestroyView() (Fragment) hoặc onDestroy() (Activity)
@Override
public void onDestroyView() {
    super.onDestroyView();
    binding = null;
}

// ❌ Bad: Không set null → Memory leak
```

---

### **4. Loading State**

```java
// ✅ Good: Disable button khi đang load
button.setEnabled(false);
progressBar.setVisibility(View.VISIBLE);
// ... call API ...
button.setEnabled(true);  // Re-enable sau khi xong

// ❌ Bad: Không disable → User spam click
```

---

### **5. Input Validation**

```java
// ✅ Good: Validate tất cả fields cùng lúc
boolean valid = true;
if (email.isEmpty()) {
    tilEmail.setError("...");
    valid = false;
}
if (password.isEmpty()) {
    tilPassword.setError("...");
    valid = false;
}
return valid;

// ❌ Bad: Return ngay khi gặp lỗi đầu tiên
if (email.isEmpty()) {
    return false;  // User không biết password cũng lỗi
}
```

---

## 📚 TÀI LIỆU THAM KHẢO

- **Android Docs:** https://developer.android.com/docs
- **Volley:** https://developer.android.com/training/volley
- **Material Design:** https://material.io/develop/android
- **ViewBinding:** https://developer.android.com/topic/libraries/view-binding

---

**🎉 Chúc bạn học tốt và hiểu rõ ứng dụng của mình!**

> **Ghi chú:** File này tóm tắt các khái niệm chính. Nếu cần hiểu sâu hơn về logic của từng hàm, vui lòng đọc code trực tiếp và debug để xem flow thực tế.
