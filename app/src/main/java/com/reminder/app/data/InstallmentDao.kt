package com.reminder.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM installments WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun getForLoan(loanId: Long): LiveData<List<Installment>>

    @Query("SELECT * FROM installments WHERE id = :id")
    suspend fun getById(id: Long): Installment?

    @Insert
    suspend fun insertAll(installments: List<Installment>)

    @Update
    suspend fun update(installment: Installment)

    @Query("SELECT * FROM installments WHERE isPaid = 0 AND dueDate > :now")
    suspend fun getUpcoming(now: Long): List<Installment>

    @Query("SELECT * FROM installments WHERE loanId = :loanId")
    suspend fun getAllForLoanSync(loanId: Long): List<Installment>
}
