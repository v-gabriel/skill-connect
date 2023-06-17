package hr.vgabriel.skillconnect.bll.services

import androidx.compose.runtime.staticCompositionLocalOf
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.repositories.FirebaseRepository
import hr.vgabriel.skillconnect.definitions.entities.ChatThreadUserEntity
import hr.vgabriel.skillconnect.definitions.entities.UserEntity
import hr.vgabriel.skillconnect.definitions.models.ChatThread
import hr.vgabriel.skillconnect.definitions.models.CommunicationUser
import hr.vgabriel.skillconnect.definitions.models.User
import hr.vgabriel.skillconnect.definitions.vm.UserRegisterVM
import hr.vgabriel.skillconnect.definitions.vm.UserVM
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface IUserService {
    suspend fun getUserData(userId: String): UserEntity?
    suspend fun saveNewUserData(
        userId: String, registerModel: UserRegisterVM, communicationUser: CommunicationUser
    ): Boolean

    suspend fun getUsersByIds(documentIds: List<String>): List<UserEntity>
    suspend fun searchUsers(
        query: String, pageSize: Int = 10, lastSnapshot: DocumentSnapshot? = null
    ): List<UserEntity>

    suspend fun getUsersByCommunicationIds(communicationUserIds: List<String>): List<User>
    suspend fun getUserChatThreads(userId: String): List<ChatThread>
    suspend fun updateCommunicationToken(
        userId: String, communicationAccessToken: String
    ): Boolean
}

val LocalUserService = staticCompositionLocalOf<UserService> { error("No UserService provided") }


class UserService(
    private val repository: FirebaseRepository,
    private val loggingService: LoggingService,
    private val userProvider: UserProvider,
) : IUserService {

    override suspend fun getUserData(userId: String): User? = suspendCoroutine { continuation ->
        try {
            val docRef = repository.dataRepository.collection(repository.USER_REPOSITORY_KEY)
                .document(userId)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = User(document)
                    continuation.resume(userData)
                } else {
                    continuation.resume(null)
                }
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        } catch (e: Exception) {
            loggingService.log(
                LogType.ERROR, "[UserService] getUserData", e
            );
        }
    }

    suspend fun updateUserData(userId: String, userModel: UserVM): Boolean? =
        suspendCoroutine { continuation ->
            try {
                val changes = UserEntity.createUserVMMap(userModel);

                val userRepository =
                    repository.dataRepository.collection(repository.USER_REPOSITORY_KEY);
                userRepository.document(userId)
                    .update(changes).addOnCompleteListener { saveTask ->
                        if (saveTask.isSuccessful) {
                            userRepository.document(userId).get().addOnSuccessListener {
                                val user = User(it);
                                userRepository.document(userId)
                                    .update(UserEntity::query.name, user.query)
                                userProvider.setUser(user)
                                continuation.resume(true)
                            }
                        } else {
                            val exception = saveTask.exception
                            if (exception != null) {
                                throw exception
                            } else {
                                continuation.resume(false)
                            }
                        }
                    }
            } catch (e: Exception) {
                loggingService.log(
                    LogType.ERROR, "[UserService] updateUserData", e
                );
                continuation.resumeWithException(e)
            }
        }

    override suspend fun saveNewUserData(
        userId: String, registerModel: UserRegisterVM, communicationUser: CommunicationUser
    ): Boolean = suspendCoroutine { continuation ->
        try {
            val user = UserEntity(
                name = registerModel.nameState.value,
                surname = registerModel.surnameState.value,
                email = registerModel.emailState.value,
                communicationUserId = communicationUser.id,
                communicationAccessToken = communicationUser.accessToken,
                tags = listOf(),
                query = listOf()
            )

            repository.dataRepository.collection(repository.USER_REPOSITORY_KEY).document(userId)
                .set(user).addOnCompleteListener() { saveTask ->
                    if (saveTask.isSuccessful) {
                        continuation.resume(true)
                    } else {
                        val exception = saveTask.exception
                        if (exception != null) {
                            continuation.resumeWithException(exception)
                        } else {
                            continuation.resume(false)
                        }
                    }
                }
        } catch (e: Exception) {
            loggingService.log(
                LogType.ERROR, "[UserService] saveUserData", e
            );
        }
    }

    override suspend fun getUserChatThreads(userId: String): List<ChatThread> = coroutineScope {
        val chatThreads = mutableListOf<ChatThread>()
        try {
            val collectionRef: CollectionReference =
                repository.dataRepository.collection(repository.CHAT_THREAD_USERS_REPOSITORY_KEY)

            val querySnapshot =
                collectionRef.whereEqualTo(ChatThreadUserEntity::userId.name, userId).get().await()
            for (document in querySnapshot.documents) {
                var chatThread = ChatThread(document)
                chatThreads.add(chatThread)
            }
            return@coroutineScope chatThreads
        } catch (e: Exception) {
            loggingService.log(
                LogType.ERROR, "[UserService] getUserChatThreads", e
            );
        }
        return@coroutineScope chatThreads
    }

    override suspend fun getUsersByCommunicationIds(communicationUserIds: List<String>): List<User> =
        coroutineScope {
            val users = mutableListOf<User>();
            try {
                val collectionRef: CollectionReference =
                    repository.dataRepository.collection(repository.USER_REPOSITORY_KEY)

                val querySnapshot = collectionRef.whereIn(
                    UserEntity::communicationUserId.name, communicationUserIds
                ).get().await()
                for (document in querySnapshot.documents) {
                    var user = User(document);
                    users.add(User(document))
                }
            } catch (e: Exception) {
                loggingService.log(LogType.ERROR, "[UserService] getUsersById", e)
            }
            return@coroutineScope users
        }

    override suspend fun getUsersByIds(documentIds: List<String>): List<User> =
        suspendCoroutine { continuation ->
            try {
                val collectionRef: CollectionReference =
                    repository.dataRepository.collection(repository.USER_REPOSITORY_KEY)
                val snapshotTasks = documentIds.map { id ->
                    collectionRef.document(id).get()
                }
                val users = mutableListOf<User>()

                snapshotTasks.forEach { task ->
                    task.addOnSuccessListener { snapshot ->
                        var user = User(snapshot);
                        users.add(user)
                        if (users.size == snapshotTasks.size) {
                            continuation.resume(users)
                        }
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
                }
            } catch (e: Exception) {
                loggingService.log(LogType.ERROR, "[UserService] getUsersById", e)
                continuation.resumeWithException(e)
            }
        }

    override suspend fun updateCommunicationToken(
        userId: String, communicationAccessToken: String
    ): Boolean {
        try {
            var collectionRef: CollectionReference =
                repository.dataRepository.collection(repository.USER_REPOSITORY_KEY)

            collectionRef.document(userId)
                .update(UserEntity::communicationAccessToken.name, communicationAccessToken)
                .await();

            return true
        } catch (e: Exception) {

        }
        return false
    }

    override suspend fun searchUsers(
        queryString: String, pageSize: Int, lastSnapshot: DocumentSnapshot?
    ): List<User> = coroutineScope {
        try {
            var queryStrings = UserEntity.getQueryStrings(queryString)

            val users = mutableListOf<User>()

            val query1 = repository.dataRepository
                .collection(repository.USER_REPOSITORY_KEY)
                .whereArrayContainsAny(User::query.name, queryStrings)
                .limit(pageSize.toLong())
                .get()

            val query2 = repository.dataRepository
                .collection(repository.USER_REPOSITORY_KEY)
                .whereArrayContainsAny(User::tags.name, queryStrings)
                .limit(pageSize.toLong())
                .get()

            val snapshot1 = query1.await().documents
            val snapshot2 = query2.await().documents

            val uniqueDocuments = HashSet<DocumentSnapshot>()
            uniqueDocuments.addAll(snapshot1)
            uniqueDocuments.addAll(snapshot2)
            for (document in uniqueDocuments) {
                users.add(User(document))
            }
            users.removeIf { it.id == userProvider.user!!.id }

            return@coroutineScope users

        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[UserService] searchUsers", e);
            return@coroutineScope emptyList<User>()
        }
    }
}