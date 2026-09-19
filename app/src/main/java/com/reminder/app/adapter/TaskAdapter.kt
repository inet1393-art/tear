package com.reminder.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reminder.app.data.Task
import com.reminder.app.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onToggle: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale("fa"))
        holder.binding.tvTitle.text = task.title
        holder.binding.tvDate.text = sdf.format(Date(task.dueDateTime))
        holder.binding.checkBox.isChecked = task.isDone
        holder.binding.checkBox.setOnClickListener { onToggle(task) }
        holder.binding.btnDelete.setOnClickListener { onDelete(task) }
        holder.binding.root.setOnClickListener { onClick(task) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
        }
    }
}
