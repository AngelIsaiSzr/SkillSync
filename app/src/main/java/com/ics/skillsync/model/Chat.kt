package com.ics.skillsync.model

import java.util.Date

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(), // IDs de los usuarios
    val lastMessage: String = "",
    val lastMessageTimestamp: Date = Date(),
    val unreadCount: Int = 0,
    val createdAt: Date = Date()
) 