package com.example.motorcycletheory.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.activities.MainActivity;

import java.util.Calendar;

public class NotificationHelper {
    public static final String CHANNEL_STUDY_REMINDER = "study_reminder";
    private static final String PREF_NAME = "notification_prefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    private static final int REMINDER_REQUEST_CODE = 1001;

    private final Context context;
    private final SharedPreferences preferences;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_STUDY_REMINDER,
                "Nhắc nhở học tập",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Thông báo nhắc nhở ôn thi lý thuyết lái xe hàng ngày");

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public void showStudyReminder() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        StudyCartManager cartManager = new StudyCartManager(context);
        int cartCount = cartManager.getCartCount();
        String contentText = cartCount > 0
                ? "Bạn có " + cartCount + " câu hỏi trong giỏ ôn tập. Cùng luyện tập nhé!"
                : "Hãy dành ít phút ôn thi lý thuyết lái xe hôm nay!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STUDY_REMINDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Đã đến giờ ôn thi!")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
            managerCompat.notify(REMINDER_REQUEST_CODE, builder.build());
        } catch (SecurityException ignored) {
        }
    }

    public void scheduleReminder(int hour, int minute) {
        preferences.edit()
                .putBoolean(KEY_REMINDER_ENABLED, true)
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .apply();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, StudyReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, REMINDER_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    public void cancelReminder() {
        preferences.edit().putBoolean(KEY_REMINDER_ENABLED, false).apply();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, StudyReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, REMINDER_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    public boolean isReminderEnabled() {
        return preferences.getBoolean(KEY_REMINDER_ENABLED, false);
    }

    public int getReminderHour() {
        return preferences.getInt(KEY_REMINDER_HOUR, 20);
    }

    public int getReminderMinute() {
        return preferences.getInt(KEY_REMINDER_MINUTE, 0);
    }
}
