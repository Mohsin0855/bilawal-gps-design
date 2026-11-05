package com.example.gpsmapcamera.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpsmapcamera.repository.HistoryRepository
import com.example.gpsmapcamera.ui.AppDatabase
import com.example.gpsmapcamera.ui.HistoryModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class history_viewmodel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository

    val allHistory: StateFlow<List<HistoryModel>>

    init {
        val historyDao = AppDatabase.getDatabase(application).historyDao()
        repository = HistoryRepository(historyDao)

        allHistory = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insert(history: HistoryModel) = viewModelScope.launch {
        repository.insert(history)
    }
    fun delete(history: HistoryModel) = viewModelScope.launch {
        repository.delete(history)
    }
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}