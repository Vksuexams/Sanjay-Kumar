package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MilkEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkEntryDao {
    @Query("SELECT * FROM milk_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<MilkEntryEntity>>

    @Query("SELECT * FROM milk_entries WHERE date = :date")
    fun getEntriesByDate(date: String): Flow<List<MilkEntryEntity>>

    @Query("SELECT * FROM milk_entries WHERE customerId = :customerId ORDER BY date DESC")
    fun getEntriesByCustomer(customerId: Long): Flow<List<MilkEntryEntity>>

    @Query("SELECT * FROM milk_entries WHERE customerId = :customerId AND date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getEntriesForCustomerInMonth(customerId: Long, monthPrefix: String): Flow<List<MilkEntryEntity>>

    @Query("SELECT * FROM milk_entries WHERE customerId = :customerId AND date LIKE :monthPrefix || '%' ORDER BY date ASC")
    suspend fun getEntriesForCustomerInMonthDirect(customerId: Long, monthPrefix: String): List<MilkEntryEntity>

    @Query("SELECT * FROM milk_entries WHERE customerId = :customerId AND date < :datePrefix ORDER BY date ASC")
    suspend fun getEntriesBeforeDate(customerId: Long, datePrefix: String): List<MilkEntryEntity>

    @Query("SELECT * FROM milk_entries WHERE customerId = :customerId AND date = :date LIMIT 1")
    suspend fun getEntryByCustomerAndDate(customerId: Long, date: String): MilkEntryEntity?

    @Query("SELECT * FROM milk_entries WHERE customerId = :customerId AND date = :date LIMIT 1")
    fun getEntryByCustomerAndDateFlow(customerId: Long, date: String): Flow<MilkEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEntry(entry: MilkEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<MilkEntryEntity>)

    @Update
    suspend fun updateEntry(entry: MilkEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: MilkEntryEntity)

    @Query("DELETE FROM milk_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM milk_entries WHERE customerId = :customerId")
    suspend fun deleteEntriesByCustomer(customerId: Long)

    @Query("SELECT * FROM milk_entries")
    suspend fun getAllEntriesDirect(): List<MilkEntryEntity>
}
