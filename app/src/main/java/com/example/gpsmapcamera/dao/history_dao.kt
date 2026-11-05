package com.example.gpsmapcamera.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.gpsmapcamera.ui.HistoryModel
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertHistory(history: HistoryModel)

    @Query("SELECT * FROM chat_history_table ORDER BY id DESC")
    fun getAllHistory(): Flow<List<HistoryModel>>

    @Delete
    suspend fun deleteHistory(history: HistoryModel)

    @Query("DELETE FROM chat_history_table")
    suspend fun deleteAllHistory()
}