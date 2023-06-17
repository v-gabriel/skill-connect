package hr.vgabriel.skillconnect.ui.components

import Screen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.MainActivity
import hr.vgabriel.skillconnect.R
import hr.vgabriel.skillconnect.bll.services.LocalAuthService
import hr.vgabriel.skillconnect.bll.services.LocalLoadingService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.bll.services.LocalToastService
import hr.vgabriel.skillconnect.bll.services.ToastData
import hr.vgabriel.skillconnect.ui.elements.shared.MyOutlinedButton

sealed class SideBarItem(val screen: Screen, val icon: ImageVector) {
    object Search : SideBarItem(Screen.Search, Icons.Filled.Search)
    object Chats : SideBarItem(Screen.Chats, Icons.Filled.List)
    object Settings : SideBarItem(Screen.Settings, Icons.Filled.Settings)
}

@Composable
fun SideBarNavigation(
    currentScreen: State<Screen>,
    navigateToScreen: (Screen) -> Unit,
) {
    val navItems = listOf(
        SideBarItem.Chats,
        SideBarItem.Search,
        SideBarItem.Settings
    )

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Skill Connect Logo",
                modifier = Modifier.size(50.dp)
            )
        }

        navItems.forEach { item ->
            SideBarItem(
                sideBarItem = item,
                isSelected = currentScreen.value == item.screen,
                navigateToScreen = navigateToScreen
            )
        }
    }
}

@Composable
fun SideBarItem(
    sideBarItem: SideBarItem,
    isSelected: Boolean,
    navigateToScreen: (Screen) -> Unit
) {
    val colors = MaterialTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isSelected) colors.primary.copy(alpha = 0.05f) else colors.background)
            .clickable { navigateToScreen(sideBarItem.screen) }
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = sideBarItem.icon,
            contentDescription = sideBarItem.screen.title,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) colors.primary else colors.onBackground
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = sideBarItem.screen.title,
            style = MaterialTheme.typography.subtitle1,
            color = if (isSelected) colors.primary else colors.onBackground
        )
    }
}
