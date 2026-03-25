# 📱 CẤU TRÚC DỰ ÁN - HỆ THỐNG THI LÝ THUYẾT LÁI XE MÔ TÔ

---

## 🎯 TỔNG QUAN DỰ ÁN

**Tên dự án:** Motorcycle Driving Theory Test Practice System (Frontend - Android)

**Mục đích:** Ứng dụng Android hỗ trợ người dùng ôn tập và thi thử lý thuyết lái xe mô tô, với giao diện cho cả **User** (người dùng thường) và **Admin** (quản trị viên).

**Công nghệ:**
- **Ngôn ngữ:** Java
- **Framework:** Android SDK
- **Thư viện chính:** 
  - Volley (Network requests)
  - Material Design Components (UI)
  - ViewBinding (View access)
  - RecyclerView (Danh sách động)
  - ViewPager2 + TabLayout (Navigation)

---

## 📁 CẤU TRÚC THƯ MỤC CHÍNH

```
Motorcycle-Driving-Theory-Test-Practice-System-FE/
│
├── app/                                    # Module chính của ứng dụng
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/motorcycletheory/
│   │   │   │   ├── activities/            # Các Activity (Màn hình chính)
│   │   │   │   ├── fragments/             # Các Fragment (Phần màn hình)
│   │   │   │   ├── adapters/              # Adapter cho RecyclerView
│   │   │   │   ├── models/                # Data models (POJO)
│   │   │   │   ├── network/               # API client
│   │   │   │   └── utils/                 # Các class tiện ích
│   │   │   │
│   │   │   ├── res/                       # Resources (Giao diện, màu, string)
│   │   │   │   ├── anim/                  # Animation files
│   │   │   │   ├── drawable/              # Drawables (icons, shapes, backgrounds)
│   │   │   │   ├── layout/                # XML layouts
│   │   │   │   ├── menu/                  # Menu files
│   │   │   │   └── values/                # Colors, strings, themes
│   │   │   │
│   │   │   └── AndroidManifest.xml        # Manifest file (Cấu hình app)
│   │   │
│   │   ├── androidTest/                   # Integration tests
│   │   └── test/                          # Unit tests
│   │
│   ├── build.gradle.kts                   # Gradle config cho module app
│   └── proguard-rules.pro                 # ProGuard rules (bảo mật code)
│
├── gradle/                                # Gradle wrapper
│   └── libs.versions.toml                 # Version catalog (quản lý dependencies)
│
├── build.gradle.kts                       # Gradle config cấp project
├── settings.gradle.kts                    # Gradle settings
├── gradle.properties                      # Gradle properties
├── local.properties                       # Local SDK path (không commit)
└── README.md                              # File mô tả dự án
```

---

## 🗂️ CHI TIẾT CÁC THƯ MỤC VÀ FILE

---

### 📂 `app/src/main/java/com/example/motorcycletheory/`

#### **1️⃣ `activities/` - Các Activity (Màn hình chính)**

Activity là thành phần chính của Android app, mỗi Activity đại diện cho một màn hình.

| File | Mục đích | Chức năng chính |
|------|----------|----------------|
| `LoginActivity.java` | Màn hình đăng nhập | - Validate email/password<br>- Gọi API `/api/auth/login`<br>- Lưu token vào SessionManager<br>- Chuyển đến MainActivity |
| `RegisterActivity.java` | Màn hình đăng ký | - Validate thông tin đăng ký<br>- Gọi API `/api/auth/signup`<br>- Quay về màn hình đăng nhập |
| `MainActivity.java` | Màn hình chính | - Chứa Bottom Navigation<br>- Quản lý các Fragment (Home, History, Admin, Profile)<br>- Xử lý navigation giữa các tab<br>- Ẩn/hiện tab dựa vào role (User/Admin) |
| `ExamTakingActivity.java` | Màn hình làm bài thi | - Hiển thị từng câu hỏi<br>- Lưu đáp án đã chọn<br>- Validate câu trả lời<br>- Submit bài thi khi hoàn thành |
| `ExamResultActivity.java` | Màn hình kết quả | - Hiển thị điểm số<br>- Hiển thị trạng thái (Đậu/Trượt)<br>- Xem chi tiết từng câu trả lời |

---

#### **2️⃣ `fragments/` - Các Fragment (Phần màn hình)**

Fragment là thành phần UI có thể tái sử dụng, nằm trong Activity.

| File | Màn hình | Chức năng |
|------|----------|-----------|
| `HomeFragment.java` | Tab "Thi thử" | - Hiển thị nút "Tạo đề thi mới"<br>- Gọi API `/api/exam/generate`<br>- Chuyển đến ExamTakingActivity |
| `HistoryFragment.java` | Tab "Lịch sử" | - Hiển thị danh sách bài thi đã làm<br>- Gọi API `/api/exam/history`<br>- Hiển thị điểm, trạng thái, thời gian |
| `ProfileFragment.java` | Tab "Tài khoản" | - Hiển thị thông tin user (email, role)<br>- Nút đăng xuất<br>- Gọi API `/api/auth/logout` |
| `AdminFragment.java` | Tab "Bảng điều khiển" | - Hiển thị thống kê (tổng user, câu hỏi, bài thi)<br>- Quản lý ViewPager2 với 3 tab con (Users, Questions, Exams)<br>- Hiển thị FAB để thêm mới |
| `AdminUsersTabFragment.java` | Tab "Người dùng" (Admin) | - Hiển thị danh sách users<br>- CRUD: Tạo, sửa, xóa user<br>- Gọi API `/api/admin/users` |
| `AdminQuestionsTabFragment.java` | Tab "Câu hỏi" (Admin) | - Hiển thị danh sách câu hỏi<br>- CRUD: Tạo, sửa, xóa câu hỏi<br>- Gọi API `/api/question` |
| `AdminExamsTabFragment.java` | Tab "Bài thi" (Admin) | - Hiển thị danh sách bài thi<br>- Xem chi tiết, xóa bài thi<br>- Gọi API `/api/exam/admin/all-exams` |

---

#### **3️⃣ `adapters/` - RecyclerView Adapters**

Adapter là cầu nối giữa dữ liệu và RecyclerView (danh sách hiển thị).

| File | Dùng cho | Chức năng |
|------|----------|-----------|
| `HistoryAdapter.java` | HistoryFragment | - Hiển thị danh sách lịch sử thi<br>- Bind dữ liệu từ `ExamHistory` model vào `item_history.xml` |
| `AdminUserAdapter.java` | AdminUsersTabFragment | - Hiển thị danh sách users<br>- Xử lý sự kiện Edit/Delete user<br>- Bind dữ liệu từ `AdminUser` model vào `item_admin_user.xml` |
| `AdminQuestionAdapter.java` | AdminQuestionsTabFragment | - Hiển thị danh sách câu hỏi<br>- Xử lý sự kiện View/Edit/Delete question<br>- Bind dữ liệu từ `AdminQuestion` model vào `item_admin_question.xml` |
| `AdminExamAdapter.java` | AdminExamsTabFragment | - Hiển thị danh sách bài thi<br>- Xử lý sự kiện View/Delete exam<br>- Bind dữ liệu từ `AdminExam` model vào `item_admin_exam.xml` |

---

#### **4️⃣ `models/` - Data Models (POJO)**

Model là class đại diện cho dữ liệu, theo mô hình POJO (Plain Old Java Object).

| File | Mô tả | Thuộc tính chính |
|------|-------|------------------|
| `ExamHistory.java` | Lịch sử thi của user | `examId`, `score`, `totalQuestions`, `passed`, `date`, `duration` |
| `AdminUser.java` | Thông tin user (Admin view) | `userId`, `username`, `email`, `role` |
| `AdminQuestion.java` | Thông tin câu hỏi (Admin view) | `questionId`, `content`, `answerA/B/C/D`, `correctAnswer`, `categoryId`, `isImportant` |
| `AdminExam.java` | Thông tin bài thi (Admin view) | `examId`, `userId`, `score`, `totalQuestions`, `isPassed`, `examDate` |

---

#### **5️⃣ `network/` - API Client**

| File | Mục đích |
|------|----------|
| `ApiClient.java` | - Singleton pattern để quản lý Volley RequestQueue<br>- Cung cấp `baseUrl` từ `strings.xml`<br>- Phương thức `endpoint(path)` để tạo full URL<br>- Phương thức `authHeaders()` để thêm Authorization header |

**Vai trò:**
- Tập trung quản lý tất cả các request HTTP
- Đảm bảo chỉ có 1 instance của RequestQueue (tiết kiệm tài nguyên)
- Tự động thêm `Authorization: Bearer <token>` vào header

---

#### **6️⃣ `utils/` - Các class tiện ích**

| File | Mục đích |
|------|----------|
| `SessionManager.java` | - Quản lý session (đăng nhập/đăng xuất)<br>- Lưu/đọc token, email, role từ SharedPreferences<br>- Kiểm tra trạng thái đăng nhập |
| `AdminViewPagerAdapter.java` | - Adapter cho ViewPager2 trong AdminFragment<br>- Quản lý 3 tab: Users, Questions, Exams |
| `SimpleTextAdapter.java` | - (Nếu có) Adapter đơn giản cho Spinner hoặc list text |

**Chi tiết SessionManager:**
- **SharedPreferences:** Lưu trữ dữ liệu key-value cục bộ trên thiết bị
- **Key lưu trữ:**
  - `jwt_token`: JWT token từ backend
  - `email`: Email người dùng
  - `role`: "User" hoặc "Admin"

---

### 📂 `app/src/main/res/` - Resources

#### **1️⃣ `anim/` - Animation**

| File | Mô tả |
|------|-------|
| `shake.xml` | Animation rung (dùng khi validation lỗi trong ExamTakingActivity) |

---

#### **2️⃣ `drawable/` - Drawables (Hình ảnh, Icon, Shape)**

**🎨 Badges (Nhãn hiển thị):**
- `badge_pass.xml`: Nhãn "ĐẬU" (màu xanh lá)
- `badge_fail.xml`: Nhãn "TRƯỢT" (màu đỏ)
- `badge_category.xml`: Nhãn danh mục câu hỏi
- `badge_important.xml`: Nhãn "Câu điểm liệt"
- `badge_answer.xml`: Nhãn đáp án
- `badge_role.xml`: Nhãn vai trò (User/Admin)

**🖼️ Shapes & Borders:**
- `border_view_content.xml`: Viền cho nội dung
- `circle_avatar.xml`: Hình tròn cho avatar

**🔙 Icons:**
- `ic_back.xml`: Icon mũi tên quay lại

---

#### **3️⃣ `layout/` - XML Layouts**

**Activities:**
- `activity_login.xml`: Giao diện đăng nhập
- `activity_register.xml`: Giao diện đăng ký
- `activity_main.xml`: Giao diện chính (chứa FragmentContainer + BottomNavigation)
- `activity_exam_taking.xml`: Giao diện làm bài thi
- `activity_exam_result.xml`: Giao diện kết quả

**Fragments:**
- `fragment_home.xml`: Tab "Thi thử"
- `fragment_history.xml`: Tab "Lịch sử"
- `fragment_profile.xml`: Tab "Tài khoản"
- `fragment_admin.xml`: Tab "Bảng điều khiển" (Admin)

**Admin Tabs:**
- `admin_tab_users.xml`: Layout cho tab Users
- `admin_tab_questions.xml`: Layout cho tab Questions
- `admin_tab_exams.xml`: Layout cho tab Exams

**RecyclerView Items:**
- `item_history.xml`: Item cho lịch sử thi
- `item_admin_user.xml`: Item cho danh sách users
- `item_admin_question.xml`: Item cho danh sách câu hỏi
- `item_admin_exam.xml`: Item cho danh sách bài thi

**Dialogs:**
- `dialog_user.xml`: Dialog tạo/sửa user
- `dialog_question.xml`: Dialog tạo/sửa câu hỏi
- `dialog_view_question.xml`: Dialog xem chi tiết câu hỏi
- `dialog_confirm_delete.xml`: Dialog xác nhận xóa

---

#### **4️⃣ `menu/` - Menu**

| File | Mô tả |
|------|-------|
| `bottom_nav_menu.xml` | Menu cho Bottom Navigation (Home, History, Admin, Profile) |

---

#### **5️⃣ `values/` - Values (Màu, Chuỗi, Theme)**

| File | Mô tả |
|------|-------|
| `colors.xml` | Định nghĩa màu sắc cho app (primary, secondary, error, text, background, ...) |
| `strings.xml` | Tất cả chuỗi text trong app (đã localize tiếng Việt) |
| `themes.xml` | Theme của app (Material Design, AppBar style, ...) |

**Vai trò:**
- **colors.xml:** Quản lý màu sắc tập trung, dễ thay đổi theme
- **strings.xml:** Hỗ trợ đa ngôn ngữ, tránh hard-code text trong code Java
- **themes.xml:** Định nghĩa style chung cho toàn app

---

### 📄 `AndroidManifest.xml`

**Mục đích:** File cấu hình chính của ứng dụng Android

**Nội dung chính:**
- Khai báo các Activity
- Khai báo permissions (INTERNET)
- Thiết lập Activity launcher (màn hình đầu tiên)
- Cấu hình app name, icon

**Ví dụ:**
```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:label="@string/app_name"
        android:theme="@style/Theme.MotorcycleTheory">
        
        <activity android:name=".activities.LoginActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity android:name=".activities.MainActivity" />
        ...
    </application>
</manifest>
```

---

## 🔄 LUỒNG HOẠT ĐỘNG CHÍNH (FLOW)

### **1. Luồng User (Người dùng thường)**

```
LoginActivity (Đăng nhập)
    ↓ (Nhập email/password)
    ↓ API: /api/auth/login
    ↓ (Lưu token, email, role = "User")
    ↓
MainActivity (Bottom Nav: Home | History | Profile)
    │
    ├── HomeFragment (Tab "Thi thử")
    │       ↓ (Click "Tạo đề thi mới")
    │       ↓ API: /api/exam/generate
    │       ↓ (Nhận examId + questions JSON)
    │       ↓
    │   ExamTakingActivity (Làm bài)
    │       ↓ (Chọn đáp án từng câu)
    │       ↓ API: /api/exam/submit
    │       ↓
    │   ExamResultActivity (Kết quả)
    │       ↓ (Hiển thị điểm, Đậu/Trượt)
    │       ↓ API: /api/exam/detail/:id (nếu xem chi tiết)
    │       ↓ (Quay về Home)
    │
    ├── HistoryFragment (Tab "Lịch sử")
    │       ↓ API: /api/exam/history
    │       ↓ (Hiển thị danh sách bài thi đã làm)
    │
    └── ProfileFragment (Tab "Tài khoản")
            ↓ (Hiển thị email, role)
            ↓ (Click "Đăng xuất")
            ↓ API: /api/auth/logout
            ↓ (Clear session)
            ↓ LoginActivity
```

---

### **2. Luồng Admin (Quản trị viên)**

```
LoginActivity (Đăng nhập)
    ↓ (Nhập email/password của Admin)
    ↓ API: /api/auth/login
    ↓ (Lưu token, email, role = "Admin")
    ↓
MainActivity (Bottom Nav: Admin | Profile)
    │   (Tab Home và History bị ẩn)
    │
    ├── AdminFragment (Tab "Bảng điều khiển")
    │       ↓ API: /api/admin/stats
    │       ↓ (Hiển thị thống kê: Tổng user, câu hỏi, bài thi)
    │       ↓
    │       ├── AdminUsersTabFragment (Tab "Người dùng")
    │       │       ↓ API: /api/admin/users (GET)
    │       │       ↓ (Hiển thị danh sách users)
    │       │       ↓ (Click "Thêm" → Dialog tạo user)
    │       │       ↓ API: /api/admin/users (POST)
    │       │       ↓ (Click "Sửa" → Dialog sửa user)
    │       │       ↓ API: /api/admin/users/:id (PUT)
    │       │       ↓ (Click "Xóa" → Dialog xác nhận)
    │       │       ↓ API: /api/admin/users/:id (DELETE)
    │       │
    │       ├── AdminQuestionsTabFragment (Tab "Câu hỏi")
    │       │       ↓ API: /api/question?page=1&pageSize=50 (GET)
    │       │       ↓ (Hiển thị danh sách câu hỏi)
    │       │       ↓ (Click "Thêm" → Dialog tạo câu hỏi)
    │       │       ↓ API: /api/question (POST)
    │       │       ↓ (Click "Xem" → Dialog xem chi tiết)
    │       │       ↓ (Click "Sửa" → Dialog sửa câu hỏi)
    │       │       ↓ API: /api/question/:id (PUT)
    │       │       ↓ (Click "Xóa" → Dialog xác nhận)
    │       │       ↓ API: /api/question/:id (DELETE)
    │       │
    │       └── AdminExamsTabFragment (Tab "Bài thi")
    │               ↓ API: /api/exam/admin/all-exams (GET)
    │               ↓ (Hiển thị danh sách bài thi)
    │               ↓ (Click "Xem" → ExamResultActivity)
    │               ↓ (Click "Xóa" → Dialog xác nhận)
    │               ↓ API: /api/exam/:id (DELETE)
    │
    └── ProfileFragment (Tab "Tài khoản")
            ↓ (Giống User)
```

---

## 🌐 API ENDPOINTS ĐƯỢC SỬ DỤNG

### **Authentication (Xác thực)**

| Endpoint | Method | Mục đích | Body | Response |
|----------|--------|----------|------|----------|
| `/api/auth/login` | POST | Đăng nhập | `{ email, password }` | `{ token, email, role }` |
| `/api/auth/signup` | POST | Đăng ký | `{ username, email, password }` | Success message |
| `/api/auth/logout` | POST | Đăng xuất | - | Success message |

### **Exam (Bài thi)**

| Endpoint | Method | Mục đích | Headers | Response |
|----------|--------|----------|---------|----------|
| `/api/exam/generate` | POST | Tạo đề thi mới | `Authorization: Bearer <token>` | `{ examId, questions[] }` |
| `/api/exam/submit` | POST | Nộp bài thi | `Authorization: Bearer <token>` | `{ score, totalQuestions, passed, failedByImportantQuestion }` |
| `/api/exam/history` | GET | Lịch sử thi | `Authorization: Bearer <token>` | `[ { examId, score, totalQuestions, isPassed, completedAt }, ... ]` |
| `/api/exam/detail/:id` | GET | Chi tiết bài thi | `Authorization: Bearer <token>` | `{ questions: [ { questionId, userAnswer, correctAnswer }, ... ] }` |
| `/api/exam/:id` | DELETE | Xóa bài thi (Admin) | `Authorization: Bearer <token>` | Success message |
| `/api/exam/admin/all-exams` | GET | Tất cả bài thi (Admin) | `Authorization: Bearer <token>` | `[ { examId, userId, score, totalQuestions, isPassed, examDate }, ... ]` |

### **Question (Câu hỏi)**

| Endpoint | Method | Mục đích | Headers | Response |
|----------|--------|----------|---------|----------|
| `/api/question?page=1&pageSize=50` | GET | Danh sách câu hỏi | `Authorization: Bearer <token>` | `{ items: [ {...}, ... ], totalCount, pageSize, currentPage }` |
| `/api/question` | POST | Tạo câu hỏi mới (Admin) | `Authorization: Bearer <token>` | Success message |
| `/api/question/:id` | PUT | Sửa câu hỏi (Admin) | `Authorization: Bearer <token>` | Success message |
| `/api/question/:id` | DELETE | Xóa câu hỏi (Admin) | `Authorization: Bearer <token>` | Success message |

### **Admin**

| Endpoint | Method | Mục đích | Headers | Response |
|----------|--------|----------|---------|----------|
| `/api/admin/stats` | GET | Thống kê dashboard | `Authorization: Bearer <token>` | `{ totalUsers, totalQuestions, totalImportantQuestions, totalExams }` |
| `/api/admin/users` | GET | Danh sách users | `Authorization: Bearer <token>` | `[ { userId, username, email, role }, ... ]` |
| `/api/admin/users` | POST | Tạo user mới | `Authorization: Bearer <token>` | Success message |
| `/api/admin/users/:id` | PUT | Sửa user | `Authorization: Bearer <token>` | Success message |
| `/api/admin/users/:id` | DELETE | Xóa user | `Authorization: Bearer <token>` | Success message |

---

## 🔧 CÁC CÔNG NGHỆ & THƯ VIỆN SỬ DỤNG

### **1. Volley - HTTP Library**

**Mục đích:** Gửi/nhận HTTP requests (GET, POST, PUT, DELETE)

**Các class chính:**
- `RequestQueue`: Hàng đợi xử lý requests
- `JsonObjectRequest`: Request trả về JSON Object
- `JsonArrayRequest`: Request trả về JSON Array
- `StringRequest`: Request trả về String

**Ví dụ:**
```java
JsonObjectRequest request = new JsonObjectRequest(
    Request.Method.POST,
    url,
    payload,
    response -> {
        // Success callback
    },
    error -> {
        // Error callback
    }
);
apiClient.getRequestQueue().add(request);
```

---

### **2. Material Design Components**

**Mục đích:** Cung cấp UI components đẹp, chuẩn Material Design

**Các components được dùng:**
- `MaterialToolbar`: Thanh tiêu đề
- `MaterialButton`: Nút bấm
- `MaterialCardView`: Card hiển thị nội dung
- `TextInputLayout`: Input field với label và error
- `TabLayout`: Tab navigation
- `ViewPager2`: Swipe giữa các trang
- `FloatingActionButton (FAB)`: Nút hành động nổi
- `BottomNavigationView`: Bottom navigation bar

---

### **3. ViewBinding**

**Mục đích:** Truy cập View trong XML một cách type-safe (không dùng `findViewById`)

**Cách hoạt động:**
- Android tự động tạo binding class cho mỗi layout XML
- Ví dụ: `activity_login.xml` → `ActivityLoginBinding`
- Sử dụng: `binding.etEmail.getText()`

**Lợi ích:**
- Tránh lỗi `NullPointerException`
- Code ngắn gọn hơn
- Compile-time safety

---

### **4. RecyclerView + Adapter Pattern**

**Mục đích:** Hiển thị danh sách dữ liệu động, hiệu năng cao

**Cách hoạt động:**
- **RecyclerView:** Container hiển thị danh sách
- **Adapter:** Bind dữ liệu vào từng item
- **ViewHolder:** Giữ reference đến các View trong item

**Quy trình:**
```
Data (List<Model>) 
  → Adapter.onBindViewHolder() 
  → ViewHolder 
  → Item View (XML)
  → RecyclerView hiển thị
```

---

### **5. SharedPreferences (SessionManager)**

**Mục đích:** Lưu trữ dữ liệu nhỏ cục bộ trên thiết bị

**Cách hoạt động:**
- Lưu dữ liệu dạng key-value
- Dữ liệu tồn tại ngay cả khi tắt app
- Phù hợp cho: token, email, settings, ...

**Ví dụ:**
```java
SharedPreferences prefs = context.getSharedPreferences("motorcycle_session", Context.MODE_PRIVATE);
prefs.edit().putString("jwt_token", token).apply();
String token = prefs.getString("jwt_token", "");
```

---

## 📊 DESIGN PATTERNS ĐƯỢC SỬ DỤNG

### **1. Singleton Pattern**
- **Class:** `ApiClient`
- **Mục đích:** Đảm bảo chỉ có 1 instance của RequestQueue

### **2. Adapter Pattern**
- **Class:** Tất cả các Adapter (`HistoryAdapter`, `AdminUserAdapter`, ...)
- **Mục đích:** Bind dữ liệu vào RecyclerView

### **3. Observer Pattern**
- **Where:** RecyclerView Adapter
- **Mục đích:** Tự động cập nhật UI khi dữ liệu thay đổi (`notifyDataSetChanged()`)

### **4. ViewHolder Pattern**
- **Where:** Bên trong mỗi Adapter
- **Mục đích:** Tối ưu hiệu năng bằng cách cache reference đến Views

### **5. Callback/Listener Pattern**
- **Where:** `OnUserActionListener`, `OnQuestionActionListener`, ...
- **Mục đích:** Xử lý sự kiện từ RecyclerView item (click Edit, Delete, ...)

---

## 🎨 QUY TẮC UI/UX ĐƯỢC ÁP DỤNG

### **1. Material Design Principles**
- Sử dụng elevation (shadow) cho cards
- Ripple effect khi click button
- Consistent spacing (16dp, 24dp)

### **2. Responsive Layouts**
- `android:fitsSystemWindows="true"`: Tránh bị status bar/notch che
- `ConstraintLayout`: Layout linh hoạt, tối ưu hiệu năng

### **3. Error Handling**
- Validation trước khi submit form
- Hiển thị error message rõ ràng (TextInputLayout, TextView với animation)
- Toast message cho thông báo nhanh

### **4. Loading States**
- ProgressBar khi loading data
- Disable button khi đang xử lý request
- Empty state khi không có dữ liệu

### **5. Navigation**
- Bottom Navigation cho main tabs
- Toolbar với back button
- Tab Layout cho sub-sections (Admin)

---

## 🛡️ BẢO MẬT & XÁC THỰC

### **1. JWT Token**
- Token được lưu trong SharedPreferences
- Tự động thêm vào header `Authorization: Bearer <token>`
- Backend verify token trước khi xử lý request

### **2. Role-Based Access**
- User role = "User": Chỉ thấy Home, History, Profile
- User role = "Admin": Chỉ thấy Admin, Profile
- Backend cũng check role trước khi cho phép CRUD

### **3. Input Validation**
- Email format check (Patterns.EMAIL_ADDRESS)
- Password minimum length (6 chars)
- Required fields check

---

## 📝 LƯU Ý QUAN TRỌNG

### **1. Base URL**
- Được định nghĩa trong `res/values/strings.xml`
- Key: `api_base_url`
- Có thể thay đổi dễ dàng (local dev, production)

### **2. UTF-8 Encoding**
- Tất cả request/response dùng UTF-8
- Hỗ trợ tiếng Việt có dấu
- `StandardCharsets.UTF_8` được dùng khi parse response

### **3. Network Error Handling**
- Check `error.networkResponse.statusCode`
- 401: Token hết hạn (redirect to login)
- 400: Bad request (validation error)
- 404: Not found
- 500: Server error

### **4. Memory Management**
- ViewBinding được set `null` trong `onDestroyView()`
- Tránh memory leak

---

## 🚀 HƯỚNG DẪN CHẠY DỰ ÁN

### **Yêu cầu:**
- Android Studio (phiên bản mới nhất)
- JDK 11+
- Android SDK API 24+ (Android 7.0 trở lên)
- Device/Emulator

### **Các bước:**

1. **Clone project:**
   ```bash
   git clone <repository-url>
   ```

2. **Mở project trong Android Studio**

3. **Cấu hình Base URL:**
   - Mở `app/src/main/res/values/strings.xml`
   - Sửa `api_base_url` thành URL backend của bạn
   ```xml
   <string name="api_base_url">http://10.0.2.2:5000</string>
   ```
   (10.0.2.2 = localhost của máy host khi chạy trên emulator)

4. **Sync Gradle:**
   - Click "Sync Now" khi Android Studio yêu cầu

5. **Chạy app:**
   - Chọn device/emulator
   - Click "Run" (Shift + F10)

---

## 📞 LIÊN HỆ & HỖ TRỢ

Nếu có thắc mắc về cấu trúc dự án, vui lòng xem file `README_FUNCTIONS.md` để hiểu chi tiết về từng hàm và logic xử lý.

---

**Tài liệu này được tạo để giúp bạn hiểu rõ cấu trúc tổng quan của ứng dụng. Hãy xem file README_FUNCTIONS.md để tìm hiểu chi tiết về cách các hàm hoạt động!**
