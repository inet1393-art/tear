package com.reminder.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reminder.app.data.Installment
import com.reminder.app.databinding.ItemInstallmentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InstallmentAdapter(private val onTogglePaid: (Installment) -> Unit) :
    ListAdapter<Installment, InstallmentAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemInstallmentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemInstallmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val inst = getItem(position)
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale("fa"))
        holder.binding.tvNumber.text = "قسط ${inst.installmentNumber}"
        holder.binding.tvDate.text = sdf.format(Date(inst.dueDate))
        holder.binding.tvAmount.text = "${inst.amount.toLong()} تومان"
        holder.binding.checkPaid.isChecked = inst.isPaid
        holder.binding.checkPaid.setOnClickListener { onTogglePaid(inst) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Installment>() {
            override fun areItemsTheSame(oldItem: Installment, newItem: Installment) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Installment, newItem: Installment) = oldItem == newItem
        }
    }
}
