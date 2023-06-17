package hr.vgabriel.skillconnect.ui.wrappers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun ScrollableContent(
    innerPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
        .padding(innerPadding)
        .systemBarsPadding()
        .imePadding()
    , contents: List<@Composable () -> Unit>
) {
    val scrollState = rememberScrollState()

    KeyboardAwareContent {
        Box(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState, true)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                contents.forEach { content ->
                    content()
                }
            }
        }
    }
}

