package hr.vgabriel.skillconnect.definitions.vm

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class TagsVM : ViewModel() {
    var items = mutableStateListOf<String>()
    val textInput = mutableStateOf("")

    private val maxTags = 7

    val canAddItem: Boolean
        get() = items.size < maxTags

    fun addItem(item: String) {
        if (canAddItem) {
            items.add(item)
        }
    }

    val isValid: Boolean
        get() = textInput.value.length in 3..10 && textInput.value.isNotEmpty()

    val infoMessage: String
        get() = "Tag cannot be empty and must have 3 to 10 characters. Max $maxTags tags. Click on tag to remove it."
}