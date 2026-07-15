package org.example.project.presentation.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPhotoPicker(
    onPhotoPicked: (PickedPhoto) -> Unit,
    onError: (String) -> Unit,
): PhotoPickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "image/jpeg"
            val fileName = context.displayNameFor(uri) ?: "photo.${mimeType.substringAfter('/', "jpg")}"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Could not read selected photo.")
            onPhotoPicked(
                PickedPhoto(
                    bytes = bytes,
                    fileName = fileName,
                    mimeType = mimeType,
                    previewUri = uri.toString(),
                )
            )
        } catch (exception: Exception) {
            onError(exception.message ?: "Could not read selected photo.")
        }
    }

    return remember(launcher) {
        object : PhotoPickerLauncher {
            override fun launch() {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

private fun Context.displayNameFor(uri: Uri): String? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}
