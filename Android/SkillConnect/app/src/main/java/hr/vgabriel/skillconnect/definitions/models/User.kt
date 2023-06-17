package hr.vgabriel.skillconnect.definitions.models

import com.google.firebase.firestore.DocumentSnapshot
import hr.vgabriel.skillconnect.definitions.entities.UserEntity
import hr.vgabriel.skillconnect.helpers.removeSpaces
import hr.vgabriel.skillconnect.helpers.splitStringBySpaces

class User(
    private val _documentSnapshot: DocumentSnapshot,
    private val _id: String = _documentSnapshot.id,
) : UserEntity(
    name = _documentSnapshot.getString(UserEntity::name.name) ?: "",
    surname = _documentSnapshot.getString(UserEntity::surname.name) ?: "",
    email = _documentSnapshot.getString(UserEntity::email.name) ?: "",
    communicationUserId = _documentSnapshot.getString(UserEntity::communicationUserId.name) ?: "",
    communicationAccessToken = _documentSnapshot.getString(UserEntity::communicationAccessToken.name)
        ?: "",
    tags = _documentSnapshot.get(UserEntity::tags.name) as? List<String> ?: emptyList(),
    query = emptyList<String>()
) {
    val documentSnapshot: DocumentSnapshot
        get() = _documentSnapshot

    val id: String
        get() = _id

    val fullName: String
        get() = "$name $surname";

    val initials: String
        get() = name.take(1) + surname.take(1)
}

