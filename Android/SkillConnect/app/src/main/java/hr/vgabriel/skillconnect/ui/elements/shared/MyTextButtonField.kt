package hr.vgabriel.skillconnect.ui.elements.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp

@Composable
fun MyTextButtonField(
    value: String,
    onValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Send,
    border: BorderStroke = BorderStroke(0.dp, Color.Transparent),
    shape: Shape = ShapeDefaults.Small,
    background: Color = MaterialTheme.colors.surface,
    tint: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
) {
    Row(
        modifier = modifier
            .background(background, shape)
            .border(border = border, shape = shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            textStyle = TextStyle.Default.copy(baselineShift = BaselineShift.None),
            value = value,
            onValueChange = onValueChange,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        IconButton(
            onClick = onButtonClick, modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Button",
                tint = tint
            )
        }
    }
}
