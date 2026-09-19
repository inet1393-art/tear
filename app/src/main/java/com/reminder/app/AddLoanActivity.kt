package com.reminder.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.reminder.app.alarm.AlarmScheduler
import com.reminder.app.data.AppDatabase
import com.reminder.app.data.Installment
import com.reminder.app.data.Loan
import com.reminder.app.databinding.ActivityAddLoanBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddLoanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLoanBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLoanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        updateDateLabel()

        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    calendar.set(Calendar.YEAR, y)
                    calendar.set(Calendar.MONTH, m)
                    calendar.set(Calendar.DAY_OF_MONTH, d)
                    updateDateLabel()
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnCalc.setOnClickListener {
            val total = binding.etTotalAmount.text.toString().toDoubleOrNull()
            val count = binding.etInstallmentCount.text.toString().toIntOrNull()
            if (total != null && count != null && count > 0) {
                val each = total / count
                binding.etInstallmentAmount.setText(each.toLong().toString())
            }
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun updateDateLabel() {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale("fa"))
        binding.tvSelectedDate.text = sdf.format(calendar.time)
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = "نام وام را وارد کنید"
            return
        }
        val total = binding.etTotalAmount.text.toString().toDoubleOrNull()
        val count = binding.etInstallmentCount.text.toString().toIntOrNull()
        val eachAmount = binding.etInstallmentAmount.text.toString().toDoubleOrNull()

        if (total == null || count == null || count <= 0 || eachAmount == null) {
            Toast.makeText(this, "لطفا مبلغ کل، تعداد و مبلغ هر قسط را درست وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val lender = binding.etLender.text.toString().trim()
        val note = binding.etNote.text.toString().trim()
        val interest = binding.etInterestRate.text.toString().toDoubleOrNull()

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val loan = Loan(
                name = name,
                lenderName = lender.ifEmpty { null },
                totalAmount = total,
                installmentCount = count,
                installmentAmount = eachAmount,
                startDate = calendar.timeInMillis,
                interestRate = interest,
                note = note.ifEmpty { null }
            )
            val loanId = db.loanDao().insert(loan)

            val installments = mutableListOf<Installment>()
            val cal = Calendar.getInstance().apply { timeInMillis = calendar.timeInMillis }
            for (i in 1..count) {
                installments.add(
                    Installment(
                        loanId = loanId,
                        installmentNumber = i,
                        dueDate = cal.timeInMillis,
                        amount = eachAmount
                    )
                )
                cal.add(Calendar.MONTH, 1)
            }
            db.installmentDao().insertAll(installments)

            val savedInstallments = db.installmentDao().getAllForLoanSync(loanId)
            savedInstallments.forEach { inst ->
                AlarmScheduler.scheduleInstallment(applicationContext, inst, name)
            }

            finish()
        }
    }
}
