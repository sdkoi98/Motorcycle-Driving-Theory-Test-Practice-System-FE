# GIẢI THÍCH CHI TIẾT CÁC HÀM TRONG DỰ ÁN

Tài liệu này giải thích từng hàm quan trọng trong từng file, bao gồm mục đích, tham số, giá trị trả về, và mối quan hệ với các hàm/file khác.

---

# LOGINACTIVITY

File: `activities/LoginActivity.java`

LoginActivity là màn hình đầu tiên. Nó được khai báo là LAUNCHER trong AndroidManifest, tức là hệ thống sẽ khởi động Activity này khi mở app.

---

## onCreate(Bundle savedInstanceState)

Đây là hàm bắt buộc của mọi Activity, được gọi khi Activity lần đầu được tạo ra.

`savedInstanceState` là Bundle lưu trạng thái trước đó (khi xoay màn hình). Trong LoginActivity không dùng đến tham số này.

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // super.onCreate() PHẢI gọi đầu tiên để Android khởi tạo Activity đúng cách

    binding = ActivityLoginBinding.inflate(getLayoutInflater());
    // inflate() đọc file activity_login.xml và tạo ra các View tương ứng
    // binding giờ chứa reference đến tất cả View trong layout

    setContentView(binding.getRoot());
    // Đặt layout đó làm giao diện của Activity

    sessionManager = new SessionManager(this);
    // Khởi tạo SessionManager để đọc/ghi SharedPreferences

    if (sessionManager.isLoggedIn()) {
        openMain();
        return;
        // Nếu đã có token (từ lần đăng nhập trước), chuyển thẳng sang MainActivity
        // return để không thực thi tiếp phần setup form bên dưới
    }

    binding.btnLogin.setOnClickListener(v -> {
        String email = String.valueOf(binding.etEmail.getText()).trim();
        String password = String.valueOf(binding.etPassword.getText()).trim();
        if (validateLoginInput(email, password)) {
            doLogin(email, password);
        }
    });

    binding.btnSignup.setOnClickListener(v ->
        startActivity(new Intent(this, RegisterActivity.class))
    );
    // startActivity tạo Intent (ý định mở màn hình) và chuyển sang RegisterActivity
}
```

---

## validateLoginInput(String email, String password)

Hàm này kiểm tra dữ liệu người dùng nhập trước khi gọi API. Nếu gọi API với dữ liệu sai, sẽ lãng phí network và trải nghiệm kém.

Trả về `true` nếu hợp lệ, `false` nếu có lỗi.

Điểm quan trọng trong thiết kế: hàm này không `return false` ngay khi gặp lỗi đầu tiên. Thay vào đó dùng flag `valid = false`, tiếp tục kiểm tra hết rồi mới return. Lý do: người dùng nhìn thấy tất cả lỗi cùng lúc, không phải sửa từng lỗi một.

```java
private boolean validateLoginInput(String email, String password) {
    binding.tilEmail.setError(null);    // Xóa lỗi cũ
    binding.tilPassword.setError(null);

    boolean valid = true;

    if (email.isEmpty()) {
        binding.tilEmail.setError(getString(R.string.error_required_email));
        valid = false;
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        // Patterns.EMAIL_ADDRESS là regex của Android để validate email
        binding.tilEmail.setError(getString(R.string.error_invalid_email));
        valid = false;
    }

    if (password.isEmpty()) {
        binding.tilPassword.setError(getString(R.string.error_required_password));
        valid = false;
    } else if (password.length() < 6) {
        binding.tilPassword.setError(getString(R.string.error_password_min));
        valid = false;
    }

    return valid;
}
```

`binding.tilEmail` là TextInputLayout (wrapper bên ngoài EditText). `setError()` hiển thị dòng chữ đỏ bên dưới input.

---

## doLogin(String email, String password)

Gọi API đăng nhập. Hàm này được gọi sau khi `validateLoginInput()` trả về true.

```java
private void doLogin(String email, String password) {
    setLoading(true);   // Bước 1: Disable button, hiện loading

    JSONObject payload = new JSONObject();
    try {
        payload.put("email", email);
        payload.put("password", password);
        // Tạo JSON: { "email": "...", "password": "..." }
    } catch (JSONException e) {
        setLoading(false);
        return;
    }

    ApiClient apiClient = ApiClient.getInstance(this);
    String url = apiClient.endpoint("/api/auth/login");
    // url = "http://10.0.2.2:5000/api/auth/login"

    JsonObjectRequest request = new JsonObjectRequest(
        Request.Method.POST, url, payload,
        response -> {
            // Callback này chạy trên Main Thread khi server trả về thành công
            setLoading(false);

            String token = response.optString("token", "");
            String responseEmail = response.optString("email", email);
            String role = response.optString("role", "User");
            // optString(key, default): Lấy giá trị, nếu key không tồn tại trả về default
            // Dùng opt* thay vì get* để không bị throw JSONException

            if (token.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.saveSession(responseEmail, token, role);
            // Lưu vào SharedPreferences để dùng cho các lần sau

            openMain();
            // Chuyển sang MainActivity
        },
        error -> {
            // Callback này chạy khi có lỗi (network error, 401, 500...)
            setLoading(false);
            String message = mapAuthError(
                error.networkResponse == null ? -1 : error.networkResponse.statusCode,
                error.networkResponse == null ? null : error.networkResponse.data
            );
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    );

    apiClient.getRequestQueue().add(request);
    // Đưa request vào hàng đợi Volley, Volley tự chạy trên background thread
}
```

---

## setLoading(boolean loading)

Quản lý trạng thái loading. Khi `loading = true`: disable tất cả button, hiện ProgressBar. Khi `loading = false`: ngược lại.

Lý do phải disable button khi loading: nếu user spam click, sẽ gửi nhiều request lên server cùng lúc gây lỗi.

```java
private void setLoading(boolean loading) {
    binding.btnLogin.setEnabled(!loading);
    binding.btnSignup.setEnabled(!loading);
    binding.pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
    // VISIBLE: hiển thị, GONE: ẩn và không chiếm không gian
}
```

---

## mapAuthError(int statusCode, byte[] data)

Chuyển đổi HTTP status code và response body thành message hiển thị cho user.

`statusCode = -1` nghĩa là không có `networkResponse`, tức là không kết nối được đến server (wifi tắt, server down...).

```java
private String mapAuthError(int statusCode, byte[] data) {
    if (statusCode == -1) {
        return getString(R.string.error_network);
    }

    String body = "";
    if (data != null && data.length > 0) {
        body = new String(data, StandardCharsets.UTF_8).trim();
        // Chuyển byte array sang String với encoding UTF-8
    }

    if (statusCode == 401) {
        if (!body.isEmpty()) {
            return getString(R.string.login_failed_prefix, stripQuotes(body));
        }
        return getString(R.string.login_failed_credentials);
    }

    if (!body.isEmpty()) {
        return getString(R.string.login_failed_prefix, stripQuotes(body));
    }

    return getString(R.string.error_unexpected);
}
```

---

## stripQuotes(String text)

Server đôi khi trả về message dạng `"Invalid credentials"` (có dấu nháy kép bao quanh). Hàm này xóa bỏ dấu nháy kép đó trước khi hiển thị cho user.

```java
private String stripQuotes(String text) {
    if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
        return text.substring(1, text.length() - 1);
        // substring(1, length-1): cắt bỏ ký tự đầu và ký tự cuối
    }
    return text;
}
```

---

## openMain()

Chuyển đến MainActivity và đóng LoginActivity. Sau khi `finish()`, LoginActivity bị xóa khỏi back stack. Người dùng không thể nhấn Back từ MainActivity để quay về màn hình đăng nhập.

```java
private void openMain() {
    startActivity(new Intent(this, MainActivity.class));
    finish();
}
```

---

# MAINACTIVITY

File: `activities/MainActivity.java`

MainActivity là container. Nó chứa BottomNavigationView ở dưới và một FrameLayout (`fragmentContainer`) ở giữa. Khi user chọn tab, MainActivity thay Fragment trong `fragmentContainer`.

---

## onCreate(Bundle savedInstanceState)

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    SessionManager sessionManager = new SessionManager(this);
    boolean isAdmin = "Admin".equalsIgnoreCase(sessionManager.getRole());

    if (isAdmin) {
        // Admin: xóa menu cũ (từ XML), build lại bằng code với 2 item
        binding.bottomNav.getMenu().clear();
        binding.bottomNav.getMenu()
            .add(0, R.id.nav_admin, 0, R.string.admin)
            .setIcon(android.R.drawable.ic_menu_manage);
        binding.bottomNav.getMenu()
            .add(0, R.id.nav_profile, 1, R.string.profile)
            .setIcon(android.R.drawable.ic_menu_myplaces);

        if (savedInstanceState == null) {
            switchFragment(new AdminFragment());
            // savedInstanceState == null: chỉ set Fragment lần đầu tiên
            // Nếu Activity được recreate (xoay màn hình), Fragment đã được khôi phục tự động
        }
    } else {
        // User: dùng menu từ XML (5 tab)
        setupCartBadge();
        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
        }
    }

    binding.bottomNav.setOnItemSelectedListener(item -> {
        int id = item.getItemId();
        if (id == R.id.nav_home) switchFragment(new HomeFragment());
        else if (id == R.id.nav_question_bank) switchFragment(new QuestionBankFragment());
        else if (id == R.id.nav_cart) switchFragment(new StudyCartFragment());
        else if (id == R.id.nav_history) switchFragment(new HistoryFragment());
        else if (id == R.id.nav_admin) switchFragment(new AdminFragment());
        else if (id == R.id.nav_profile) switchFragment(new ProfileFragment());
        return true;
    });
}
```

---

## switchFragment(Fragment fragment)

Thay thế Fragment đang hiển thị bằng Fragment mới.

```java
private void switchFragment(Fragment fragment) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragmentContainer, fragment)
        // replace: xóa Fragment cũ, đặt Fragment mới vào container
        .commit();
}
```

---

## setupCartBadge() và refreshCartBadge()

Badge là số đỏ hiện trên icon trong BottomNav. `setupCartBadge()` tạo badge lần đầu. `refreshCartBadge()` được gọi mỗi khi số lượng câu trong giỏ thay đổi.

```java
private void setupCartBadge() {
    cartBadge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart);
    cartBadge.setBackgroundColor(getColor(R.color.danger_red));
    cartBadge.setBadgeTextColor(getColor(R.color.white));
    refreshCartBadge();
}

public void refreshCartBadge() {
    if (cartBadge == null) return;
    int count = new StudyCartManager(this).getCartCount();
    if (count > 0) {
        cartBadge.setNumber(count);
        cartBadge.setVisible(true);
    } else {
        cartBadge.clearNumber();
        cartBadge.setVisible(false);
    }
}
```

`refreshCartBadge()` là `public` để Fragment có thể gọi từ bên ngoài. Khi QuestionBankFragment hay StudyCartFragment thay đổi giỏ, chúng cast Activity thành MainActivity và gọi hàm này:

```java
// Trong QuestionBankFragment.updateCartBadge():
if (getActivity() instanceof MainActivity) {
    ((MainActivity) getActivity()).refreshCartBadge();
}
```

---

# EXAMCONFIRMACTIVITY

File: `activities/ExamConfirmActivity.java`

Màn hình xác nhận thông tin trước khi thi. Có form điền thông tin cá nhân, RadioGroup chọn phương thức thanh toán, và mô phỏng 2 giây xử lý thanh toán rồi mới tạo đề thi.

---

## prefillUserInfo()

Tự động điền thông tin từ session để user không phải gõ lại.

```java
private void prefillUserInfo() {
    String email = sessionManager.getEmail();
    binding.etEmail.setText(email);

    String name = deriveNameFromEmail(email);
    binding.etFullName.setText(name);
}
```

---

## deriveNameFromEmail(String email)

Tạo tên hiển thị từ email. Logic: lấy phần trước `@`, split bằng `.` hoặc `_`, capitalize ký tự đầu mỗi phần.

Ví dụ: `"nguyen.van.a@gmail.com"` → `["nguyen", "van", "a"]` → `"Nguyen Van A"`

Hàm này xuất hiện ở cả ExamConfirmActivity và ProfileFragment với logic giống hệt nhau.

```java
private String deriveNameFromEmail(String email) {
    if (email == null || !email.contains("@")) return "";
    String prefix = email.substring(0, email.indexOf('@'));
    String[] parts = prefix.split("[._]");  // Regex: split bằng dấu . hoặc _
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
        if (part.isEmpty()) continue;
        sb.append(Character.toUpperCase(part.charAt(0)));
        if (part.length() > 1) sb.append(part.substring(1));
        sb.append(' ');
    }
    return sb.toString().trim();
}
```

---

## processPayment()

Được gọi khi nhấn nút "Thanh toán". Validate input, rồi delay 2 giây (giả lập xử lý), rồi gọi `generateExam()`.

```java
private void processPayment() {
    if (!validateInput()) return;

    binding.btnPay.setEnabled(false);
    binding.progressBar.setVisibility(View.VISIBLE);

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
        // Hàm này chạy sau 2000ms (2 giây) trên Main Thread
        if (binding == null) return;  // Guard: Activity có thể đã bị destroy
        binding.progressBar.setVisibility(View.GONE);
        generateExam();
    }, 2000);
}
```

`Handler(Looper.getMainLooper()).postDelayed(runnable, 2000)` là cách Android delay một đoạn code chạy trên Main Thread.

---

## generateExam()

Gọi API để server tạo đề thi ngẫu nhiên.

```java
private void generateExam() {
    ApiClient apiClient = ApiClient.getInstance(this);
    String url = apiClient.endpoint("/api/exam/generate");

    JsonObjectRequest request = new JsonObjectRequest(
        Request.Method.POST, url, null,
        response -> {
            generatedExamId = response.optInt("examId", -1);
            JSONArray questions = response.optJSONArray("questions");

            if (generatedExamId <= 0 || questions == null || questions.length() == 0) {
                Toast.makeText(this, getString(R.string.home_exam_invalid), Toast.LENGTH_SHORT).show();
                binding.btnPay.setEnabled(true);
                return;
            }

            generatedQuestionsJson = questions.toString();
            // Chuyển JSONArray thành String để truyền qua Intent
            showConfirmation();
        },
        error -> {
            binding.btnPay.setEnabled(true);
            String msg = getString(R.string.home_generate_error);
            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                msg = getString(R.string.home_session_expired);
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    ) {
        @Override
        public Map<String, String> getHeaders() {
            return ApiClient.authHeaders(sessionManager);
        }
        // Override để handle UTF-8 trong response
        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            String json = new String(response.data, StandardCharsets.UTF_8);
            return Response.success(new JSONObject(json), ...);
        }
    };

    apiClient.getRequestQueue().add(request);
}
```

---

## showConfirmation()

Sau khi tạo đề thi thành công, ẩn form thanh toán và hiện card xác nhận với chi tiết và nút "Bắt đầu thi".

```java
private void showConfirmation() {
    binding.btnPay.setVisibility(View.GONE);
    binding.cardConfirmation.setVisibility(View.VISIBLE);

    String paymentMethod = getSelectedPaymentMethod();
    String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    binding.tvConfirmationDetail.setText(
        getString(R.string.billing_success_detail, generatedExamId, paymentMethod, timestamp)
    );

    binding.btnStartExam.setOnClickListener(v -> {
        Intent intent = new Intent(this, ExamTakingActivity.class);
        intent.putExtra(ExamTakingActivity.EXTRA_EXAM_ID, generatedExamId);
        intent.putExtra(ExamTakingActivity.EXTRA_QUESTIONS_JSON, generatedQuestionsJson);
        startActivity(intent);
        finish();
        // finish() để user không back lại màn hình xác nhận sau khi đã vào thi
    });
}
```

---

# EXAMTAKINGACTIVITY

File: `activities/ExamTakingActivity.java`

Lưu ý đặc biệt: Activity này KHÔNG dùng ViewBinding, dùng `setContentView(R.layout.activity_exam_taking)` và `findViewById()` trực tiếp.

Dữ liệu đầu vào: `EXTRA_EXAM_ID` (int) và `EXTRA_QUESTIONS_JSON` (String — một JSON array chứa các câu hỏi).

Cấu trúc lưu đáp án: `JSONObject selectedAnswers` — key là questionId (String), value là "A"/"B"/"C"/"D".

---

## onCreate()

Parse câu hỏi từ JSON string, khởi tạo các View bằng `findViewById()`, setup listener và hiển thị câu đầu tiên.

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_exam_taking);
    // Không dùng ViewBinding ở đây

    // Lấy tất cả View bằng findViewById
    tvQuestionIndex = findViewById(R.id.tvQuestionIndex);
    tvQuestionContent = findViewById(R.id.tvQuestionContent);
    tvErrorMessage = findViewById(R.id.tvErrorMessage);
    rgAnswers = findViewById(R.id.rgAnswers);
    rbA = findViewById(R.id.rbA);
    // ... tương tự cho rbB, rbC, rbD, btnNextQuestion

    examId = getIntent().getIntExtra(EXTRA_EXAM_ID, -1);
    String questionsJson = getIntent().getStringExtra(EXTRA_QUESTIONS_JSON);

    // Validation: nếu dữ liệu không hợp lệ thì đóng Activity ngay
    if (examId <= 0 || questionsJson == null || questionsJson.isEmpty()) {
        Toast.makeText(this, getString(R.string.error_exam_data_invalid), Toast.LENGTH_SHORT).show();
        finish();
        return;
    }

    questions = new JSONArray(questionsJson);
    // Parse String thành JSONArray

    btnNextQuestion.setOnClickListener(v -> onNextClicked());

    rgAnswers.setOnCheckedChangeListener((group, checkedId) -> {
        if (checkedId != -1) hideError();
        // Khi user chọn đáp án, tự động ẩn thông báo lỗi "Chưa chọn đáp án"
    });

    renderQuestion();
}
```

---

## renderQuestion()

Hiển thị câu hỏi tại vị trí `currentIndex`. Gồm: set text câu hỏi, set text 4 đáp án, restore đáp án đã chọn (nếu có), đổi text nút (Tiếp theo/Nộp bài).

```java
private void renderQuestion() {
    JSONObject question = questions.optJSONObject(currentIndex);
    int questionId = question.optInt("questionId", -1);

    hideError();  // Xóa error khi chuyển câu mới

    tvQuestionIndex.setText(getString(R.string.exam_question_index, currentIndex + 1, questions.length()));
    // VD: "Câu 3/25"
    tvQuestionContent.setText(question.optString("content", ""));
    rbA.setText("A. " + question.optString("answerA", ""));
    rbB.setText("B. " + question.optString("answerB", ""));
    rbC.setText("C. " + question.optString("answerC", ""));
    rbD.setText("D. " + question.optString("answerD", ""));

    // Restore đáp án đã chọn ở câu này (khi user nhấn Back)
    rgAnswers.clearCheck();
    String savedAnswer = selectedAnswers.optString(String.valueOf(questionId), "");
    if ("A".equals(savedAnswer)) rgAnswers.check(R.id.rbA);
    else if ("B".equals(savedAnswer)) rgAnswers.check(R.id.rbB);
    else if ("C".equals(savedAnswer)) rgAnswers.check(R.id.rbC);
    else if ("D".equals(savedAnswer)) rgAnswers.check(R.id.rbD);

    // Câu cuối: hiện "Nộp bài", còn lại: hiện "Tiếp theo"
    boolean isLast = (currentIndex == questions.length() - 1);
    btnNextQuestion.setText(isLast ? getString(R.string.exam_submit) : getString(R.string.exam_next));
}
```

---

## onNextClicked()

Xử lý khi nhấn nút "Tiếp theo" hoặc "Nộp bài". Validate xem đã chọn đáp án chưa, lưu đáp án, rồi chuyển câu hoặc submit.

```java
private void onNextClicked() {
    JSONObject question = questions.optJSONObject(currentIndex);
    int questionId = question.optInt("questionId", -1);

    String selected = getSelectedOption();
    if (selected.isEmpty()) {
        showError(getString(R.string.error_answer_required));
        return;
        // Không cho qua nếu chưa chọn đáp án
    }

    selectedAnswers.put(String.valueOf(questionId), selected);
    // Lưu vào JSONObject: key = "123" (questionId), value = "A"

    if (currentIndex == questions.length() - 1) {
        submitExam();
        return;
    }

    currentIndex++;
    renderQuestion();
    // Tăng index và render câu tiếp theo
}
```

---

## getSelectedOption()

Đọc RadioGroup để biết user đang chọn đáp án nào.

```java
private String getSelectedOption() {
    int selectedId = rgAnswers.getCheckedRadioButtonId();
    if (selectedId == R.id.rbA) return "A";
    if (selectedId == R.id.rbB) return "B";
    if (selectedId == R.id.rbC) return "C";
    if (selectedId == R.id.rbD) return "D";
    return "";  // Chưa chọn gì
}
```

---

## submitExam()

Gửi toàn bộ đáp án lên server. Body JSON có dạng `{ "examId": 123, "answers": { "1": "A", "2": "C", ... } }`.

```java
private void submitExam() {
    btnNextQuestion.setEnabled(false);  // Tránh submit 2 lần

    JSONObject payload = new JSONObject();
    payload.put("examId", examId);
    payload.put("answers", selectedAnswers);
    // selectedAnswers đã chứa đủ đáp án của tất cả câu

    ApiClient apiClient = ApiClient.getInstance(this);
    JsonObjectRequest request = new JsonObjectRequest(
        Request.Method.POST,
        apiClient.endpoint("/api/exam/submit"),
        payload,
        response -> {
            btnNextQuestion.setEnabled(true);
            Intent intent = new Intent(this, ExamResultActivity.class);
            intent.putExtra(ExamResultActivity.EXTRA_EXAM_ID, examId);
            intent.putExtra(ExamResultActivity.EXTRA_SCORE, response.optInt("score", 0));
            intent.putExtra(ExamResultActivity.EXTRA_TOTAL_QUESTIONS, response.optInt("totalQuestions", questions.length()));
            intent.putExtra(ExamResultActivity.EXTRA_PASSED, response.optBoolean("passed", false));
            intent.putExtra(ExamResultActivity.EXTRA_FAILED_IMPORTANT, response.optBoolean("failedByImportantQuestion", false));
            startActivity(intent);
            finish();
            // finish() để không back về màn hình làm bài sau khi đã nộp
        },
        error -> {
            btnNextQuestion.setEnabled(true);
            // Xử lý lỗi...
        }
    ) {
        @Override
        public Map<String, String> getHeaders() {
            return ApiClient.authHeaders(new SessionManager(this));
        }
    };
    apiClient.getRequestQueue().add(request);
}
```

---

## showError(String message) và hideError()

`showError()` hiển thị message lỗi và chạy animation shake (rung) vào cả text lỗi và RadioGroup, tạo hiệu ứng thu hút sự chú ý.

```java
private void showError(String message) {
    tvErrorMessage.setText(message);
    tvErrorMessage.setVisibility(View.VISIBLE);

    Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
    tvErrorMessage.startAnimation(shake);
    rgAnswers.startAnimation(shake);
    // Cả 2 View cùng rung để user chú ý
}

private void hideError() {
    tvErrorMessage.setVisibility(View.GONE);
    tvErrorMessage.setText("");
}
```

---

# QUESTIONDETAILACTIVITY

File: `activities/QuestionDetailActivity.java`

Màn hình xem chi tiết một câu hỏi. Dữ liệu nhận qua Intent Extras, không gọi API. Có chức năng thêm/xóa câu hỏi khỏi giỏ ôn tập.

Constants dùng để truyền data:
- `EXTRA_QUESTION_ID`, `EXTRA_CONTENT`, `EXTRA_ANSWER_A/B/C/D`
- `EXTRA_CORRECT`, `EXTRA_CATEGORY`, `EXTRA_IMPORTANT`

---

## loadQuestion()

Đọc tất cả Intent Extras, tạo object `AdminQuestion`, rồi hiển thị lên UI.

```java
private void loadQuestion() {
    int questionId = getIntent().getIntExtra(EXTRA_QUESTION_ID, -1);
    String content = getIntent().getStringExtra(EXTRA_CONTENT);
    // ... đọc tương tự cho các field khác

    question = new AdminQuestion(questionId, content, answerA, answerB, answerC, answerD,
                                  correct, categoryId, isImportant);

    binding.tvQuestionContent.setText(content);
    binding.tvAnswerA.setText("A. " + answerA);
    // ... tương tự B, C, D

    binding.tvCorrectAnswer.setText("Đáp án " + correct);
    binding.tvCategory.setText(getString(R.string.category_format, categoryId));

    if (isImportant) {
        binding.tvImportantBadge.setVisibility(View.VISIBLE);
        // Hiện badge "Câu điểm liệt"
    }

    highlightCorrectAnswer(correct);
}
```

---

## highlightCorrectAnswer(String correct)

Vẽ background màu xanh lá lên TextView chứa đáp án đúng để người dùng nhận ra ngay.

```java
private void highlightCorrectAnswer(String correct) {
    TextView target = null;
    if ("A".equalsIgnoreCase(correct)) target = binding.tvAnswerA;
    else if ("B".equalsIgnoreCase(correct)) target = binding.tvAnswerB;
    else if ("C".equalsIgnoreCase(correct)) target = binding.tvAnswerC;
    else if ("D".equalsIgnoreCase(correct)) target = binding.tvAnswerD;

    if (target != null) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(getColor(R.color.correct_answer_bg));     // Màu nền xanh nhạt
        bg.setCornerRadius(8 * getResources().getDisplayMetrics().density);  // Bo góc 8dp
        bg.setStroke(2dp, getColor(R.color.brand_green));     // Viền xanh đậm 2dp
        target.setBackground(bg);
        target.setTextColor(getColor(R.color.brand_dark));
    }
}
```

---

## setupCartButton() và updateCartButtonState()

Nút này toggle: nếu câu hỏi đang trong giỏ thì xóa ra, nếu chưa có thì thêm vào.

```java
private void setupCartButton() {
    updateCartButtonState();  // Set trạng thái ban đầu

    binding.btnAddToCart.setOnClickListener(v -> {
        boolean inCart = cartManager.isInCart(question.getQuestionId());
        if (inCart) {
            cartManager.removeQuestion(question.getQuestionId());
            Toast.makeText(this, getString(R.string.toast_removed_from_cart), Toast.LENGTH_SHORT).show();
        } else {
            cartManager.addQuestion(question);
            Toast.makeText(this, getString(R.string.toast_added_to_cart), Toast.LENGTH_SHORT).show();
        }
        updateCartButtonState();
    });
}

private void updateCartButtonState() {
    boolean inCart = cartManager.isInCart(question.getQuestionId());
    if (inCart) {
        binding.btnAddToCart.setText(getString(R.string.btn_remove_from_cart));
        binding.btnAddToCart.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.danger_red)));
    } else {
        binding.btnAddToCart.setText(getString(R.string.btn_add_to_cart));
        binding.btnAddToCart.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.brand_green)));
    }
}
```

---

# DRIVINGSCHOOLMAPACTIVITY

File: `activities/DrivingSchoolMapActivity.java`

Hiển thị bản đồ Google Maps với 8 trường sát hạch hard-coded. Implement 2 interface: `OnMapReadyCallback` (từ Google Maps) và `DrivingSchoolAdapter.OnSchoolActionListener` (từ Adapter riêng của app).

---

## onCreate()

Setup toolbar, RecyclerView cho danh sách bên dưới, và khởi tạo bản đồ bất đồng bộ.

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityDrivingSchoolMapBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    schools = getDrivingSchools();  // Lấy danh sách 8 trường hard-coded

    DrivingSchoolAdapter adapter = new DrivingSchoolAdapter(this);
    // "this" vừa là Context vừa là OnSchoolActionListener

    binding.rvSchools.setLayoutManager(new LinearLayoutManager(this));
    binding.rvSchools.setAdapter(adapter);
    adapter.submitList(schools);

    SupportMapFragment mapFragment = (SupportMapFragment)
        getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
    // getMapAsync() là bất đồng bộ, khi bản đồ sẵn sàng sẽ callback onMapReady()
}
```

---

## onMapReady(GoogleMap map)

Callback được gọi khi Google Maps SDK đã tải xong. Thêm marker cho tất cả 8 trường và điều chỉnh camera để hiển thị tất cả.

```java
public void onMapReady(GoogleMap map) {
    googleMap = map;
    googleMap.getUiSettings().setZoomControlsEnabled(true);

    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

    for (DrivingSchool school : schools) {
        LatLng position = new LatLng(school.getLatitude(), school.getLongitude());
        googleMap.addMarker(new MarkerOptions()
            .position(position)
            .title(school.getName())
            .snippet(school.getAddress()));
        boundsBuilder.include(position);
        // Thêm từng tọa độ vào boundsBuilder để tính vùng bao phủ
    }

    LatLngBounds bounds = boundsBuilder.build();
    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    // newLatLngBounds(bounds, padding): điều chỉnh camera sao cho tất cả marker nằm trong khung
}
```

---

## onSchoolClick(DrivingSchool school)

Khi user click vào một trường trong danh sách, camera của bản đồ zoom đến trường đó.

```java
public void onSchoolClick(DrivingSchool school) {
    if (googleMap != null) {
        LatLng position = new LatLng(school.getLatitude(), school.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        // animateCamera: di chuyển mượt mà, zoom level 15 (khá gần)
    }
}
```

---

## onDirectionClick(DrivingSchool school)

Mở Google Maps app để chỉ đường. Có fallback về web nếu không cài Google Maps.

```java
public void onDirectionClick(DrivingSchool school) {
    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + school.getLatitude() + "," + school.getLongitude());
    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
    mapIntent.setPackage("com.google.android.apps.maps");

    if (mapIntent.resolveActivity(getPackageManager()) != null) {
        startActivity(mapIntent);
    } else {
        // Google Maps không cài → mở web browser
        Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
        startActivity(new Intent(Intent.ACTION_VIEW, webUri));
    }
}
```

---

# HOMEFRAGMENT

File: `fragments/HomeFragment.java`

Fragment đầu tiên của User. Có 3 khu vực chức năng: tạo đề thi, bản đồ, nhắc nhở.

---

## onViewCreated()

```java
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    notificationHelper = new NotificationHelper(requireContext());

    binding.btnGenerateExam.setOnClickListener(v -> {
        SessionManager sessionManager = new SessionManager(requireContext());
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(...).show();
            return;
        }
        startActivity(new Intent(requireContext(), ExamConfirmActivity.class));
        // Chuyển đến ExamConfirmActivity (không gọi API ở đây)
    });

    binding.cardMap.setOnClickListener(v ->
        startActivity(new Intent(requireContext(), DrivingSchoolMapActivity.class))
    );

    setupReminderCard();
}
```

---

## setupReminderCard()

Cài đặt card nhắc nhở: đọc trạng thái hiện tại, setup Switch listener, setup nút chọn giờ.

```java
private void setupReminderCard() {
    boolean enabled = notificationHelper.isReminderEnabled();
    binding.switchReminder.setChecked(enabled);
    updateReminderStatus();
    // Hiển thị text trạng thái ngay từ đầu

    binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (!buttonView.isPressed()) return;
        // isPressed() check: listener này bị gọi ngay cả khi setChecked() từ code
        // Chỉ xử lý khi user tự tay bấm switch

        if (isChecked) {
            requestNotificationPermissionAndEnable();
        } else {
            notificationHelper.cancelReminder();
            updateReminderStatus();
        }
    });

    binding.btnSetTime.setOnClickListener(v -> showTimePicker());
    binding.btnSetTime.setVisibility(enabled ? View.VISIBLE : View.GONE);
    // Chỉ hiện nút chọn giờ khi đã bật nhắc nhở
}
```

---

## requestNotificationPermissionAndEnable()

Android 13 (TIRAMISU) trở lên yêu cầu xin quyền `POST_NOTIFICATIONS` trước khi có thể hiện thông báo.

```java
private void requestNotificationPermissionAndEnable() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            // Hiện dialog xin quyền của Android
            return;
        }
    }
    enableReminder();
    // Android < 13 không cần xin quyền, gọi thẳng
}
```

`notificationPermissionLauncher` là `ActivityResultLauncher` được đăng ký ở đầu Fragment:

```java
private final ActivityResultLauncher<String> notificationPermissionLauncher =
    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            enableReminder();  // User đồng ý → bật nhắc nhở
        } else {
            binding.switchReminder.setChecked(false);  // User từ chối → tắt switch
        }
    });
```

---

## showTimePicker()

Hiện dialog chọn giờ phút.

```java
private void showTimePicker() {
    int currentHour = notificationHelper.getReminderHour();
    int currentMinute = notificationHelper.getReminderMinute();

    new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
        // Callback khi user chọn xong giờ
        notificationHelper.scheduleReminder(hourOfDay, minute);
        updateReminderStatus();
    }, currentHour, currentMinute, true).show();
    // true: dùng định dạng 24h
}
```

---

# QUESTIONBANKFRAGMENT

File: `fragments/QuestionBankFragment.java`

Hiển thị ngân hàng câu hỏi với bộ lọc chip và chức năng bookmark.

---

## onViewCreated()

```java
public void onViewCreated(View view, Bundle savedInstanceState) {
    cartManager = new StudyCartManager(requireContext());
    adapter = new QuestionBankAdapter(this, getBookmarkedIds());
    // "this" là OnQuestionBankListener
    // getBookmarkedIds() trả về Set<Integer> các ID đã bookmark

    binding.rvQuestions.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvQuestions.setAdapter(adapter);

    setupFilters();
    loadQuestions();
}
```

---

## setupFilters()

Lắng nghe thay đổi ChipGroup. Khi chip nào được chọn, cập nhật `currentFilter` và gọi `applyFilter()`.

```java
private void setupFilters() {
    binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
        if (checkedIds.contains(R.id.chipImportant)) {
            currentFilter = "important";
        } else if (checkedIds.contains(R.id.chipBookmarked)) {
            currentFilter = "bookmarked";
        } else {
            currentFilter = "all";
        }
        applyFilter();
    });
}
```

---

## loadQuestions()

Gọi API lấy 200 câu hỏi. Override `parseNetworkResponse` để xử lý UTF-8.

```java
private void loadQuestions() {
    binding.progressBar.setVisibility(View.VISIBLE);
    String url = apiClient.endpoint("/api/question?page=1&pageSize=200");

    JsonObjectRequest request = new JsonObjectRequest(...) {
        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            // Override bắt buộc để đọc tiếng Việt đúng encoding
            String jsonString = new String(response.data, StandardCharsets.UTF_8);
            return Response.success(new JSONObject(jsonString), ...);
        }

        @Override
        public Map<String, String> getHeaders() {
            return ApiClient.authHeaders(sessionManager);
        }
    };
}
```

---

## parseQuestions(JSONObject response)

Server có thể trả về dữ liệu dưới key `"items"` hoặc `"questions"` — code handle cả 2 trường hợp.

```java
private void parseQuestions(JSONObject response) {
    allQuestions.clear();
    JSONArray items = response.optJSONArray("items");
    if (items == null) {
        items = response.optJSONArray("questions");  // Fallback
    }
    if (items == null || items.length() == 0) {
        showEmpty(getString(R.string.question_bank_empty));
        return;
    }

    for (int i = 0; i < items.length(); i++) {
        JSONObject obj = items.optJSONObject(i);
        allQuestions.add(new AdminQuestion(
            obj.optInt("questionId", -1),
            obj.optString("content", ""),
            // ... các field khác
        ));
    }
    applyFilter();
}
```

---

## applyFilter()

Lọc `allQuestions` dựa theo `currentFilter` và cập nhật Adapter.

```java
private void applyFilter() {
    List<AdminQuestion> filtered;
    switch (currentFilter) {
        case "important":
            filtered = new ArrayList<>();
            for (AdminQuestion q : allQuestions) {
                if (q.isImportant()) filtered.add(q);
            }
            break;
        case "bookmarked":
            Set<Integer> bookmarked = getBookmarkedIds();
            filtered = new ArrayList<>();
            for (AdminQuestion q : allQuestions) {
                if (bookmarked.contains(q.getQuestionId())) filtered.add(q);
            }
            break;
        default:  // "all"
            filtered = new ArrayList<>(allQuestions);
    }

    binding.tvQuestionCount.setText(getString(R.string.question_bank_count, filtered.size()));
    adapter.submitList(filtered);
}
```

---

## onBookmarkToggle(AdminQuestion question, boolean addToCart)

Callback từ Adapter khi user nhấn icon bookmark. Cập nhật StudyCartManager, refresh icon bookmark trong Adapter, cập nhật badge trên MainActivity.

```java
public void onBookmarkToggle(AdminQuestion question, boolean addToCart) {
    if (addToCart) {
        cartManager.addQuestion(question);
    } else {
        cartManager.removeQuestion(question.getQuestionId());
    }

    adapter.updateBookmarks(getBookmarkedIds());
    // Truyền lại Set mới để Adapter cập nhật icon

    updateCartBadge();
    // Cập nhật số badge trên bottom nav

    if ("bookmarked".equals(currentFilter)) {
        applyFilter();
        // Nếu đang lọc bookmark mà xóa bookmark → re-filter để ẩn câu đó
    }
}

private void updateCartBadge() {
    if (getActivity() instanceof MainActivity) {
        ((MainActivity) getActivity()).refreshCartBadge();
    }
}
```

---

# STUDYCARTFRAGMENT

File: `fragments/StudyCartFragment.java`

---

## loadCart()

Đọc dữ liệu từ StudyCartManager và cập nhật toàn bộ UI.

```java
private void loadCart() {
    List<AdminQuestion> items = cartManager.getCartItems();
    updateSummary(items);  // Cập nhật tổng câu, tổng câu điểm liệt

    if (items.isEmpty()) {
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        binding.rvCart.setVisibility(View.GONE);
        binding.btnPractice.setEnabled(false);
        binding.btnPractice.setAlpha(0.5f);  // Làm mờ nút khi disabled
    } else {
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.rvCart.setVisibility(View.VISIBLE);
        binding.btnPractice.setEnabled(true);
        binding.btnPractice.setAlpha(1f);
        adapter.submitList(items);
    }
}
```

---

## confirmClearAll()

Hiện AlertDialog xác nhận trước khi xóa hết. Pattern này dùng chung với tất cả confirm dialog trong app.

```java
private void confirmClearAll() {
    if (cartManager.getCartCount() == 0) return;

    new AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.admin_dialog_confirm_delete))
        .setMessage(getString(R.string.cart_clear_confirm))
        .setPositiveButton(getString(R.string.admin_btn_delete), (dialog, which) -> {
            cartManager.clearCart();
            loadCart();
            updateCartBadge();
        })
        .setNegativeButton(getString(R.string.admin_btn_cancel), null)
        .show();
}
```

---

# ADMINFRAGMENT

File: `fragments/AdminFragment.java`

AdminFragment là "cha" của 3 sub-fragment. Nó cung cấp `sessionManager` và `apiClient` cho các con.

---

## setupViewPager()

Tạo 3 Fragment con và đưa vào ViewPager2.

```java
private void setupViewPager() {
    usersTabFragment = new AdminUsersTabFragment();
    questionsTabFragment = new AdminQuestionsTabFragment();
    examsTabFragment = new AdminExamsTabFragment();

    AdminViewPagerAdapter adapter = new AdminViewPagerAdapter(this);
    // "this" là Fragment cha (dùng childFragmentManager)

    adapter.addFragment(usersTabFragment, getString(R.string.admin_tab_users));
    adapter.addFragment(questionsTabFragment, getString(R.string.admin_tab_questions));
    adapter.addFragment(examsTabFragment, getString(R.string.admin_tab_exams));

    binding.viewPager.setAdapter(adapter);

    binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            currentTabPosition = position;
            // Lưu tab đang chọn để FAB biết mở dialog nào
        }
    });

    new TabLayoutMediator(binding.tabLayout, binding.viewPager,
        (tab, position) -> tab.setText(adapter.getPageTitle(position))
    ).attach();
    // TabLayoutMediator kết nối TabLayout với ViewPager2 tự động
}
```

---

## onFabClicked()

FAB (nút + tròn góc dưới phải) tạo item mới tương ứng với tab đang hiển thị.

```java
private void onFabClicked() {
    switch (currentTabPosition) {
        case 0:  // Tab Users
            usersTabFragment.showCreateUserDialog();
            break;
        case 1:  // Tab Questions
            questionsTabFragment.showCreateQuestionDialog();
            break;
        case 2:  // Tab Exams
            Toast.makeText(requireContext(), "Không thể tạo bài thi từ Admin", Toast.LENGTH_SHORT).show();
            break;
    }
}
```

---

## getSessionManager() và getApiClient()

Các hàm `public` để Fragment con truy cập tài nguyên của cha.

```java
public SessionManager getSessionManager() { return sessionManager; }
public ApiClient getApiClient() { return apiClient; }
public void refreshDashboard() { loadDashboardStats(); }
```

Fragment con (ví dụ AdminExamsTabFragment) dùng như sau:

```java
AdminFragment parent = (AdminFragment) getParentFragment();
sessionManager = parent.getSessionManager();
apiClient = parent.getApiClient();
```

---

# ADMINEXAMSTABFRAGMENT

File: `fragments/AdminExamsTabFragment.java`

---

## loadExams()

Dùng `JsonArrayRequest` (khác với `JsonObjectRequest`) vì API này trả về array thẳng, không bọc trong object.

```java
private void loadExams() {
    String url = apiClient.endpoint("/api/exam/admin/all-exams");

    JsonArrayRequest request = new JsonArrayRequest(
        Request.Method.GET, url, null,
        response -> {
            List<AdminExam> exams = parseExams(response);
            if (exams.isEmpty()) {
                binding.tvEmptyExams.setVisibility(View.VISIBLE);
            } else {
                adapter.submitList(exams);
            }
        },
        error -> Toast.makeText(...).show()
    ) {
        @Override
        public Map<String, String> getHeaders() {
            return ApiClient.authHeaders(sessionManager);
        }
    };
    apiClient.getRequestQueue().add(request);
}
```

---

## onViewExam(AdminExam exam)

Callback khi click nút "Xem". Truyền dữ liệu sang ExamResultActivity qua Intent Extras.

```java
public void onViewExam(AdminExam exam) {
    Intent intent = new Intent(requireContext(), ExamResultActivity.class);
    intent.putExtra(ExamResultActivity.EXTRA_EXAM_ID, exam.getExamId());
    intent.putExtra(ExamResultActivity.EXTRA_SCORE, exam.getScore());
    intent.putExtra(ExamResultActivity.EXTRA_TOTAL_QUESTIONS, exam.getTotalQuestions());
    intent.putExtra(ExamResultActivity.EXTRA_PASSED, exam.isPassed());
    intent.putExtra(ExamResultActivity.EXTRA_FAILED_IMPORTANT, false);
    startActivity(intent);
}
```

---

## onDeleteExam(AdminExam exam) và deleteExam(int examId)

Hiện dialog xác nhận, rồi gọi DELETE API. Sau khi xóa thành công, reload list và yêu cầu AdminFragment cập nhật số liệu thống kê.

```java
public void onDeleteExam(AdminExam exam) {
    // Inflate dialog bằng ViewBinding
    DialogConfirmDeleteBinding dialogBinding = DialogConfirmDeleteBinding.inflate(getLayoutInflater());
    dialogBinding.tvDeleteMessage.setText("Bạn có chắc chắn muốn xóa bài thi #" + exam.getExamId() + " không?");

    AlertDialog dialog = new AlertDialog.Builder(requireContext())
        .setView(dialogBinding.getRoot())
        .create();

    dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
    dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
        deleteExam(exam.getExamId());
        dialog.dismiss();
    });
    dialog.show();
}

private void deleteExam(int examId) {
    StringRequest request = new StringRequest(
        Request.Method.DELETE,
        apiClient.endpoint("/api/exam/" + examId),
        response -> {
            Toast.makeText(requireContext(), "Xóa bài thi thành công", Toast.LENGTH_SHORT).show();
            loadExams();  // Reload danh sách

            AdminFragment parent = (AdminFragment) getParentFragment();
            if (parent != null) {
                parent.refreshDashboard();
                // Yêu cầu cha cập nhật con số "Tổng bài thi"
            }
        },
        error -> {
            int status = error.networkResponse == null ? -1 : error.networkResponse.statusCode;
            Toast.makeText(requireContext(), "Lỗi: " + status, Toast.LENGTH_SHORT).show();
        }
    ) {
        @Override
        public Map<String, String> getHeaders() {
            return ApiClient.authHeaders(sessionManager);
        }
    };
    apiClient.getRequestQueue().add(request);
}
```

---

# SESSIONMANAGER

File: `utils/SessionManager.java`

SharedPreferences tên `"motorcycle_session"`. Các key: `jwt_token`, `email`, `role`.

```java
public void saveSession(String email, String token, String role) {
    preferences.edit()
        .putString("jwt_token", token)
        .putString("email", email)
        .putString("role", role)
        .apply();
    // apply() ghi không đồng bộ (nhanh hơn commit() nhưng không đảm bảo ghi xong ngay)
}

public boolean isLoggedIn() {
    String token = getToken();
    return token != null && !token.isEmpty();
}

public void clear() {
    preferences.edit().clear().apply();
    // Xóa tất cả key, dùng khi logout
}
```

---

# STUDYCARTMANAGER

File: `utils/StudyCartManager.java`

SharedPreferences tên `"study_cart"`. Lưu danh sách câu hỏi dạng JSON array string.

```java
public void addQuestion(AdminQuestion question) {
    List<AdminQuestion> cart = getCartItems();
    for (AdminQuestion q : cart) {
        if (q.getQuestionId() == question.getQuestionId()) {
            return;  // Đã có rồi, không thêm trùng
        }
    }
    cart.add(question);
    saveCart(cart);
}

public List<AdminQuestion> getCartItems() {
    String json = preferences.getString(KEY_CART_ITEMS, "[]");
    // Đọc JSON string, nếu chưa có thì dùng array rỗng "[]"
    List<AdminQuestion> items = new ArrayList<>();
    JSONArray array = new JSONArray(json);
    for (int i = 0; i < array.length(); i++) {
        JSONObject obj = array.optJSONObject(i);
        items.add(new AdminQuestion(
            obj.optInt("questionId", -1),
            // ... parse các field khác
        ));
    }
    return items;
}

private void saveCart(List<AdminQuestion> items) {
    JSONArray array = new JSONArray();
    for (AdminQuestion q : items) {
        JSONObject obj = new JSONObject();
        obj.put("questionId", q.getQuestionId());
        obj.put("content", q.getContent());
        // ... serialize tất cả field
        array.put(obj);
    }
    preferences.edit().putString(KEY_CART_ITEMS, array.toString()).apply();
    // Lưu JSONArray dưới dạng String
}
```

---

# NOTIFICATIONHELPER

File: `utils/NotificationHelper.java`

---

## createNotificationChannels()

Android 8 (Oreo) trở lên yêu cầu tạo Notification Channel trước khi hiển thị thông báo. Channel được tạo trong constructor của NotificationHelper.

```java
private void createNotificationChannels() {
    NotificationChannel channel = new NotificationChannel(
        CHANNEL_STUDY_REMINDER,  // ID channel: "study_reminder"
        "Nhắc nhở học tập",       // Tên hiển thị trong Settings
        NotificationManager.IMPORTANCE_HIGH  // Mức độ: hiện pop-up, có âm thanh
    );
    channel.setDescription("Thông báo nhắc nhở ôn thi hàng ngày");
    NotificationManager manager = context.getSystemService(NotificationManager.class);
    manager.createNotificationChannel(channel);
}
```

---

## scheduleReminder(int hour, int minute)

Đặt báo thức lặp lại hàng ngày vào giờ chỉ định.

```java
public void scheduleReminder(int hour, int minute) {
    // Lưu settings trước
    preferences.edit()
        .putBoolean(KEY_REMINDER_ENABLED, true)
        .putInt(KEY_REMINDER_HOUR, hour)
        .putInt(KEY_REMINDER_MINUTE, minute)
        .apply();

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    Intent intent = new Intent(context, StudyReminderReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
        context, REMINDER_REQUEST_CODE, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        // FLAG_IMMUTABLE: yêu cầu bảo mật từ Android 12
    );

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, 0);

    if (calendar.before(Calendar.getInstance())) {
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        // Nếu giờ đã qua trong hôm nay, đặt lịch cho ngày mai
    }

    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,            // Loại: đánh thức máy nếu đang ngủ
        calendar.getTimeInMillis(),          // Thời điểm đầu tiên
        AlarmManager.INTERVAL_DAY,           // Lặp lại mỗi 24 giờ
        pendingIntent
    );
}
```

---

## showStudyReminder()

Được gọi bởi `StudyReminderReceiver` khi đến giờ. Tạo và hiển thị notification với nội dung động.

```java
public void showStudyReminder() {
    StudyCartManager cartManager = new StudyCartManager(context);
    int cartCount = cartManager.getCartCount();

    // Nội dung notification phụ thuộc vào giỏ ôn tập
    String contentText = cartCount > 0
        ? "Bạn có " + cartCount + " câu hỏi trong giỏ ôn tập. Cùng luyện tập nhé!"
        : "Hãy dành ít phút ôn thi lý thuyết lái xe hôm nay!";

    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STUDY_REMINDER)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Đã đến giờ ôn thi!")
        .setContentText(contentText)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
        // BigTextStyle: cho phép text dài hơn trong notification
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)  // Tự ẩn khi user tap vào
        .setContentIntent(pendingIntent);  // Mở MainActivity khi tap

    NotificationManagerCompat.from(context).notify(REMINDER_REQUEST_CODE, builder.build());
}
```

---

# STUDYREMINDERRECEIVER

File: `utils/StudyReminderReceiver.java`

BroadcastReceiver đơn giản nhất trong project. Khi AlarmManager đến giờ, hệ thống tạo Intent broadcast và gửi đến Receiver này. Receiver chỉ làm một việc: gọi `showStudyReminder()`.

```java
public class StudyReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        new NotificationHelper(context).showStudyReminder();
    }
}
```

Receiver phải được khai báo trong `AndroidManifest.xml`. Không cần Activity đang chạy, Receiver hoạt động kể cả khi app đang đóng.

---

# APICLIENT

File: `network/ApiClient.java`

Singleton đảm bảo chỉ có 1 `RequestQueue` trong toàn app.

```java
public static ApiClient getInstance(Context context) {
    if (instance == null) {
        // Chỉ tạo instance mới nếu chưa có
        instance = new ApiClient(context.getApplicationContext());
        // Dùng applicationContext để tránh memory leak
        // (Activity context có thể bị destroy, applicationContext thì không)
    }
    return instance;
}

public String endpoint(String path) {
    return getBaseUrl() + path;
    // getBaseUrl() đọc từ strings.xml: "http://10.0.2.2:5000"
}

public static Map<String, String> authHeaders(SessionManager sessionManager) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + sessionManager.getToken());
    return headers;
    // Mọi request cần auth đều cần 2 header này
}
```

---

# CÁC PATTERN QUAN TRỌNG CẦN NHỚ

## Pattern 1: Fragment nhận callback từ Adapter

Adapter định nghĩa Interface, Fragment implement Interface đó.

```
Fragment (implement Listener)
    ↓ truyền "this" vào Adapter
Adapter (lưu Listener)
    ↓ khi user click item
Adapter gọi listener.onSomeAction(model)
    ↓
Fragment.onSomeAction(model) được gọi
    ↓ Fragment xử lý (mở màn hình, gọi API, ...)
```

Ví dụ:
```java
// Trong Adapter
public interface OnExamActionListener {
    void onViewExam(AdminExam exam);
    void onDeleteExam(AdminExam exam);
}

// Fragment implement interface
public class AdminExamsTabFragment extends Fragment
    implements AdminExamAdapter.OnExamActionListener {

    public void onViewExam(AdminExam exam) {
        // Mở ExamResultActivity
    }
    public void onDeleteExam(AdminExam exam) {
        // Hiện dialog xác nhận
    }
}
```

## Pattern 2: Dialog với ViewBinding

```java
// 1. Inflate binding cho dialog
DialogConfirmDeleteBinding dialogBinding = DialogConfirmDeleteBinding.inflate(getLayoutInflater());

// 2. Set nội dung
dialogBinding.tvDeleteMessage.setText("Bạn có chắc chắn muốn xóa...?");

// 3. Tạo AlertDialog dùng view từ binding
AlertDialog dialog = new AlertDialog.Builder(requireContext())
    .setView(dialogBinding.getRoot())
    .create();

// 4. Setup button listeners
dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
    // Thực hiện hành động
    dialog.dismiss();
});

// 5. Hiển thị
dialog.show();
```

## Pattern 3: Sub-fragment lấy tài nguyên từ Fragment cha

```java
// Trong sub-fragment (AdminExamsTabFragment)
public void onViewCreated(...) {
    AdminFragment parent = (AdminFragment) getParentFragment();
    if (parent != null) {
        sessionManager = parent.getSessionManager();
        apiClient = parent.getApiClient();
    }
}

// Sau khi thực hiện CRUD, thông báo cho cha cập nhật thống kê
AdminFragment parent = (AdminFragment) getParentFragment();
if (parent != null) {
    parent.refreshDashboard();
}
```

## Pattern 4: JSON parsing an toàn

```java
// Dùng opt*() thay vì get*() để tránh JSONException
String value = response.optString("key", "defaultValue");
int number = response.optInt("count", 0);
boolean flag = response.optBoolean("isPassed", false);
JSONArray array = response.optJSONArray("items");  // null nếu không có key

// Nếu dùng get*() mà key không tồn tại → throw JSONException → crash app
// String value = response.getString("key");  // NGUY HIỂM
```
