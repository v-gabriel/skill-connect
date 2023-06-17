package hr.vgabriel.skillconnect.ui.wrappers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService

@Composable
fun LoadingContent(content: @Composable () -> Unit) {

    val loadingService = LocalLoadingService.current

    val loadingJobsCount = loadingService.loadingJobsCount.collectAsState();
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        content()
        if (loadingJobsCount.value > 0) {
            Box(
                Modifier.matchParentSize().background(Color.Gray.copy(alpha = 0.5f))
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}
