package hr.vgabriel.skillconnect.ui.elements.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MyOutlinedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        border = BorderStroke(0.dp, Color.Transparent),
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            disabledBackgroundColor = Color.Transparent,
            backgroundColor = Color.Transparent,
            contentColor = MaterialTheme.colors.primary,
        ),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        content = content,
        enabled = enabled
    )
}
