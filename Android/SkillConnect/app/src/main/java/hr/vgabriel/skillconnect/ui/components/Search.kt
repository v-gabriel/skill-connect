package hr.vgabriel.skillconnect.ui.components

import Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.bll.providers.LocalUserProvider
import hr.vgabriel.skillconnect.bll.services.LocalAzureChatService
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.bll.services.LocalUserService
import hr.vgabriel.skillconnect.bll.services.ToastData
import hr.vgabriel.skillconnect.definitions.models.User
import hr.vgabriel.skillconnect.definitions.vm.TagsVM
import hr.vgabriel.skillconnect.ui.elements.shared.MyTextButtonField
import hr.vgabriel.skillconnect.ui.elements.shared.NoData
import hr.vgabriel.skillconnect.ui.elements.shared.Tags
import hr.vgabriel.skillconnect.ui.elements.shared.UserImage


@Composable
fun Search() {
    val lazyColumnState = rememberLazyListState()
    val performSearch = remember { mutableStateOf(false) }
    val userItemClick = remember { mutableStateOf<User?>(null) }

    val userService = LocalUserService.current
    val loadingService = LocalLoadingService.current
    val toastService = LocalToastService.current
    val userProvider = LocalUserProvider.current
    val navService = LocalNavService.current
    val azureChatService = LocalAzureChatService.current

    val searchQuery = remember { mutableStateOf("") }
    val users = remember { mutableStateListOf<User>() }
    suspend fun updateUsers() {
        val moreUsers = userService.searchUsers(
            queryString = searchQuery.value,
            lastSnapshot = if (users.isNotEmpty()) users.last().documentSnapshot else null
        )
        users.addAll(moreUsers)
    }

    LaunchedEffect(userItemClick.value) {
        if (userItemClick.value != null) {
            loadingService.delegateLoad {
                try {
                    val clickedUserChatThreadIds =
                        userService.getUserChatThreads(userId = userItemClick.value!!.id)
                            .map { it.chatThreadId }
                    val userChatThreadIds = userService.getUserChatThreads(userProvider.user!!.id)
                        .map { it.chatThreadId }
                    val mutualChatThreadIds =
                        userChatThreadIds.toSet().intersect(clickedUserChatThreadIds.toSet())

                    if (mutualChatThreadIds.isEmpty()) {
                        val userIds = listOf(
                            userProvider.user!!.id, userItemClick.value!!.id
                        )
                        azureChatService.createChatThread(
                            userIds = userIds
                        )

                        navService.navigateToScreen(Screen.Chats)
                        return@delegateLoad
                    } else {
                        toastService.setToast(
                            ToastData(
                                message = "Room already exists."
                            )
                        )
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

    LaunchedEffect(lazyColumnState.isScrollInProgress, performSearch.value) {
        var lastVisibleIndex = lazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        if (performSearch.value) {
            users.clear()
            updateUsers()
            performSearch.value = false
        } else if (lazyColumnState.isScrollInProgress && lastVisibleIndex >= users.size - 2) {
            updateUsers()
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

        UserList(users = users, lazyColumnState = lazyColumnState, onItemClick = { it ->
            userItemClick.value = it
        })
    }

}

@Composable
fun UserList(
    users: List<User>, lazyColumnState: LazyListState, onItemClick: (user: User) -> Unit
) {
    if (users.isEmpty()) {
        NoData()
    } else {
        LazyColumn(
            state = lazyColumnState,
        ) {
            items(users) { user ->
                UserItem(user) {
                    onItemClick(user)
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User, onClick: () -> Unit
) {
    val tagsVM: TagsVM = remember { TagsVM() };

    tagsVM.items.clear()
    tagsVM.items.addAll(user.tags);

    val initials = user.name.take(1) + user.surname.take(1)

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
                    .align(Alignment.CenterVertically)
            ) {
                UserImage(initials = initials)
//                Box(
//                    modifier = Modifier
//                        .size(48.dp)
//                        .background(
//                            color = MaterialTheme.colors.primary, shape = CircleShape
//                        )
//                        .fillMaxHeight()
//                        .align(Alignment.CenterVertically),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = initials,
//                        style = MaterialTheme.typography.subtitle1,
//                        color = MaterialTheme.colors.onPrimary
//                    )
//                }

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxHeight()
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "${user.name} ${user.surname}", maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Tags(
                        enabled = false,
                        viewModel = tagsVM
                    )
                }

            }
            Column(
                modifier = Modifier.padding(start = 4.dp)
            ) {
                IconButton(onClick = { }, enabled = false) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Arrow Right",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}




