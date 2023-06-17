package hr.vgabriel.skillconnect.definitions.vm

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azure.android.communication.chat.models.ChatEvent
import com.azure.android.communication.chat.models.ChatEventType
import com.azure.android.communication.chat.models.ChatMessageReceivedEvent
import com.azure.android.communication.chat.models.ChatParticipant
import com.azure.android.communication.chat.models.ListParticipantsOptions
import com.azure.android.core.rest.util.paging.PagedResponse
import com.azure.android.core.util.RequestContext
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.services.AzureChatService
import hr.vgabriel.skillconnect.bll.services.ChatEventHandler
import hr.vgabriel.skillconnect.bll.services.ChatEventService
import hr.vgabriel.skillconnect.bll.services.LogType
import hr.vgabriel.skillconnect.bll.services.LoggingService
import hr.vgabriel.skillconnect.bll.services.UserService
import hr.vgabriel.skillconnect.definitions.models.ChatMessagesPage
import hr.vgabriel.skillconnect.definitions.models.User
import hr.vgabriel.skillconnect.definitions.models.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


val LocalChatThreadVM = staticCompositionLocalOf<ChatThreadVM> {
    error("No ChatThreadVM provided")
}

class ChatThreadVMFactory(
    private val azureChatService: AzureChatService,
    private val userService: UserService,
    private val loggingService: LoggingService,
    private val chatEventService: ChatEventService,
    private val userProvider: UserProvider,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatThreadVM::class.java)) {
            return ChatThreadVM(
                azureChatService,
                userService,
                loggingService,
                chatEventService,
                userProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ChatThreadVM(
    private val azureChatService: AzureChatService,
    private val userService: UserService,
    private val loggingService: LoggingService,
    private val chatEventService: ChatEventService,
    private val userProvider: UserProvider
) : ViewModel() {

    private val pageSize = 10

    private var _chatThreadId = MutableStateFlow("")
    var chatThreadId = _chatThreadId.asStateFlow()

    private var _chatMessagesPage = MutableStateFlow(ChatMessagesPage())
    private var _participants = MutableStateFlow<List<User>>(mutableListOf())
    var participants = _participants.asStateFlow()

    private val _userMessages: MutableStateFlow<MutableList<UserMessage>> = MutableStateFlow(
        mutableListOf()
    )
    val userMessages: StateFlow<List<UserMessage>> = _userMessages.asStateFlow()

    private fun nextMessageHandler(event: ChatEvent) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val chatMessageReceivedEvent = event as ChatMessageReceivedEvent
                val threadId = chatMessageReceivedEvent.chatThreadId
                if (threadId == _chatThreadId.value) {
                    fetchNextMessage()
                }
            } catch (e: Exception) {
                loggingService.log(
                    LogType.ERROR,
                    "[ChatThreadVM] eventHandler for ${chatThreadId}.",
                    e
                )
                return@launch
            }
        }
    }


    private fun clearData() {
        _participants.value = emptyList()
        _chatMessagesPage.value.chatMessages.clear()
        _chatMessagesPage.value.continuationToken = null
        updateChatMessages()
    }

    //@OptIn(InternalCoroutinesApi::class)
    suspend fun initChatThreadVM(chatThreadId: String): Boolean =
        coroutineScope {
            try {
                if (chatThreadId == _chatThreadId.value) {
                    return@coroutineScope true
                }

                clearData()
                val id = "${this@ChatThreadVM::class.simpleName}" //${chatThreadId}"
                chatEventService.unsubscribeFromEvents(id)

                val chatThreadAsyncClient =
                    azureChatService.getChatThreadClient(chatThreadId)
                if (chatThreadAsyncClient != null) {
                    _chatThreadId.value = chatThreadId

                    val participants = mutableListOf<ChatParticipant>()
                    val participantsPagedAsyncStream = chatThreadAsyncClient.listParticipants(
                        ListParticipantsOptions().setMaxPageSize(pageSize),
                        RequestContext.NONE
                    )

                    val result = suspendCoroutine { continuation ->
                        //kotlinx.coroutines.internal.synchronized(this@ChatThreadVM) {
                        participantsPagedAsyncStream.byPage()
                            .forEach { page: PagedResponse<ChatParticipant> ->
                                participants.addAll(page.elements)
                                if (page.continuationToken == null || page.elements.isEmpty()) {
                                    continuation.resume(true)
                                }
                            }
                        //}
                    }
                    if (result && participants.isNotEmpty()) {
                        val participantCommunicationIds =
                            participants.map { it.communicationIdentifier.rawId }
                        val users =
                            userService.getUsersByCommunicationIds(participantCommunicationIds)
                        _participants.value = users

                        fetchMoreMessages()

                        chatEventService.subscribeToEvents(
                            ChatEventHandler(
                                id = id,
                                chatEventType = ChatEventType.CHAT_MESSAGE_RECEIVED
                            ) {
                                nextMessageHandler(it)
                            })

                        return@coroutineScope true

                    } else {
                        throw Exception("Participants enumeration failed")
                    }
                } else {
                    throw Exception("Chat thread client is null")
                }
            } catch (e: Exception) {
                loggingService.log(LogType.ERROR, "[ChatThreadVM] initChatThreadVM", e)
            }
            return@coroutineScope false
        }

    private suspend fun fetchNextMessage() {
        try {
            val newChatMessage = azureChatService.getChatMessagesPage(
                threadId = _chatThreadId.value,
                pageSize = 1
            ).chatMessages[0]

            _chatMessagesPage.value.chatMessages.add(0, newChatMessage)
            updateChatMessages()
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[ChatThreadVM] fetchNextMessage", e)
        }
    }

    suspend fun fetchMoreMessages() {
        try {
            val newChatMessagesPage = azureChatService.getChatMessagesPage(
                threadId = _chatThreadId.value,
                chatMessagesPage = _chatMessagesPage.value
            )
            newChatMessagesPage.chatMessages.addAll(0, _chatMessagesPage.value.chatMessages)
            _chatMessagesPage.value = newChatMessagesPage

            updateChatMessages()
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[ChatThreadVM] fetchMoreMessages", e)
        }
    }

    private fun updateChatMessages() {
        try {
            val newUserMessages: MutableList<UserMessage> = UserMessage.mapMessagesToUsers(
                _chatMessagesPage.value.chatMessages.toList(),
                _participants.value.toList()
            )

            _userMessages.value = newUserMessages
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[ChatThreadVM] updateChatMessages", e)
        }
    }
}