package hr.vgabriel.skillconnect.definitions.models

import android.util.Log
import com.azure.android.communication.chat.models.ChatMessage

class UserMessage(
    val message: ChatMessage,
    val user: User
) {
    companion object {
        fun mapMessagesToUsers(
            chatMessages: List<ChatMessage>,
            users: List<User>
        ): MutableList<UserMessage> {
            return chatMessages.mapNotNull { message ->
                val user = users.find {
                    it.communicationUserId == message.senderCommunicationIdentifier?.rawId
                }
                if (user != null) {
                    UserMessage(message, user)
                } else {
                    null
                }
            }.toMutableList()
        }
    }
}