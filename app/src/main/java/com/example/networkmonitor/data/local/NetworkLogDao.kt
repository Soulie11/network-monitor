package com.example.networkmonitor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkLogDao {

    @Query("SELECT * FROM connection_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ConnectionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ConnectionLogEntity)

    @Query("DELETE FROM connection_logs")
    suspend fun clearAll()
}
