package com.reminder.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "installments",
    foreignKeys = [ForeignKey(
        entity = Loan::class,
        parentColumns = ["id"],
        childColumns = ["loanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("loanId")]
)
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanId: Long,
    val installmentNumber: Int,
    val dueDate: Long,
    val amount: Double,
    val isPaid: Boolean = false,
    val paidDate: Long? = null
)
