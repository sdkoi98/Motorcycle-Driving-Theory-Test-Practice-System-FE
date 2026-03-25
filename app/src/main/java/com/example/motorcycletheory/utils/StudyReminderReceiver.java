package com.example.motorcycletheory.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StudyReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper helper = new NotificationHelper(context);
        helper.showStudyReminder();
    }
}
