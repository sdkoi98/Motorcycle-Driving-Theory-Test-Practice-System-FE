# 🔍 GIẢI THÍCH CHI TIẾT CÁC HÀM TRONG DỰ ÁN

---

## 📚 MỤC LỤC

1. [Activities](#activities)
   - [LoginActivity](#1-loginactivity)
   - [RegisterActivity](#2-registeractivity)
   - [MainActivity](#3-mainactivity)
   - [ExamTakingActivity](#4-examtakingactivity)
   - [ExamResultActivity](#5-examresultactivity)

2. [Fragments](#fragments)
   - [HomeFragment](#1-homefragment)
   - [HistoryFragment](#2-historyfragment)
   - [ProfileFragment](#3-profilefragment)
   - [AdminFragment](#4-adminfragment)
   - [AdminUsersTabFragment](#5-adminuserstabfragment)
   - [AdminQuestionsTabFragment](#6-adminquestionstabfragment)
   - [AdminExamsTabFragment](#7-adminexamstabfragment)

3. [Adapters](#adapters)
4. [Models](#models)
5. [Network & Utils](#network--utils)

---

## 🎬 ACTIVITIES

---

### **1. LoginActivity**

**File:** `activities/LoginActivity.java`

**Mục đích:** Xử lý đăng nhập người dùng

**Luồng hoạt động:**
```
onCreate() 
  → Kiểm tra đã login? 
    → Có: openMain()
    → Không: Hiển thị form login
  → User nhập email/password
  → validateLoginInput()
    → Valid? doLogin()
    → Invalid? Hiển thị error
  → doLogin() gọi API
    → Success: Lưu session → openMain()
    → Error: Hiển thị error message
```

---

#### **📌 Các thuộc tính (Fields)**

```java
private ActivityLoginBinding binding;
// - ViewBinding cho activity_login.xml
// - Truy cập View mà không cần findViewById()
// - Ví dụ: binding.etEmail, binding.btnLogin

private SessionManager sessionManager;
// - Quản lý session (token, email, role)
// - Lưu vào SharedPreferences
// - Kiểm tra trạng thái đăng nhập
```

---

#### **📌 Hàm onCreate()**

```java
protected void onCreate(Bundle savedInstanceState)
```

**Tham số:**
- `savedInstanceState`: Lưu trạng thái Activity trước đó (khi xoay màn hình, ...)

**Mục đích:** Khởi tạo Activity khi Activity được tạo lần đầu

**Chi tiết từng dòng:**

```java
super.onCreate(savedInstanceState);
// Gọi hàm onCreate() của class cha (AppCompatActivity)
// BẮT BUỘC phải gọi trước

binding = ActivityLoginBinding.inflate(getLayoutInflater());
// 1. getLayoutInflater(): Lấy LayoutInflater từ hệ thống
// 2. ActivityLoginBinding.inflate(): Tạo binding object từ activity_login.xml
// 3. Lúc này binding đã chứa reference đến tất cả Views trong layout

setContentView(binding.getRoot());
// Đặt layout cho Activity
// binding.getRoot() trả về root View của layout (thường là ConstraintLayout)

sessionManager = new SessionManager(this);
// Khởi tạo SessionManager với context = Activity hiện tại
// SessionManager sẽ dùng context này để truy cập SharedPreferences

if (sessionManager.isLoggedIn()) {
    // Kiểm tra xem user đã đăng nhập chưa
    // isLoggedIn() check token trong SharedPreferences
    openMain();
    // Nếu đã login → Chuyển đến MainActivity
    return;
    // Dừng execution, không hiển thị form login
}
```

**Thiết lập sự kiện:**

```java
binding.btnLogin.setOnClickListener(v -> {
    // Lambda expression (Java 8+)
    // Viết tắt của: new View.OnClickListener() { ... }
    
    String email = String.valueOf(binding.etEmail.getText()).trim();
    // 1. binding.etEmail: EditText cho email
    // 2. .getText(): Lấy Editable object
    // 3. String.valueOf(): Convert Editable thành String
    // 4. .trim(): Xóa khoảng trắng đầu/cuối
    
    String password = String.valueOf(binding.etPassword.getText()).trim();
    // Tương tự cho password
    
    if (!validateLoginInput(email, password)) {
        // Validate input trước khi gọi API
        // Nếu không valid → return (dừng lại)
        return;
    }

    doLogin(email, password);
    // Gọi API đăng nhập
});

binding.btnSignup.setOnClickListener(v ->
    startActivity(new Intent(this, RegisterActivity.class))
);
// Click "Đăng ký" → Mở RegisterActivity
// Intent là đối tượng để chuyển màn hình trong Android
```

---

#### **📌 Hàm validateLoginInput()**

```java
private boolean validateLoginInput(String email, String password)
```

**Tham số:**
- `email`: Email nhập từ EditText
- `password`: Password nhập từ EditText

**Return:** `true` nếu valid, `false` nếu có lỗi

**Mục đích:** Kiểm tra input hợp lệ trước khi gọi API (Client-side validation)

**Chi tiết:**

```java
binding.tilEmail.setError(null);
binding.tilPassword.setError(null);
// Clear error cũ (nếu có)
// tilEmail là TextInputLayout bọc EditText
// setError(null) = xóa error message

boolean valid = true;
// Flag để track valid state

if (email.isEmpty()) {
    // Kiểm tra email có rỗng không
    binding.tilEmail.setError(getString(R.string.error_required_email));
    // Hiển thị error message dưới TextInputLayout
    // getString(R.string.xxx) lấy string từ strings.xml
    valid = false;
    
} else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
    // Patterns.EMAIL_ADDRESS: Regex pattern sẵn của Android
    // .matcher(email): Tạo matcher object
    // .matches(): Check email có đúng format không
    binding.tilEmail.setError(getString(R.string.error_invalid_email));
    valid = false;
}

if (password.isEmpty()) {
    binding.tilPassword.setError(getString(R.string.error_required_password));
    valid = false;
    
} else if (password.length() < 6) {
    // Password phải >= 6 ký tự
    binding.tilPassword.setError(getString(R.string.error_password_min));
    valid = false;
}

return valid;
// Return kết quả cuối cùng
```

**Tại sao không dùng nhiều `return false`?**
- Để hiển thị TẤT CẢ lỗi cùng lúc, không chỉ lỗi đầu tiên
- UX tốt hơn: User biết hết lỗi cần sửa

---

#### **📌 Hàm doLogin()**

```java
private void doLogin(String email, String password)
```

**Tham số:**
- `email`: Email đã validate
- `password`: Password đã validate

**Mục đích:** Gọi API đăng nhập và xử lý response

**Chi tiết:**

**1. Tạo JSON payload:**

```java
setLoading(true);
// Hiển thị loading, disable buttons

JSONObject payload = new JSONObject();
// Tạo JSON object để gửi lên server

try {
    payload.put("email", email);
    payload.put("password", password);
    // Thêm key-value vào JSON
    // Kết quả: { "email": "user@example.com", "password": "123456" }
    
} catch (JSONException e) {
    // put() có thể throw JSONException (hiếm khi xảy ra)
    setLoading(false);
    Toast.makeText(this, getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
    // Hiển thị error toast
    return;
}
```

**2. Tạo API request:**

```java
ApiClient apiClient = ApiClient.getInstance(this);
// Lấy singleton instance của ApiClient
// getInstance(context) đảm bảo chỉ có 1 instance

String url = apiClient.endpoint("/api/auth/login");
// Tạo full URL: baseUrl + "/api/auth/login"
// Ví dụ: "http://10.0.2.2:5000/api/auth/login"

JsonObjectRequest request = new JsonObjectRequest(
    Request.Method.POST,
    // HTTP method: POST
    
    url,
    // URL endpoint
    
    payload,
    // JSON body
    
    response -> {
        // SUCCESS CALLBACK (Lambda)
        // response là JSONObject trả về từ server
        
        setLoading(false);
        // Ẩn loading
        
        String token = response.optString("token", "");
        // optString(key, defaultValue): Lấy value, nếu không có trả về ""
        // An toàn hơn getString() (không throw exception)
        
        String responseEmail = response.optString("email", email);
        // Lấy email từ response, nếu không có dùng email input
        
        String role = response.optString("role", "User");
        // Lấy role, default là "User"

        if (token.isEmpty()) {
            // Nếu không có token → Lỗi
            Toast.makeText(this, getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.saveSession(responseEmail, token, role);
        // Lưu email, token, role vào SharedPreferences
        
        openMain();
        // Chuyển đến MainActivity
    },
    
    error -> {
        // ERROR CALLBACK (Lambda)
        // error là VolleyError object
        
        setLoading(false);
        
        String message = mapAuthError(
            error.networkResponse == null ? -1 : error.networkResponse.statusCode,
            // Lấy status code, nếu null (network error) thì -1
            
            error.networkResponse == null ? null : error.networkResponse.data
            // Lấy response body (byte array)
        );
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
);

apiClient.getRequestQueue().add(request);
// Thêm request vào hàng đợi để thực thi
// Volley sẽ tự động xử lý request trên background thread
```

---

#### **📌 Hàm setLoading()**

```java
private void setLoading(boolean loading)
```

**Tham số:**
- `loading`: `true` = đang loading, `false` = không loading

**Mục đích:** Hiển thị/ẩn loading indicator và disable/enable buttons

**Chi tiết:**

```java
binding.btnLogin.setEnabled(!loading);
binding.btnSignup.setEnabled(!loading);
// !loading: Nếu loading = true → setEnabled(false)
// Disable buttons khi đang loading để tránh spam click

binding.pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
// Ternary operator: condition ? ifTrue : ifFalse
// Nếu loading = true → VISIBLE, ngược lại → GONE
// VISIBLE: Hiển thị, GONE: Ẩn và không chiếm không gian
```

---

#### **📌 Hàm mapAuthError()**

```java
private String mapAuthError(int statusCode, byte[] data)
```

**Tham số:**
- `statusCode`: HTTP status code (200, 401, 500, ...)
- `data`: Response body dạng byte array (có thể null)

**Return:** Error message dạng String

**Mục đích:** Convert status code và response body thành error message dễ hiểu cho user

**Chi tiết:**

```java
if (statusCode == -1) {
    // -1 = network error (không kết nối được server)
    return getString(R.string.error_network);
}

String body = "";
if (data != null && data.length > 0) {
    // Nếu có response body
    body = new String(data, StandardCharsets.UTF_8).trim();
    // Convert byte array thành String với encoding UTF-8
    // .trim(): Xóa khoảng trắng đầu/cuối
}

if (statusCode == 401) {
    // 401 = Unauthorized (sai email/password)
    if (!body.isEmpty()) {
        return getString(R.string.login_failed_prefix, stripQuotes(body));
        // Format string: "Đăng nhập thất bại: <message>"
    }
    return getString(R.string.login_failed_credentials);
}

if (!body.isEmpty()) {
    // Có error message từ server
    return getString(R.string.login_failed_prefix, stripQuotes(body));
}

return getString(R.string.error_unexpected);
// Default error message
```

---

#### **📌 Hàm stripQuotes()**

```java
private String stripQuotes(String text)
```

**Tham số:**
- `text`: String có thể chứa dấu ngoặc kép

**Return:** String đã xóa dấu ngoặc kép

**Mục đích:** Server đôi khi trả về error message dạng `"Error message"` (có quotes), hàm này xóa quotes

**Chi tiết:**

```java
if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
    // Kiểm tra:
    // 1. text có ít nhất 2 ký tự
    // 2. Bắt đầu bằng "
    // 3. Kết thúc bằng "
    
    return text.substring(1, text.length() - 1);
    // substring(start, end): Cắt string từ vị trí start đến end-1
    // Ví dụ: "Hello" → substring(1, 4) → "ell"
}
return text;
// Nếu không có quotes thì return nguyên bản
```

---

#### **📌 Hàm openMain()**

```java
private void openMain()
```

**Mục đích:** Chuyển đến MainActivity và đóng LoginActivity

**Chi tiết:**

```java
Intent intent = new Intent(this, MainActivity.class);
// Tạo Intent để chuyển màn hình
// Intent(context, targetActivity)

startActivity(intent);
// Khởi động MainActivity

finish();
// Đóng LoginActivity
// User không thể back về màn hình login bằng nút Back
```

---

## 📊 TÓM TẮT FLOW LoginActivity

```
User nhập email/password → Click "Đăng nhập"
  ↓
validateLoginInput()
  ↓ (Valid)
doLogin()
  ↓
setLoading(true) → Disable buttons, hiển thị progress
  ↓
Tạo JSONObject payload
  ↓
Gọi API POST /api/auth/login
  ↓
  ├── SUCCESS:
  │     ↓
  │   setLoading(false)
  │     ↓
  │   Lấy token, email, role từ response
  │     ↓
  │   sessionManager.saveSession()
  │     ↓
  │   openMain() → MainActivity
  │
  └── ERROR:
        ↓
      setLoading(false)
        ↓
      mapAuthError() → Convert status code thành error message
        ↓
      Toast.show(message)
```

---

