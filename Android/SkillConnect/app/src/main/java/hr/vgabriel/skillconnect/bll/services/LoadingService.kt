package hr.vgabriel.skillconnect.bll.services

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ILoadingService {
    suspend fun delegateLoad(function: suspend () -> Unit)
}

val LocalLoadingService =
    staticCompositionLocalOf<LoadingService> { error("No LoadingService provided") }

class LoadingService(
    private val loggingService: LoggingService
) : ILoadingService {
    private val _loadingJobsCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val loadingJobsCount: StateFlow<Int> = _loadingJobsCount.asStateFlow()

    override suspend fun delegateLoad(function: suspend () -> Unit) {
        run {
            _loadingJobsCount.value += 1;
            try {
                function()
            } catch (e: Exception) {
                loggingService.log(LogType.ERROR, "[LoadingService] delegateLoad", e);
            } finally {
                _loadingJobsCount.value -= 1;
            }
        }
    }
}