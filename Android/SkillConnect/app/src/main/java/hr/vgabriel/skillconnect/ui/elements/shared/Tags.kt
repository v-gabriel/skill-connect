package hr.vgabriel.skillconnect.ui.elements.shared


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Tag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.vgabriel.skillconnect.definitions.vm.TagsVM
import hr.vgabriel.skillconnect.helpers.removeSpaces
import hr.vgabriel.skillconnect.ui.elements.form.MyFormTextField

@Composable
fun Tags(
    viewModel: TagsVM = viewModel(),
    enabled: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        if (enabled) {
            MyFormTextField(
                value = viewModel.textInput.value,
                onValueChange = { it ->
                    viewModel.textInput.value = it.lowercase().removeSpaces
                },
                label = "Tag",
                infoMessage = viewModel.infoMessage,
                onButtonClick = {
                    if (viewModel.isValid) {
                        viewModel.addItem(viewModel.textInput.value)
                        viewModel.textInput.value = ""
                    }
                },
                buttonIcon = Icons.Default.Add,
                enabled = enabled
            )
        }

        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .alpha(if (enabled) 1f else 0.5f)
        ) {
            if (viewModel.items.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                ) {
                    items(viewModel.items) { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = enabled){
                                    if (enabled) {
                                        viewModel.items.remove(item)
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                tint = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
                                contentDescription = "Tag",
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(16.dp)
                            )
                            Text(
                                text = item,
                                style = MaterialTheme.typography.body2,
                            )
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.CenterStart)
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = "No tags",
                        style = MaterialTheme.typography.body2,
                    )
                }
            }
        }
    }
}


