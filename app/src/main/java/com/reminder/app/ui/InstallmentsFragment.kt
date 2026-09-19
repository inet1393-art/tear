package com.reminder.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.reminder.app.AddLoanActivity
import com.reminder.app.LoanDetailActivity
import com.reminder.app.adapter.LoanAdapter
import com.reminder.app.data.AppDatabase
import com.reminder.app.databinding.FragmentInstallmentsBinding

class InstallmentsFragment : Fragment() {

    private var _binding: FragmentInstallmentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LoanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstallmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getInstance(requireContext())

        adapter = LoanAdapter { loan ->
            val intent = Intent(requireContext(), LoanDetailActivity::class.java)
            intent.putExtra("loanId", loan.id)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        db.loanDao().getAll().observe(viewLifecycleOwner) { loans ->
            adapter.submitList(loans)
            binding.emptyView.visibility = if (loans.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddLoanActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
