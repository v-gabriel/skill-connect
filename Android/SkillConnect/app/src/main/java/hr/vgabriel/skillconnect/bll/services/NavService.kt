package hr.vgabriel.skillconnect.bll.services

import Screen
import androidx.compose.runtime.staticCompositionLocalOf
import hr.vgabriel.skillconnect.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface INavService {
    fun navigateToScreen(screen: Screen)
    fun popPreviousScreen()
    fun resetPreviousScreens()
}

val LocalNavService =
    staticCompositionLocalOf<NavService> { error("No NavigationService provided") }

class NavService : INavService {

    private val _previousScreens: MutableList<Screen> = mutableListOf()

    private val _currentScreen: MutableStateFlow<Screen> = MutableStateFlow(Screen.Init)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    override fun navigateToScreen(screen: Screen) {
        addToPreviousScreens(_currentScreen.value)
        setCurrentScreen(screen)
    }

    private fun setCurrentScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    private fun addToPreviousScreens(screen: Screen) {
        _previousScreens.add(screen)
    }

    override fun resetPreviousScreens() {
        _previousScreens.clear()
    }

    override fun popPreviousScreen() {
        if (_previousScreens.isNotEmpty()) {
            val lastIndex = _previousScreens.lastIndex
            val previousScreen = _previousScreens.removeAt(lastIndex)
            setCurrentScreen(previousScreen)
        } else{
            MainActivity.leaveActivity()
        }
    }
}
