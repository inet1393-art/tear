package com.reminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminder.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context.applicationContext)
                val now = System.currentTimeMillis()

                val tasks = db.taskDao().getUpcoming(now)
                tasks.forEach { AlarmScheduler.scheduleTask(context, it) }

                val installments = db.installmentDao().getUpcoming(now)
                installments.forEach { inst ->
                    val loan = db.loanDao().getById(inst.loanId)
                    AlarmScheduler.scheduleInstallment(context, inst, loan?.name ?: "وام")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
