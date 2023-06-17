package hr.vgabriel.skillconnect.definitions.vm

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azure.android.communication.chat.models.ChatEvent
import com.azure.android.communication.chat.models.ChatEventType
import com.azure.android.communication.chat.models.ChatThreadCreatedEvent
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.services.AzureChatService
import hr.vgabriel.skillconnect.bll.services.ChatEventHandler
import hr.vgabriel.skillconnect.bll.services.ChatEventService
import hr.vgabriel.skillconnect.bll.services.LogType
import hr.vgabriel.skillconnect.bll.services.LoggingService
import hr.vgabriel.skillconnect.bll.services.NotificationService
import hr.vgabriel.skillconnect.bll.services.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatThreadsVMFactory(
    private val azureChatService: AzureChatService,
    private val userService: UserService,
    private val loggingService: LoggingService,
    private val chatEventService: ChatEventService,
    private val userProvider: UserProvider,
    private val notificationService: NotificationService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatThreadsVM::class.java)) {
            return ChatThreadsVM(
                azureChatService,
                userService,
                loggingService,
                chatEventService,
                userProvider,
                notificationService
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val LocalChatThreadsVM = staticCompositionLocalOf<ChatThreadsVM> {
    error("No ChatThreadsVM provided")
}

class ChatThreadsVM(
    private val azureChatService: AzureChatService,
    private val userService: UserService,
    private val loggingService: LoggingService,
    private val chatEventService: ChatEventService,
    private val userProvider: UserProvider,
    private val notificationService: NotificationService
) : ViewModel() {

    private var _chatThreadIds: MutableStateFlow<MutableList<String>> =
        MutableStateFlow(mutableListOf())
    val chatThreadIds: StateFlow<MutableList<String>> = _chatThreadIds.asStateFlow();

    private val _chatThreadItemVMs = MutableStateFlow(mutableListOf<ChatThreadItemVM>())
    val chatThreadItemVMs = _chatThreadItemVMs.asStateFlow();

    var chatThreadsRefresh = MutableStateFlow(false)

    private fun threadCreatedEventHandler(payload: ChatEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val chatMessageReceivedEvent = payload as ChatThreadCreatedEvent
                var threadId = chatMessageReceivedEvent.chatThreadId;
                notificationService.displayMessageNotification(
                    title = "New chat created",
                    content = "Someone is trying to contact you!"
                )
                initChatThreadItemVM(threadId)
            } catch (e: Exception) {
                loggingService.log(LogType.ERROR, "[ChatThreadsVM] threadCreatedEventHandler.", e)
                return@launch
            }
        }
    }

    suspend fun initChatThreadItemVM(threadId: String): Boolean = coroutineScope {
        try {
            val chatThreadItemVM = ChatThreadItemVM(
                azureChatService,
                userService,
                loggingService,
                chatEventService,
                userProvider
            )

            _chatThreadIds.value.add(threadId)
            var result = chatThreadItemVM.initChatThreadVM(threadId);
            if (!result) {
                throw Exception("ChatThreadVM init error")
            }
            _chatThreadItemVMs.value.add(chatThreadItemVM)
            chatThreadsRefresh.value = !chatThreadsRefresh.value

            return@coroutineScope true
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[ChatThreadsVM] initChatThread for $threadId", e)
            return@coroutineScope false
        }
    }

    private fun clearData() {
        _chatThreadIds.value.clear()
        _chatThreadItemVMs.value.clear()
    }

    fun initChatEventServiceHandlers() {
        val id = "${this@ChatThreadsVM::class.simpleName}"

        chatEventService.subscribeToEvents(
            ChatEventHandler(
                id = id,
                ChatEventType.CHAT_THREAD_CREATED
            ) {
                threadCreatedEventHandler(it)
            })
    }

    suspend fun initChatThreadItemVMs(): Boolean = coroutineScope {
        try {
            clearData()

            val userThreads = userService.getUserChatThreads(userProvider.user!!.id)
            val chatThreadIds = userThreads.map { it.chatThreadId };

            _chatThreadIds.value.clear()
            _chatThreadIds.value.addAll(chatThreadIds);

            for (chatThreadId in chatThreadIds) {
                initChatThreadItemVM(chatThreadId)
            }

            return@coroutineScope true
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[ChatThreadsVM] initChatThreads", e)
            return@coroutineScope false
        }
    }
}