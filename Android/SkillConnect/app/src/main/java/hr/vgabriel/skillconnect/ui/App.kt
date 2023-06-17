import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.bll.services.LocalDialogService
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.ui.components.ChatThread
import hr.vgabriel.skillconnect.ui.components.ChatThreads
import hr.vgabriel.skillconnect.ui.components.Init
import hr.vgabriel.skillconnect.ui.components.Landing
import hr.vgabriel.skillconnect.ui.components.Login
import hr.vgabriel.skillconnect.ui.components.Profile
import hr.vgabriel.skillconnect.ui.components.Settings
import hr.vgabriel.skillconnect.ui.components.Register
import hr.vgabriel.skillconnect.ui.components.Search
import hr.vgabriel.skillconnect.ui.components.SideBarNavigation
import kotlinx.coroutines.launch

sealed class Screen(val title: String, val screen: @Composable () -> Unit) {
    object Init : Screen("Init", { Init() })

    object Landing : Screen("Landing", { Landing() })
    object Login : Screen("Login", { Login() })
    object Register : Screen("Register", { Register() })

    object Chat : Screen("Chat", { ChatThread() })

    object Search : Screen("Search", { Search() })
    object Chats : Screen("Chats", { ChatThreads() })

    object Settings : Screen("Settings", { Settings() })
    object Profile : Screen("Profile", { Profile() })

    companion object {
        val standaloneScreens: List<Screen>
            get() = listOf(
                Screen.Landing,
                Screen.Register,
                Screen.Login,
                Screen.Chat,
                Screen.Init,
                Screen.Profile
            )
    }
}

@Composable
fun App() {
    val toastService = LocalToastService.current
    val dialogService = LocalDialogService.current

    val navService = LocalNavService.current
    val loadingService = LocalLoadingService.current

    val loadingInstances = loadingService.loadingJobsCount.collectAsState()

    val currentToast = toastService.toastData.collectAsState();
    val showToast = toastService.showToast.collectAsState()

    val currentScreen = navService.currentScreen.collectAsState()

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(showToast.value) {
        if (currentToast?.value != null) {
            scope.launch {
                val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                    currentToast.value!!.message,
                    if (currentToast.value!!.closeable) "Close" else null,
                    currentToast.value!!.duration
                )
                when (snackbarResult) {
                    SnackbarResult.Dismissed -> scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    SnackbarResult.ActionPerformed -> scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
    dialogService.DialogInit()


    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (currentScreen.value !in Screen.standaloneScreens) {
                TopAppBar(
                    elevation = 0.dp,
                    backgroundColor = MaterialTheme.colors.background,
                    title = { Text(text = currentScreen.value.title) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    scaffoldState.drawerState.open()
                                }
                            },
                            content = { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                        )
                    })
            }
        },
        snackbarHost = { snackbarHostState ->
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        modifier = Modifier.padding(16.dp),
                        actionOnNewLine = false,
                        backgroundColor = toastService.getBackgroundColor(currentToast.value!!.type),
                        contentColor = toastService.getContentColor(currentToast.value!!.type),
                        actionColor = toastService.getContentColor(currentToast.value!!.type)
                    )
                }
            )
        },
        drawerContent = {
            SideBarNavigation(
                currentScreen = currentScreen,
                navigateToScreen = { screen ->
                    navService.navigateToScreen(screen)
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                currentScreen.value.screen()
            }
        },
        drawerGesturesEnabled = currentScreen.value !in Screen.standaloneScreens && loadingInstances.value == 0,
        drawerContentColor = MaterialTheme.colors.background,
        drawerElevation = DrawerDefaults.Elevation,
    )
}