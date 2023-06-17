package hr.vgabriel.skillconnect.bll.services

import androidx.compose.runtime.staticCompositionLocalOf
import com.azure.android.communication.chat.ChatAsyncClient
import com.azure.android.communication.chat.models.ChatEvent
import com.azure.android.communication.chat.models.ChatEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

interface IChatEventService {
    fun bindToChatClient(chatAsyncClient: ChatAsyncClient)
}

val LocalChatEventService = staticCompositionLocalOf<ChatEventService> {
    error("No LocalChatEventService provided")
}

class ChatEventHandler(
    val id: String,
    val chatEventType: ChatEventType,
    val handle: suspend (event: ChatEvent) -> Unit
)

class ChatEventService(
    private val loggingService: LoggingService
) : IChatEventService {
    private val eventHandlers: MutableList<ChatEventHandler> = mutableListOf()
    private var isBound = false

    override fun bindToChatClient(chatAsyncClient: ChatAsyncClient) {
        try {
            if (isBound) {
                throw Exception("Already bound")
            }
            chatAsyncClient.addEventHandler(ChatEventType.CHAT_MESSAGE_RECEIVED) { event ->
                CoroutineScope(Dispatchers.Main).launch {
                    val deferredHandlers =
                        eventHandlers.filter { it.chatEventType == ChatEventType.CHAT_MESSAGE_RECEIVED }
                            .map { handler ->
                                async(Dispatchers.Default) {
                                    run {
                                        handler.handle(event)
                                    }
                                }
                            }
                    deferredHandlers.awaitAll()
                }
            }
            chatAsyncClient.addEventHandler(ChatEventType.CHAT_THREAD_CREATED) { event ->
                CoroutineScope(Dispatchers.Main).launch {
                    val deferredHandlers =
                        eventHandlers.filter { it.chatEventType == ChatEventType.CHAT_THREAD_CREATED }
                            .map { handler ->
                                async(Dispatchers.Default) {
                                    run {
                                        handler.handle(event)
                                    }
                                }
                            }
                    deferredHandlers.awaitAll()
                }
            }
        } catch (e: Exception) {
            loggingService.log(LogType.ERROR, "[ChatEventService] bindToChatClient")
        }
    }

    fun subscribeToEvents(handler: ChatEventHandler) {
        if (eventHandlers.any { it.id == handler.id }) {
            return
        }
        eventHandlers.add(handler)
    }

    fun unsubscribeFromEvents(handlerId: String) {
        eventHandlers.removeAll { it.id == handlerId }
    }
}
