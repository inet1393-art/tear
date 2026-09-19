package com.reminder.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reminder.app.data.Loan
import com.reminder.app.databinding.ItemLoanBinding

class LoanAdapter(private val onClick: (Loan) -> Unit) :
    ListAdapter<Loan, LoanAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemLoanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLoanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val loan = getItem(position)
        holder.binding.tvName.text = loan.name
        holder.binding.tvAmount.text = "مبلغ کل: ${loan.totalAmount.toLong()} تومان"
        holder.binding.tvInstallments.text = "تعداد اقساط: ${loan.installmentCount}"
        holder.binding.root.setOnClickListener { onClick(loan) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Loan>() {
            override fun areItemsTheSame(oldItem: Loan, newItem: Loan) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Loan, newItem: Loan) = oldItem == newItem
        }
    }
}
