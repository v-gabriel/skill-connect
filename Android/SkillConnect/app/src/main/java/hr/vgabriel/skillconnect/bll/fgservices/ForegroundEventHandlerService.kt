package hr.vgabriel.skillconnect.bll.fgservices

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.azure.android.communication.chat.models.ChatEvent
import com.azure.android.communication.chat.models.ChatEventType
import com.azure.android.communication.chat.models.ChatMessageReceivedEvent
import hr.vgabriel.skillconnect.MainActivity
import hr.vgabriel.skillconnect.bll.helpers.AppLifecycleTracker
import hr.vgabriel.skillconnect.bll.services.AzureChatService
import hr.vgabriel.skillconnect.bll.services.ChatEventHandler
import hr.vgabriel.skillconnect.bll.services.ChatEventService
import hr.vgabriel.skillconnect.bll.services.LogType
import hr.vgabriel.skillconnect.bll.services.LoggingService
import hr.vgabriel.skillconnect.bll.services.NotificationService
import hr.vgabriel.skillconnect.bll.services.UserService
import hr.vgabriel.skillconnect.helpers.getId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ForegroundEventHandlerService() : Service() {
    companion object {
        private val ID =
            ForegroundEventHandlerService::class.simpleName!!.getId
    }

    var isActivityPaused = false

    private val binder = BackgroundChatServiceBinder()

    private var chatEventService: ChatEventService? = null
    private var userService: UserService? = null
    private var notificationService: NotificationService? = null
    private var loggingService: LoggingService? = null
    private var appLifecycleTracker: AppLifecycleTracker? = null
    private var azureChatService: AzureChatService? = null

    fun initDependencies(
        chatEventService: ChatEventService,
        userService: UserService,
        notificationService: NotificationService,
        loggingService: LoggingService,
        azureChatService: AzureChatService,
    ) {
        this.chatEventService = chatEventService
        this.userService = userService
        this.notificationService = notificationService
        this.loggingService = loggingService
        this.azureChatService = azureChatService

        chatEventService.unsubscribeFromEvents("$ID")
        chatEventService.subscribeToEvents(
            ChatEventHandler(
                id = "$ID",
                ChatEventType.CHAT_MESSAGE_RECEIVED
            ) {
                messageReceivedEventHandler(it)
            })
    }

    private fun messageReceivedEventHandler(payload: ChatEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val chatMessageReceivedEvent = payload as ChatMessageReceivedEvent
                var threadId = chatMessageReceivedEvent.chatThreadId;
                val user =
                    userService!!.getUsersByCommunicationIds(listOf(payload.sender.rawId))[0];
                if (isActivityPaused) {
                    notificationService!!.displayMessageNotification(
                        title = "${user.name} ${user.surname}",
                        "${payload.content}"
                    )
                }
            } catch (e: Exception) {
                loggingService?.log(
                    LogType.ERROR,
                    "[BackgroundChatService] messageReceivedEventHandler.",
                    e
                )
                return@launch
            }
        }
    }

    inner class BackgroundChatServiceBinder : Binder() {
        fun getService(): ForegroundEventHandlerService {
            return this@ForegroundEventHandlerService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(ID, notification)

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val intent = Intent(MainActivity.appContext, MainActivity::class.java).apply {}

        val pendingIntent = NotificationService.getPendingIntent(intent)
        val builder = NotificationService.getBuilder(
            "Skill Connect is running",
            "Listening for events",
            pendingIntent
        )

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        return builder.build()
    }
}