package com.reminder.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.reminder.app.adapter.InstallmentAdapter
import com.reminder.app.alarm.AlarmScheduler
import com.reminder.app.data.AppDatabase
import com.reminder.app.databinding.ActivityLoanDetailBinding
import kotlinx.coroutines.launch

class LoanDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoanDetailBinding
    private lateinit var adapter: InstallmentAdapter
    private var loanId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loanId = intent.getLongExtra("loanId", -1)
        val db = AppDatabase.getInstance(applicationContext)

        adapter = InstallmentAdapter { inst ->
            lifecycleScope.launch {
                val updated = inst.copy(
                    isPaid = !inst.isPaid,
                    paidDate = if (!inst.isPaid) System.currentTimeMillis() else null
                )
                db.installmentDao().update(updated)
                if (updated.isPaid) {
                    AlarmScheduler.cancelInstallment(applicationContext, updated.id)
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            val loan = db.loanDao().getById(loanId)
            loan?.let {
                binding.tvName.text = it.name
                binding.tvTotalAmount.text = "مبلغ کل وام: ${it.totalAmount.toLong()} تومان"
                binding.tvLender.text = it.lenderName?.let { l -> "وام‌دهنده: $l" } ?: ""
            }
        }

        db.installmentDao().getForLoan(loanId).observe(this) { list ->
            adapter.submitList(list)
        }

        binding.btnDeleteLoan.setOnClickListener {
            lifecycleScope.launch {
                val loan = db.loanDao().getById(loanId)
                val installments = db.installmentDao().getAllForLoanSync(loanId)
                installments.forEach { AlarmScheduler.cancelInstallment(applicationContext, it.id) }
                loan?.let { db.loanDao().delete(it) }
                finish()
            }
        }
    }
}
