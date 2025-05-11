package com.example.ultai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val isUser: Boolean,
    val timestamp: Date = Date()
) 