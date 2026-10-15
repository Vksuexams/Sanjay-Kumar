package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BusinessProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<BusinessProfileEntity?>

    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): BusinessProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: BusinessProfileEntity)

    @Update
    suspend fun updateProfile(profile: BusinessProfileEntity)
}
