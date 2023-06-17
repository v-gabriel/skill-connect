package hr.vgabriel.skillconnect.ui.components

import Screen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import hr.vgabriel.skillconnect.R
import hr.vgabriel.skillconnect.bll.services.LocalAuthService
import hr.vgabriel.skillconnect.bll.services.LocalNavService
import hr.vgabriel.skillconnect.ui.wrappers.ScrollableContent

@Composable
fun Landing() {
    val navService = LocalNavService.current
    val authService = LocalAuthService.current

    ScrollableContent(
        contents = listOf {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 75.dp, bottom = 25.dp, start = 25.dp, end = 25.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Skill Connect Logo",
                        modifier = Modifier.size(75.dp)
                    )

                    Text(
                        text = "Skill Connect",
                        style = MaterialTheme.typography.h4,
                    )
                }

                Spacer(modifier = Modifier.padding(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(top = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { navService.navigateToScreen(Screen.Login) },
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Login",
                            modifier = Modifier
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                        )
                    }

                    OutlinedButton(
                        onClick = { navService.navigateToScreen(Screen.Register) },
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Register",
                                modifier = Modifier
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Register"
                            )
                        }
                    }
                }
            }
        }
    )
}


