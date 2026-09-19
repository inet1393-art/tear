package com.reminder.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.reminder.app.data.Installment
import com.reminder.app.data.Task

object AlarmScheduler {

    const val TYPE_TASK = "TASK"
    const val TYPE_INSTALLMENT = "INSTALLMENT"

    private fun requestCodeForTask(id: Long): Int = (id * 2).toInt()
    private fun requestCodeForInstallment(id: Long): Int = (id * 2 + 1).toInt()

    fun scheduleTask(context: Context, task: Task) {
        if (task.isDone) return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", TYPE_TASK)
            putExtra("id", task.id)
            putExtra("title", task.title)
            putExtra("description", task.description ?: "")
        }
        schedule(context, requestCodeForTask(task.id), intent, task.dueDateTime)
    }

    fun cancelTask(context: Context, taskId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        cancel(context, requestCodeForTask(taskId), intent)
    }

    fun scheduleInstallment(context: Context, installment: Installment, loanName: String) {
        if (installment.isPaid) return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", TYPE_INSTALLMENT)
            putExtra("id", installment.id)
            putExtra("title", "قسط $loanName")
            putExtra(
                "description",
                "قسط شماره ${installment.installmentNumber} به مبلغ ${installment.amount.toLong()} تومان"
            )
        }
        schedule(context, requestCodeForInstallment(installment.id), intent, installment.dueDate)
    }

    fun cancelInstallment(context: Context, installmentId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        cancel(context, requestCodeForInstallment(installmentId), intent)
    }

    private fun schedule(context: Context, requestCode: Int, intent: Intent, triggerAt: Long) {
        if (triggerAt <= System.currentTimeMillis()) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun cancel(context: Context, requestCode: Int, intent: Intent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }
}
