package com.ics.skillsync.model

import java.util.Date

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(), // IDs de los usuarios
    val lastMessage: String = "",
    val lastMessageTimestamp: Date = Date(),
    val unreadCounts: Map<String, Int> = emptyMap(), // userId -> count
    val createdAt: Date = Date()
) 