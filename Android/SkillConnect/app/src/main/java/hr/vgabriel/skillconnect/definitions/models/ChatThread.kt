package hr.vgabriel.skillconnect.definitions.models

import com.google.firebase.firestore.DocumentSnapshot
import hr.vgabriel.skillconnect.definitions.entities.ChatThreadEntity

class ChatThread(
    private val _documentSnapshot: DocumentSnapshot,
    private val _id: String = _documentSnapshot.id,
) : ChatThreadEntity(
    chatThreadId = _documentSnapshot.getString(ChatThreadEntity::chatThreadId.name) ?: "",
    title = _documentSnapshot.getString(ChatThreadEntity::title.name) ?: "",
) {

}