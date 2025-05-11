package com.ics.skillsync.model

import java.util.Date

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false
) 