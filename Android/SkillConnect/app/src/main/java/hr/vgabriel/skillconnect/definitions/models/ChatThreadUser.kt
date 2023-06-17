package hr.vgabriel.skillconnect.definitions.models

import com.google.firebase.firestore.DocumentSnapshot
import hr.vgabriel.skillconnect.definitions.entities.ChatThreadUserEntity

class ChatThreadUser(
    private val _documentSnapshot: DocumentSnapshot,
    private val _id: String = _documentSnapshot.id,
) : ChatThreadUserEntity(
    chatThreadId = _documentSnapshot.getString(ChatThreadUserEntity::chatThreadId.name) ?: "",
    userId = _documentSnapshot.getString(ChatThreadUserEntity::userId.name) ?: "",
) {

}