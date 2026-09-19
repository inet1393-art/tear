package com.reminder.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.reminder.app.alarm.AlarmScheduler
import com.reminder.app.data.AppDatabase
import com.reminder.app.data.Task
import com.reminder.app.databinding.ActivityAddTaskBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private val calendar = Calendar.getInstance()
    private var editingTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateDateTimeLabel()

        val taskId = intent.getLongExtra("taskId", -1)
        if (taskId != -1L) {
            lifecycleScope.launch {
                val db = AppDatabase.getInstance(applicationContext)
                editingTask = db.taskDao().getById(taskId)
                editingTask?.let { task ->
                    binding.etTitle.setText(task.title)
                    binding.etDescription.setText(task.description)
                    calendar.timeInMillis = task.dueDateTime
                    updateDateTimeLabel()
                    binding.btnDelete.visibility = android.view.View.VISIBLE
                }
            }
        }

        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    calendar.set(Calendar.YEAR, y)
                    calendar.set(Calendar.MONTH, m)
                    calendar.set(Calendar.DAY_OF_MONTH, d)
                    updateDateTimeLabel()
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnPickTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, h, min ->
                    calendar.set(Calendar.HOUR_OF_DAY, h)
                    calendar.set(Calendar.MINUTE, min)
                    calendar.set(Calendar.SECOND, 0)
                    updateDateTimeLabel()
                },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
            ).show()
        }

        binding.btnSave.setOnClickListener { save() }

        binding.btnDelete.setOnClickListener {
            editingTask?.let { task ->
                lifecycleScope.launch {
                    AppDatabase.getInstance(applicationContext).taskDao().delete(task)
                    AlarmScheduler.cancelTask(applicationContext, task.id)
                    finish()
                }
            }
        }
    }

    private fun updateDateTimeLabel() {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale("fa"))
        val stf = SimpleDateFormat("HH:mm", Locale("fa"))
        binding.tvSelectedDate.text = sdf.format(calendar.time)
        binding.tvSelectedTime.text = stf.format(calendar.time)
    }

    private fun save() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.etTitle.error = "عنوان را وارد کنید"
            return
        }
        val description = binding.etDescription.text.toString().trim()

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val task = editingTask?.copy(
                title = title,
                description = description,
                dueDateTime = calendar.timeInMillis,
                isDone = false
            ) ?: Task(
                title = title,
                description = description,
                dueDateTime = calendar.timeInMillis
            )

            val id = if (editingTask != null) {
                db.taskDao().update(task)
                task.id
            } else {
                db.taskDao().insert(task)
            }

            val finalTask = task.copy(id = id)
            AlarmScheduler.scheduleTask(applicationContext, finalTask)
            finish()
        }
    }
}
