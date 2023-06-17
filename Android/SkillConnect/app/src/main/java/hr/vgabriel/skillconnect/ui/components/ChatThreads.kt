package hr.vgabriel.skillconnect.ui.components

import Screen
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.vgabriel.skillconnect.bll.providers.LocalUserProvider
import hr.vgabriel.skillconnect.bll.providers.UserProvider
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.bll.services.ToastData
import hr.vgabriel.skillconnect.definitions.vm.ChatThreadItemVM
import hr.vgabriel.skillconnect.definitions.vm.ChatThreadVM
import hr.vgabriel.skillconnect.definitions.vm.LocalChatThreadsVM
import hr.vgabriel.skillconnect.ui.elements.shared.MyTextButtonField
import hr.vgabriel.skillconnect.ui.elements.shared.NoData
import kotlinx.coroutines.launch


@Composable
fun ChatThreads() {
    val userProvider = LocalUserProvider.current

    val searchQuery = remember { mutableStateOf("") }

    val lazyColumnState = rememberLazyListState()

    val performSearch = remember { mutableStateOf(true) }
    val chatClick = remember { mutableStateOf<String?>(null) }

    val chatThreadsVM = LocalChatThreadsVM.current;

    var chatThreads = chatThreadsVM.chatThreadIds.collectAsState()
    val chatThreadItemVMs = chatThreadsVM.chatThreadItemVMs.collectAsState()

    val chatThreadsRefresh = chatThreadsVM.chatThreadsRefresh.collectAsState()
    val chatThreadsFilteredItemVMs = remember(
        chatThreadItemVMs.value,
        searchQuery.value,
        chatThreadsRefresh.value,
    ) {
        derivedStateOf {
            chatThreadItemVMs.value.filter { item ->
                item.participants.value.filter { it.id != userProvider.user!!.id }
                    .any { participant ->
                        participant.fullName.contains(searchQuery.value, ignoreCase = true)
                    }
            }.sortedByDescending { item ->
                item.lastChatMessage?.value?.message?.createdOn
            }.toMutableList()
        }
    }

    val navigationService = LocalNavService.current
    val toastService = LocalToastService.current

    val chatThreadVM: ChatThreadVM = viewModel()

    LaunchedEffect(chatClick.value) {
        if (chatClick.value != null) {
            navigationService.navigateToScreen(Screen.Chat);
            chatThreadVM.viewModelScope.launch {
                try {
                    val result = chatThreadVM.initChatThreadVM(chatClick.value!!);
                    if (!result) {
                        throw Exception("Chat thread init failed")
                    }
                } catch (e: Exception) {
                    toastService.setToast(
                        ToastData(
                            message = "Something went wrong. Please try again later."
                        )
                    )
                }
            }
        }
    }

    Column() {
        MyTextButtonField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            onButtonClick = { performSearch.value = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            icon = Icons.Default.Search
        )
        ChatThreadList(
            chatThreads = chatThreadsFilteredItemVMs.value,
            lazyColumnState = lazyColumnState,
            onItemClick = { chatThreadItemVM ->
                val chatThread =
                    chatThreads.value.find { it == chatThreadItemVM.chatThreadId.value };
                chatClick.value = chatThread
            })
    }
}

@Composable
fun ChatThreadList(
    chatThreads: MutableList<ChatThreadItemVM>,
    lazyColumnState: LazyListState,
    onItemClick: (chatThread: ChatThreadItemVM) -> Unit
) {

    if (chatThreads.isEmpty()) {
        NoData()
    } else {
        LazyColumn(
            state = lazyColumnState,
        ) {
            items(chatThreads) { chatThread ->
                ChatThreadItem(chatThread) {
                    onItemClick(chatThread)
                }
            }
        }
    }
}

@Composable
fun ChatThreadItem(
    chatThread: ChatThreadItemVM, onClick: () -> Unit
) {
    val participants = chatThread.participants.collectAsState()
    val lastMessage = chatThread.lastChatMessage.collectAsState()

    val userProvider: UserProvider = LocalUserProvider.current
    val chatThreadsVM = LocalChatThreadsVM.current

    val title = remember(participants) {
        derivedStateOf {
            participants.value.filter { it.id != userProvider.user!!.id }
                .joinToString(", ") { "${it.name} ${it.surname}" }
        }
    }

    LaunchedEffect(lastMessage.value) {
        chatThreadsVM.chatThreadsRefresh.value = !chatThreadsVM.chatThreadsRefresh.value
    }

    val lastMessageContent = remember(lastMessage) {
        derivedStateOf {
            if (!lastMessage.value?.message?.content?.message.isNullOrEmpty()) {
                val lastMessageContent = "${lastMessage.value?.message?.content?.message}"
                //var userName = "${lastMessage.value?.user?.name ?: ""} ${lastMessage.value?.user?.surname ?: ""}"
                if (lastMessage.value?.user?.id == userProvider.user!!.id) {
                    "You: $lastMessageContent"
                } else {
                    lastMessageContent
                }
            } else {
                ""
            }
        }
    }

    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colors.primary, shape = CircleShape
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (participants.value.count() == 1) {
                            Icons.Default.Person
                        } else {
                            Icons.Default.Person2
                        }, contentDescription = "Icon", tint = MaterialTheme.colors.onPrimary
                    )
                }

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(text = title.value)
                    Text(
                        text = if (lastMessageContent.value.isNullOrEmpty()) "No data" else lastMessageContent.value,
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontStyle = if (lastMessageContent.value.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal
                    )
                }
            }
        }
    }
}




