package hr.vgabriel.skillconnect

import App
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jakewharton.threetenabp.AndroidThreeTen
import hr.vgabriel.skillconnect.bll.fgservices.ForegroundEventHandlerService
import hr.vgabriel.skillconnect.bll.helpers.AppLifecycleTracker
import hr.vgabriel.skillconnect.bll.providers.LocalUserProvider
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.repositories.FirebaseRepository
import hr.vgabriel.skillconnect.bll.services.AuthService
import hr.vgabriel.skillconnect.bll.services.AzureChatService
import hr.vgabriel.skillconnect.bll.services.ChatEventService
import hr.vgabriel.skillconnect.bll.services.DialogService
import hr.vgabriel.skillconnect.bll.services.LoadingService
import hr.vgabriel.skillconnect.bll.services.LocalAuthService
import hr.vgabriel.skillconnect.bll.services.LocalAzureChatService
import hr.vgabriel.skillconnect.bll.services.LocalChatEventService
import hr.vgabriel.skillconnect.bll.services.LocalDialogService
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService
import hr.vgabriel.skillconnect.bll.services.LocalLoggingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.bll.services.LocalUserService
import hr.vgabriel.skillconnect.bll.services.LoggingService
import hr.vgabriel.skillconnect.bll.services.NavService
import hr.vgabriel.skillconnect.bll.services.NotificationService
import hr.vgabriel.skillconnect.bll.services.ToastService
import hr.vgabriel.skillconnect.bll.services.UserService
import hr.vgabriel.skillconnect.definitions.vm.ChatThreadVM
import hr.vgabriel.skillconnect.definitions.vm.ChatThreadVMFactory
import hr.vgabriel.skillconnect.definitions.vm.ChatThreadsVM
import hr.vgabriel.skillconnect.definitions.vm.ChatThreadsVMFactory
import hr.vgabriel.skillconnect.definitions.vm.LocalChatThreadVM
import hr.vgabriel.skillconnect.definitions.vm.LocalChatThreadsVM
import hr.vgabriel.skillconnect.definitions.vm.LocalUserVM
import hr.vgabriel.skillconnect.definitions.vm.TagsVM
import hr.vgabriel.skillconnect.definitions.vm.UserVM
import hr.vgabriel.skillconnect.ui.theme.SkillConnectTheme
import hr.vgabriel.skillconnect.ui.wrappers.LoadingContent
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    private lateinit var userVM: UserVM
    private lateinit var userTagsVM: TagsVM
    private lateinit var chatThreadVM: ChatThreadVM
    private lateinit var chatThreadsVM: ChatThreadsVM

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var toastService: ToastService
    private lateinit var dialogService: DialogService
    private lateinit var navService: NavService
    private lateinit var userProvider: UserProvider
    private lateinit var loggingService: LoggingService
    private lateinit var notificationService: NotificationService
    private lateinit var loadingService: LoadingService
    private lateinit var userService: UserService
    private lateinit var azureChatService: AzureChatService
    private lateinit var authService: AuthService
    private lateinit var chatEventService: ChatEventService

    private val appLifecycleTracker = AppLifecycleTracker()

    private var foregroundEventHandlerService: ForegroundEventHandlerService? = null

    lateinit var serviceConnection: ServiceConnection

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1

    private fun checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        } else {
            return
        }
    }

    companion object {
        lateinit var appContext: Context
        lateinit var appActivity: MainActivity

        fun restart() {
            appActivity.restartApp()
        }

        fun leaveActivity() {
            appActivity.moveTaskToBack(true)
        }
    }

    private fun isServiceRunning(serviceClass: Class<out Service>): Boolean {
        val manager = appActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = manager.getRunningServices(Int.MAX_VALUE)

        for (service in runningServices) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }

    private fun finishAllActivities() {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.let { manager ->
            val appTasks = manager.appTasks
            for (appTask in appTasks) {
                appTask.finishAndRemoveTask()
            }
        }
    }

    fun stopAllForegroundServices() {
        val activityManager =
            appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val foregroundServiceClasses = arrayListOf<Class<*>>()

        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        for (serviceInfo in runningServices) {
            if (serviceInfo.foreground) {
                val serviceClass = try {
                    Class.forName(serviceInfo.service.className)
                } catch (e: ClassNotFoundException) {
                    continue
                }
                foregroundServiceClasses.add(serviceClass)
            }
        }

        for (serviceClass in foregroundServiceClasses) {
            val serviceIntent = Intent(appContext, serviceClass)
            appContext.stopService(serviceIntent)
        }
    }

    private fun clearResources() {
        stopAllForegroundServices()
        finishAllActivities()

        finishAndRemoveTask()
        finishAffinity()
        finish()
    }

    private fun restartApp() {
        val packageManager = packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        clearResources()

        startActivity(intent)
        exitProcess(0)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(getString(R.string.channel_id), name, importance).apply {
                    description = descriptionText
                }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onResume() {
        super.onResume()
        foregroundEventHandlerService?.isActivityPaused = false
    }

    override fun onPause() {
        super.onPause()
        foregroundEventHandlerService?.isActivityPaused = true
    }

    override fun onDestroy() {
        super.onDestroy()

        clearResources()

        applicationContext.unbindService(serviceConnection)
        val serviceIntent = Intent(this, ForegroundEventHandlerService::class.java)
        stopService(serviceIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.userVM = UserVM()
        this.userTagsVM = TagsVM()
        userVM.userTagsVM = userTagsVM

        this.firebaseRepository =
            FirebaseRepository(
                Firebase.auth, FirebaseFirestore.getInstance()
            )
        this.toastService = ToastService()
        this.dialogService = DialogService()
        this.navService = NavService()
        this.userProvider =
            UserProvider(
                userVM = userVM
            )
        this.loggingService =
            LoggingService(
                repository = firebaseRepository,
                userProvider = userProvider
            )
        this.notificationService = NotificationService()
        this.loadingService = LoadingService(loggingService = loggingService)
        this.userService =
            UserService(
                repository = firebaseRepository,
                loggingService = loggingService,
                userProvider = userProvider
            )
        this.chatEventService =
            ChatEventService(
                loggingService
            )
        this.azureChatService =
            AzureChatService(
                loggingService = loggingService,
                repository = firebaseRepository,
                userService = userService,
                userProvider = userProvider,
                chatEventService = chatEventService,
                navService = navService,
                notificationService = notificationService
            )
        this.authService =
            AuthService(
                repository = firebaseRepository,
                userService = userService,
                azureChatService = azureChatService,
                userProvider = userProvider,
                loggingService = loggingService
            )
        val chatThreadVMFactory = ChatThreadVMFactory(
            azureChatService,
            userService,
            loggingService,
            chatEventService,
            userProvider
        )
        this.chatThreadVM =
            ViewModelProvider(
                this,
                chatThreadVMFactory
            )[ChatThreadVM::class.java]

        val chatThreadsVMFactory = ChatThreadsVMFactory(
            azureChatService,
            userService,
            loggingService,
            chatEventService,
            userProvider,
            notificationService
        )
        this.chatThreadsVM =
            ViewModelProvider(
                this,
                chatThreadsVMFactory
            )[ChatThreadsVM::class.java]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerActivityLifecycleCallbacks(appLifecycleTracker)
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

                val binder = service as ForegroundEventHandlerService.BackgroundChatServiceBinder
                foregroundEventHandlerService = binder.getService()
                foregroundEventHandlerService?.initDependencies(
                    chatEventService = chatEventService,
                    userService = userService,
                    notificationService = notificationService,
                    loggingService = loggingService,
                    azureChatService = azureChatService,
                )
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                foregroundEventHandlerService = null
            }
        }

        AndroidThreeTen.init(this)
        checkNotificationPermission()
        createNotificationChannel()

        appContext = applicationContext
        appActivity = this

        setContent {
            DependenciesProvider(
                _userVM = userVM,
                _firebaseRepository = firebaseRepository,
                _toastService = toastService,
                _dialogService = dialogService,
                _navService = navService,
                _userProvider = userProvider,
                _loggingService = loggingService,
                _notificationService = notificationService,
                _loadingService = loadingService,
                _userService = userService,
                _chatEventService = chatEventService,
                _azureChatService = azureChatService,
                _authService = authService,
                _chatThreadsVM = chatThreadsVM,
                _chatThreadVM = chatThreadVM
            ) {
                SkillConnectTheme {
                    LoadingContent {
                        App()
                    }
                }
            }
        }
    }
}

@Composable
fun DependenciesProvider(
    _userVM: UserVM,
    _firebaseRepository: FirebaseRepository,
    _toastService: ToastService,
    _dialogService: DialogService,
    _navService: NavService,
    _userProvider: UserProvider,
    _loggingService: LoggingService,
    _notificationService: NotificationService,
    _loadingService: LoadingService,
    _userService: UserService,
    _chatEventService: ChatEventService,
    _azureChatService: AzureChatService,
    _authService: AuthService,
    _chatThreadsVM: ChatThreadsVM,
    _chatThreadVM: ChatThreadVM,
    content: @Composable () -> Unit
) {

    val userVM: UserVM = remember { _userVM }

    val toastService = remember { _toastService }
    val dialogService = remember { _dialogService }
    val navService = remember { _navService }
    val userProvider = remember { _userProvider }
    val loggingService = remember { _loggingService }
    val loadingService = remember { _loadingService }
    val userService = remember { _userService }
    val chatEventService = remember { _chatEventService }
    val azureChatService = remember { _azureChatService }
    val authService = remember { _authService }

    val chatThreadVM = remember { mutableStateOf(_chatThreadVM) }
    val chatThreadsVM = remember { mutableStateOf(_chatThreadsVM) }

    CompositionLocalProvider(
        LocalNavService provides navService,
        LocalAuthService provides authService,
        LocalAzureChatService provides azureChatService,
        LocalUserProvider provides userProvider,
        LocalLoadingService provides loadingService,
        LocalLoggingService provides loggingService,
        LocalToastService provides toastService,
        LocalUserService provides userService,
        LocalChatEventService provides chatEventService,
        LocalDialogService provides dialogService,

        LocalChatThreadsVM provides chatThreadsVM.value,
        LocalChatThreadVM provides chatThreadVM.value,
        LocalUserVM provides userVM
    ) {
        BackHandler(
            enabled = true,
            onBack = {
                navService.popPreviousScreen()
            }
        )
        content()
    }
}