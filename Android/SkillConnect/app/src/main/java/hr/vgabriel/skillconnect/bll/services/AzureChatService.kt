package hr.vgabriel.skillconnect.bll.services

import androidx.compose.runtime.staticCompositionLocalOf
import com.azure.android.communication.chat.ChatAsyncClient
import com.azure.android.communication.chat.ChatClientBuilder
import com.azure.android.communication.chat.ChatThreadAsyncClient
import com.azure.android.communication.chat.models.ChatEvent
import com.azure.android.communication.chat.models.ChatEventType
import com.azure.android.communication.chat.models.ChatMessage
import com.azure.android.communication.chat.models.ChatMessageReceivedEvent
import com.azure.android.communication.chat.models.ChatMessageType
import com.azure.android.communication.chat.models.ChatParticipant
import com.azure.android.communication.chat.models.ChatThreadProperties
import com.azure.android.communication.chat.models.CreateChatThreadOptions
import com.azure.android.communication.chat.models.CreateChatThreadResult
import com.azure.android.communication.chat.models.ListChatMessagesOptions
import com.azure.android.communication.chat.models.SendChatMessageOptions
import com.azure.android.communication.common.CommunicationTokenCredential
import com.azure.android.communication.common.CommunicationUserIdentifier
import com.azure.android.core.rest.util.paging.PagedResponse
import com.azure.android.core.util.RequestContext
import hr.vgabriel.skillconnect.BuildConfig
import hr.vgabriel.skillconnect.MainActivity
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.repositories.FirebaseRepository
import hr.vgabriel.skillconnect.definitions.entities.ChatThreadEntity
import hr.vgabriel.skillconnect.definitions.entities.ChatThreadTopic
import hr.vgabriel.skillconnect.definitions.entities.ChatThreadUserEntity
import hr.vgabriel.skillconnect.definitions.models.ChatMessagesPage
import hr.vgabriel.skillconnect.definitions.models.CommunicationUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


interface IAzureChatService {
    suspend fun createChatThread(
        topic: ChatThreadTopic = ChatThreadTopic.Chat,
        communicationUserIds: List<String>
    ): String?

    fun sendMessage(threadId: String, message: String): Boolean
    suspend fun getChatMessagesPage(
        threadId: String,
        pageSize: Int = 10,
        chatMessagesPage: ChatMessagesPage = ChatMessagesPage()
    ): ChatMessagesPage

    suspend fun initChatClient(userAccessToken: String): Boolean
}

val LocalAzureChatService =
    staticCompositionLocalOf<AzureChatService> { error("No AzureChatService provided") }

class AzureChatService(
    private val loggingService: LoggingService,
    private val repository: FirebaseRepository,
    private val userService: UserService,
    private val userProvider: UserProvider,
    private val chatEventService: ChatEventService,
    private val navService: NavService,
    private val notificationService: NotificationService
) : IAzureChatService {

    val ENDPOINT_URL = BuildConfig.AZURE_COMMUNICATION_ENDPOINT
    val CREATE_COMMUNICATION_USER_URL = BuildConfig.AZURE_COMMUNICATION_CREATE_COMMUNICATION_USER_URL
    val REFRESH_TOKEN_URL = BuildConfig.AZURE_COMMUNICATION_REFRESH_TOKEN_URL

    private var chatAsyncClient: ChatAsyncClient? = null;

    fun getChatThreadClient(threadId: String): ChatThreadAsyncClient? {
        return chatAsyncClient!!.getChatThreadClient(threadId)
    }

    private fun messageReceivedEventHandler(payload: ChatEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val chatMessageReceivedEvent = payload as ChatMessageReceivedEvent
                var threadId = chatMessageReceivedEvent.chatThreadId;
            } catch (e: Exception) {
                loggingService.log(
                    LogType.ERROR,
                    "[AzureChatService] messageReceivedEventHandler.",
                    e
                )
                return@launch
            }
        }
    }

    override suspend fun initChatClient(userAccessToken: String): Boolean = coroutineScope {
        try {
            if (chatAsyncClient != null) {
                throw Exception("Client already initialized")
            }
            chatAsyncClient = ChatClientBuilder().endpoint(ENDPOINT_URL)
                .credential(CommunicationTokenCredential(userAccessToken))
                .buildAsyncClient();
            if (chatAsyncClient != null) {
                chatAsyncClient!!.startRealtimeNotifications(MainActivity.appContext) { throwable ->
                    throw throwable
                }
                chatEventService.bindToChatClient(chatAsyncClient!!)
                chatEventService.subscribeToEvents(
                    ChatEventHandler(
                        id = userAccessToken,
                        ChatEventType.CHAT_MESSAGE_RECEIVED
                    ) {
                        messageReceivedEventHandler(it)
                    })

                return@coroutineScope true
            } else {
                throw Exception("Chat client init error.")
            }
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[AzureChatService] initChatClient", e)
        }
        return@coroutineScope false
    }

    override fun sendMessage(threadId: String, message: String): Boolean {
        try {
            val chatThreadClient = chatAsyncClient!!.getChatThreadClient(threadId)
            val chatMessageOptions =
                SendChatMessageOptions().setType(ChatMessageType.TEXT).setContent(message)
            val chatMessageId = chatThreadClient.sendMessage(chatMessageOptions).get().id

            return true
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[AzureChatService] sendMessage", e)
        }
        return false
    }

    override suspend fun getChatMessagesPage(
        threadId: String, pageSize: Int, chatMessagesPage: ChatMessagesPage
    ): ChatMessagesPage = coroutineScope {
        try {
            val chatThreadClient = chatAsyncClient!!.getChatThreadClient(threadId)
            val listMessagesOptions = ListChatMessagesOptions().setMaxPageSize(pageSize)
            val stream = chatThreadClient.listMessages(listMessagesOptions, RequestContext.NONE)

            val nextChatMessagesPage = ChatMessagesPage()
            val result = suspendCoroutine { continuation ->
                // fetch next page
                if (chatMessagesPage.continuationToken != null) {
                    stream.getPage(chatMessagesPage.continuationToken)
                    { pagedResponse: PagedResponse<ChatMessage>, throwable: Throwable? ->
                        if (throwable != null) {
                            continuation.resume(false)
                            return@getPage
                        } else {
                            nextChatMessagesPage.chatMessages.addAll(pagedResponse.elements)
                            nextChatMessagesPage.continuationToken = pagedResponse.continuationToken
                            continuation.resume(true)
                            return@getPage
                        }
                    };
                }
                // first fetch
                else if (chatMessagesPage.chatMessages.isEmpty()) {
                    stream.byPage().forEach { it ->
                        nextChatMessagesPage.chatMessages.addAll(it.elements)
                        nextChatMessagesPage.continuationToken = it.continuationToken
                        continuation.resume(true)
                    }
                }
                // no more messages
                else {
                    continuation.resume(true)
                }
            }
            if (!result) {
                throw Exception("Pageable fetch error")
            }

            return@coroutineScope nextChatMessagesPage
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[AzureChatService] getMessagesFromChatThread", e)
            throw e
        }
    }


    override suspend fun createChatThread(
        topic: ChatThreadTopic, userIds: List<String>
    ): String? = coroutineScope {
        try {
            val participants: MutableList<ChatParticipant> = ArrayList()
            val communicationUserIds =
                userService.getUsersByIds(userIds).map { it -> it.communicationUserId };
            communicationUserIds.forEach { id ->
                participants.add(
                    ChatParticipant().setCommunicationIdentifier(
                        CommunicationUserIdentifier(
                            id
                        )
                    )
                )
            }

            val createChatThreadOptions =
                CreateChatThreadOptions().setTopic(topic.name).setParticipants(participants)

            val createChatThreadResult: CreateChatThreadResult? =
                chatAsyncClient?.createChatThread(createChatThreadOptions)?.get()
            val chatThreadProperties: ChatThreadProperties? =
                createChatThreadResult?.chatThreadProperties

            val threadId = chatThreadProperties?.id

            if (threadId != null) {
                var chatThread: ChatThreadEntity = ChatThreadEntity(
                    chatThreadId = threadId, title = topic.name
                )

                val chatThreadsRef =
                    repository.dataRepository.collection(repository.CHAT_THREADS_REPOSITORY_KEY)
                val chatThreadUsersRef =
                    repository.dataRepository.collection(repository.CHAT_THREAD_USERS_REPOSITORY_KEY)

                val batch = repository.dataRepository.batch()

                chatThreadsRef.document(chatThread.chatThreadId).set(
                    chatThread
                )

                userIds.forEach { id ->
                    var chatThreadUser: ChatThreadUserEntity = ChatThreadUserEntity(
                        chatThreadId = threadId, userId = id
                    )
                    var docRef = chatThreadUsersRef.document();
                    batch.set(
                        docRef, chatThreadUser
                    )
                }
                batch.commit().await()

                return@coroutineScope threadId
            }
            throw Exception("Thread init error");
        } catch (e: Exception) {
            loggingService.log(
                LogType.ERROR, "[AzureCommunicationService] initChatThread", e
            )
        }
        return@coroutineScope null
    }


    suspend fun refreshAccessToken(communicationUserId: String): CommunicationUser? =
        suspendCoroutine { continuation ->
            try {
                val client = OkHttpClient()

                val request = Request.Builder()
                    .url("$REFRESH_TOKEN_URL&communicationUserId=$communicationUserId")
                    .get()
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        throw e
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()

                            val responseObject = JSONObject(responseBody)

                            val accessToken = responseObject.getString("accessToken")
                            val userId = responseObject.getString("id")

                            val communicationUser = CommunicationUser(userId, accessToken)

                            continuation.resume(communicationUser)
                        } else {
                            continuation.resume(null)
                        }
                    }
                })
            } catch (e: Exception) {
                loggingService.log(
                    LogType.ERROR, "[AzureCommunicationService] refreshAccessToken", e
                )
                continuation.resumeWithException(e)
            }
        }

    suspend fun getCommunicationUser(): CommunicationUser? = suspendCoroutine { continuation ->
        try {
            val client = OkHttpClient()

            val request = Request.Builder().url(CREATE_COMMUNICATION_USER_URL).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {

                        val responseBody = response.body?.string()
                        val responseObject = JSONObject(responseBody)

                        val accessToken = responseObject.getString("accessToken")
                        val userId = responseObject.getString("id")

                        val communicationUser = CommunicationUser(userId, accessToken)

                        continuation.resume(communicationUser)
                    } else {
                        continuation.resume(null)
                    }
                }
            })
        } catch (e: Exception) {
            loggingService.log(
                LogType.ERROR, "[AzureCommunicationService] getCommunicationUser", e
            )
            continuation.resumeWithException(e)
        }
    }
}
