package hr.vgabriel.skillconnect.ui.wrappers

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO: fix
@Composable
fun ScrollableLazyContent(
    modifier: Modifier, contents: List<@Composable () -> Unit>
) {
    val scrollState = rememberLazyListState()

    KeyboardAwareContent {
        LazyColumn(
            state = scrollState
        ) {
            contents.forEach { content ->
                item() {
                    content()
                }
            }
        }

    }
}