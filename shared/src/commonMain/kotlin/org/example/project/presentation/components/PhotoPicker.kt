package org.example.project.presentation.components

import androidx.compose.runtime.Composable

data class PickedPhoto(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
    val previewUri: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedPhoto) return false
        return bytes.contentEquals(other.bytes) &&
            fileName == other.fileName &&
            mimeType == other.mimeType &&
            previewUri == other.previewUri
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (previewUri?.hashCode() ?: 0)
        return result
    }
}

interface PhotoPickerLauncher {
    fun launch()
}

@Composable
expect fun rememberPhotoPicker(
    onPhotoPicked: (PickedPhoto) -> Unit,
    onError: (String) -> Unit = {},
): PhotoPickerLauncher
