package com.reminder.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY startDate DESC")
    fun getAll(): LiveData<List<Loan>>

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getById(id: Long): Loan?

    @Insert
    suspend fun insert(loan: Loan): Long

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)
}
