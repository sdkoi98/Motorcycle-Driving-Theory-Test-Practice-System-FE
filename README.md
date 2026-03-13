# Motorcycle Theory FE (Android Java)

Frontend duoc scaffold bang Android native Java theo stack tham khao tu AIFood.

## Technology

- Android Gradle Plugin 8.10.1
- Gradle wrapper 8.13
- Java 11 bytecode target
- AndroidX + Material Components
- Volley + Gson cho API layer

## Project Structure

- `app/src/main/java/com/example/motorcycletheory/activities`
	- `LoginActivity`
	- `MainActivity`
- `app/src/main/java/com/example/motorcycletheory/fragments`
	- `HomeFragment` (Thi thu)
	- `HistoryFragment` (Lich su thi)
	- `AdminFragment` (Admin module)
	- `ProfileFragment` (Thong tin + dang xuat)
- `app/src/main/java/com/example/motorcycletheory/network`
	- `ApiClient` (Volley queue + base URL)
- `app/src/main/java/com/example/motorcycletheory/utils`
	- `SessionManager` (SharedPreferences)

## Run

1. Mo terminal tai thu muc project FE.
2. Chay lenh:

```bash
./gradlew assembleDebug
```

Windows CMD:

```bat
gradlew.bat assembleDebug
```

APK output:

- `app/build/outputs/apk/debug/app-debug.apk`

## API Base URL

File `app/src/main/res/values/strings.xml`:

- `api_base_url = http://10.0.2.2:5121/`

`10.0.2.2` dung cho Android Emulator de goi localhost tu may phat trien.

## Next Implementation Steps

- Thay login tam trong `LoginActivity` bang call `POST /api/auth/login`.
- Hoan thien `HomeFragment` voi flow:
	- `POST /api/exam/generate`
	- `POST /api/exam/submit`
- Hoan thien `HistoryFragment` voi `GET /api/exam/history`.
- Hoan thien `AdminFragment` voi:
	- `GET /api/admin/stats`
	- `GET /api/admin/users`
	- CRUD user neu role la Admin.