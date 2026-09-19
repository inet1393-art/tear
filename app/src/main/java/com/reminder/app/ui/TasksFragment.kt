package com.reminder.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.reminder.app.AddTaskActivity
import com.reminder.app.adapter.TaskAdapter
import com.reminder.app.alarm.AlarmScheduler
import com.reminder.app.data.AppDatabase
import com.reminder.app.databinding.FragmentTasksBinding
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getInstance(requireContext())

        adapter = TaskAdapter(
            onToggle = { task ->
                lifecycleScope.launch {
                    val updated = task.copy(isDone = !task.isDone)
                    db.taskDao().update(updated)
                    if (updated.isDone) {
                        AlarmScheduler.cancelTask(requireContext(), task.id)
                    } else {
                        AlarmScheduler.scheduleTask(requireContext(), updated)
                    }
                }
            },
            onDelete = { task ->
                lifecycleScope.launch {
                    db.taskDao().delete(task)
                    AlarmScheduler.cancelTask(requireContext(), task.id)
                }
            },
            onClick = { task ->
                val intent = Intent(requireContext(), AddTaskActivity::class.java)
                intent.putExtra("taskId", task.id)
                startActivity(intent)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        db.taskDao().getAll().observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            binding.emptyView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
