package com.reminder.app.alarm

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.reminder.app.data.AppDatabase
import com.reminder.app.databinding.ActivityFullScreenAlarmBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FullScreenAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenAlarmBinding
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()
        binding = ActivityFullScreenAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type") ?: ""
        val id = intent.getLongExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "یادآوری"
        val description = intent.getStringExtra("description") ?: ""

        binding.tvTitle.text = title
        binding.tvDescription.text = description

        startAlarmSound()

        binding.btnDismiss.setOnClickListener { handleDone(type, id) }
        binding.btnSnooze.setOnClickListener { handleSnooze(type, id, title, description) }
    }

    private fun setupWindow() {
        val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            km.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startAlarmSound() {
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(this, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            ringtone?.play()
        } catch (e: Exception) { }

        vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        val pattern = longArrayOf(0, 800, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopAlarmSound() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    private fun handleDone(type: String, id: Long) {
        stopAlarmSound()
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(id.toInt())
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(applicationContext)
            if (type == AlarmScheduler.TYPE_TASK) {
                val task = db.taskDao().getById(id)
                if (task != null) db.taskDao().update(task.copy(isDone = true))
            }
        }
        finish()
    }

    private fun handleSnooze(type: String, id: Long, title: String, description: String) {
        stopAlarmSound()
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(id.toInt())
        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("type", type)
            putExtra("id", id)
            putExtra("title", title)
            putExtra("description", description)
        }
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            this, (id.toInt() * 100), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pi)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }

    override fun onBackPressed() {
        // از بستن با دکمه بازگشت جلوگیری می‌شود تا کاربر یکی از گزینه‌ها را انتخاب کند
    }
}
