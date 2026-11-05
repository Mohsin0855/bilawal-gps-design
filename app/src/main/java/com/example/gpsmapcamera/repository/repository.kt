package com.example.gpsmapcamera.repository

import com.example.gpsmapcamera.dao.HistoryDao
import com.example.gpsmapcamera.ui.HistoryModel
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

    val allHistory: Flow<List<HistoryModel>> = historyDao.getAllHistory()

    suspend fun insert(history: HistoryModel) {
        historyDao.insertHistory(history)
    }
    suspend fun delete(history: HistoryModel) {
        historyDao.deleteHistory(history)
    }

    suspend fun deleteAll() {
        historyDao.deleteAllHistory()
    }
}