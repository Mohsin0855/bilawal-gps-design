package com.example.gpsmapcamera.ui

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history_table")
data class HistoryModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phoneNumber: String,
    val message: String
)