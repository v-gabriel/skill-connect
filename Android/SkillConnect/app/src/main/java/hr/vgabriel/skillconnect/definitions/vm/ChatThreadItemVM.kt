package hr.vgabriel.skillconnect.definitions.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azure.android.communication.chat.ChatThreadAsyncClient
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
import hr.vgabriel.skillconnect.definitions.models.User
import hr.vgabriel.skillconnect.definitions.models.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatThreadItemVMFactory(
    private val azureChatService: AzureChatService,
    private val userService: UserService,
    private val loggingService: LoggingService,
    private val chatEventService: ChatEventService,
    private val userProvider: UserProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatThreadItemVM::class.java)) {
            return ChatThreadItemVM(
                azureChatService, userService, loggingService, chatEventService, userProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ChatThreadItemVM(
    private val azureChatService: AzureChatService,
    private val userService: UserService,
    private val loggingService: LoggingService,
    private val chatEventService: ChatEventService,
    private val userProvider: UserProvider
) : ViewModel() {

    private var pageSize = 10

    private var _chatThreadId = MutableStateFlow("")
    var chatThreadId = _chatThreadId.asStateFlow()

    private var _lastChatMessage: MutableStateFlow<UserMessage?> = MutableStateFlow(null)
    var lastChatMessage = _lastChatMessage.asStateFlow()

    private var _participants = MutableStateFlow<List<User>>(mutableListOf())
    var participants = _participants.asStateFlow()

    //@OptIn(InternalCoroutinesApi::class)
    suspend fun initChatThreadVM(threadId: String): Boolean = coroutineScope {
        try {
            val id = "${this@ChatThreadItemVM::class.simpleName}${threadId}"

            if (threadId == _chatThreadId.value) {
                updateLastMessage()

                return@coroutineScope true
            }

            chatEventService.unsubscribeFromEvents(id)

            val chatThreadAsyncClient =
                azureChatService.getChatThreadClient(threadId);
            if (chatThreadAsyncClient != null) {
                _chatThreadId.value = threadId

                val participants = mutableListOf<ChatParticipant>()
                val participantsPagedAsyncStream = chatThreadAsyncClient!!.listParticipants(
                    ListParticipantsOptions().setMaxPageSize(pageSize), RequestContext.NONE
                )

                val result = suspendCoroutine<Boolean> { continuation ->
//                   kotlinx.coroutines.internal.synchronized(this@ChatThreadItemVM) {
                        participantsPagedAsyncStream.byPage()
                            .forEach { page: PagedResponse<ChatParticipant> ->
                                participants.addAll(page.elements);
                                if (page.continuationToken == null || page.elements.isEmpty()) {
                                    continuation.resume(true)
                                }
                            }
//                    }
                }
                if (result && participants.isNotEmpty()) {
                    var users =
                        userService.getUsersByCommunicationIds(participants.map { it.communicationIdentifier.rawId });
                    _participants.value = users

                    updateLastMessage()

                    chatEventService.subscribeToEvents(
                        ChatEventHandler(
                            id = id,
                            chatEventType = ChatEventType.CHAT_MESSAGE_RECEIVED
                        ) { event ->
                            nextMessageHandler(event)
                        })

                    return@coroutineScope true

                } else {
                    throw Exception("Participants enumeration failed")
                }
            } else {
                throw Exception("Chat thread client is null")
            }
        } catch (e: Exception) {
            loggingService.log(
                LogType.ERROR, "[ChatThreadItemVM] initChatThreadVM for ${chatThreadId.value}", e
            )
        }
        return@coroutineScope false
    }

    private fun nextMessageHandler(event: ChatEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val chatMessageReceivedEvent = event as ChatMessageReceivedEvent
                var threadId = chatMessageReceivedEvent.chatThreadId;

                if (threadId == _chatThreadId.value) {
                    updateLastMessage()
                }
            } catch (e: Exception) {
                loggingService.log(
                    LogType.ERROR,
                    "[ChatThreadItemVM] nextMessageHandler for ${chatThreadId.value}",
                    e
                )
                return@launch
            }
        }
    }

    private suspend fun updateLastMessage() {
        try {
            val newChatMessage = azureChatService.getChatMessagesPage(
                threadId = _chatThreadId.value, pageSize = 1
            ).chatMessages[0]
            if(newChatMessage.content.message == null){
                return
            }

            val messages = mutableListOf(newChatMessage);
            var newUserMessage = UserMessage.mapMessagesToUsers(
                messages, _participants.value
            )[0]

            _lastChatMessage.value = newUserMessage
        } catch (e: Exception) {
            loggingService.log(
                LogType.ERROR, "[ChatThreadItemVM] updateLastMessage for ${chatThreadId.value}", e
            )
        }
    }
}