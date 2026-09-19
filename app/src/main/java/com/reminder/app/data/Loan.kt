package com.reminder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lenderName: String? = null,
    val totalAmount: Double,
    val installmentCount: Int,
    val installmentAmount: Double,
    val startDate: Long,
    val interestRate: Double? = null,
    val note: String? = null
)
