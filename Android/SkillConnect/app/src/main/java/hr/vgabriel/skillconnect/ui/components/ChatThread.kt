package hr.vgabriel.skillconnect.ui.components

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.vgabriel.skillconnect.bll.providers.LocalUserProvider
import hr.vgabriel.skillconnect.bll.services.LocalAzureChatService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.definitions.models.UserMessage
import hr.vgabriel.skillconnect.definitions.vm.ChatThreadVM
import hr.vgabriel.skillconnect.ui.elements.shared.MyTextButtonField
import hr.vgabriel.skillconnect.ui.elements.shared.NoData
import kotlinx.coroutines.flow.StateFlow
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun ChatThread(
) {
    val lazyColumnState = rememberLazyListState()
    val navService = LocalNavService.current
    val userProvider = LocalUserProvider.current

    var chatThreadVM: ChatThreadVM = viewModel()
    val userMessages = chatThreadVM.userMessages.collectAsState()
    val participants = chatThreadVM.participants.collectAsState()

    val title = remember(participants) {
        derivedStateOf {
            participants.value.filter { it.id != userProvider.user!!.id }
                .joinToString(", ") { "${it.name} ${it.surname}" }
        }
    }

    LaunchedEffect(lazyColumnState.isScrollInProgress) {
        if (chatThreadVM.userMessages.value.isNotEmpty()) {
            var lastVisibleIndex = lazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

            if (lastVisibleIndex >= userMessages.value.size - 2) {
                chatThreadVM.fetchMoreMessages()
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            backgroundColor = MaterialTheme.colors.background,
            elevation = 0.dp,
            title = { Text(text = title.value) },
            navigationIcon = {
                IconButton(onClick = { navService.navigateToScreen(Screen.Chats) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
                }
            })
    }, bottomBar = { SendChatArea(threadId = chatThreadVM.chatThreadId) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ChatMessageList(
                messages = userMessages, lazyColumnState = lazyColumnState
            )
        }
    }
}

@Composable
fun ChatMessageList(
    messages: State<List<UserMessage>>,
    lazyColumnState: LazyListState,
) {
    val userProvider = LocalUserProvider.current

    if (messages.value.isEmpty()) {
        NoData()
    } else {
        LazyColumn(
            state = lazyColumnState,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
            reverseLayout = true
        ) {
            itemsIndexed(messages.value) { index, userMessage ->
                ChatMessage(
                    userMessage = userMessage,
                    modifier = Modifier.fillMaxWidth(),
                    isMyMessage = userMessage.user.communicationUserId == userProvider.user?.communicationUserId
                )
            }
        }
    }
}

@Composable
fun SendChatArea(
    threadId: StateFlow<String>
) {
    var message = remember { mutableStateOf("") }

    val azureChatService = LocalAzureChatService.current

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        MyTextButtonField(
            value = message.value,
            onValueChange = { message.value = it },
            onButtonClick = {
                azureChatService.sendMessage(threadId.value, message.value)
                message.value = ""
            },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Send,
            tint = MaterialTheme.colors.primary,
        )
    }
}

@Composable
fun ChatMessage(
    isMyMessage: Boolean = false, userMessage: UserMessage, modifier: Modifier = Modifier
) {
    val backgroundColor = if (isMyMessage) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.surface
    }

    val contentColor = if (isMyMessage) {
        MaterialTheme.colors.onPrimary
    } else {
        MaterialTheme.colors.onSurface
    }

    val createdOn = remember {
        val now = LocalDate.now()
        val created = userMessage.message.createdOn.toLocalDate()
        if (now == created) {
            userMessage.message.createdOn.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            userMessage.message.createdOn.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        BoxWithConstraints {
            val padding = if (isMyMessage) {
                PaddingValues(start = maxWidth * 0.3f, top = 16.dp, end = 16.dp, bottom = 16.dp)
            } else {
                PaddingValues(end = maxWidth * 0.3f, top = 16.dp, start = 16.dp, bottom = 16.dp)
            }
            val align: Alignment.Horizontal = if (isMyMessage) {
                Alignment.End
            } else {
                Alignment.Start
            }
            Column(
                modifier = Modifier
                    .height(this@BoxWithConstraints.maxHeight)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = createdOn, style = MaterialTheme.typography.caption.copy(
                            color = MaterialTheme.colors.onBackground.copy(
                                alpha = 0.5f
                            )
                        ), textAlign = TextAlign.Center
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .width(IntrinsicSize.Min)
                        .wrapContentWidth(align),
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                backgroundColor, shape = RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            modifier = Modifier.wrapContentWidth(align = align),

                            text = userMessage.message.content.message,
                            style = MaterialTheme.typography.body1,
                            color = contentColor,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}



