package hr.vgabriel.skillconnect.definitions.entities

enum class ChatThreadTopic {
    Chat
}

open class ChatThreadEntity(
    val chatThreadId: String,
    val title: String?
)
