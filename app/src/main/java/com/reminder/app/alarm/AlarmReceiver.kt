package com.reminder.app.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.reminder.app.R
import com.reminder.app.ReminderApplication

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: return
        val id = intent.getLongExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "یادآوری"
        val description = intent.getStringExtra("description") ?: ""

        val fullScreenIntent = Intent(context, FullScreenAlarmActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra("type", type)
            putExtra("id", id)
            putExtra("title", title)
            putExtra("description", description)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, (id.toInt() * 10 + 1), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setContentIntent(fullScreenPendingIntent)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id.toInt(), notification)

        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            // اگر امکان اجرای مستقیم نبود، نوتیفیکیشن تمام‌صفحه خودش صفحه را باز می‌کند
        }
    }
}
