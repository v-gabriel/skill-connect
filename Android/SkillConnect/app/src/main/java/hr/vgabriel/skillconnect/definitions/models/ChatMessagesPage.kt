package hr.vgabriel.skillconnect.definitions.models

import com.azure.android.communication.chat.models.ChatMessage

data class ChatMessagesPage (
    var continuationToken: String? = null,
    var chatMessages: MutableList<ChatMessage> = mutableListOf()
)