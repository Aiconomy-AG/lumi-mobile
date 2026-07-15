package org.example.project.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun rememberPhotoPicker(
    onPhotoPicked: (PickedPhoto) -> Unit,
    onError: (String) -> Unit,
): PhotoPickerLauncher {
    val delegate = remember(onPhotoPicked, onError) {
        IosPhotoPickerDelegate(onPhotoPicked, onError)
    }

    return remember(delegate) {
        object : PhotoPickerLauncher {
            override fun launch() {
                val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
                if (rootController == null) {
                    onError("Could not open photo picker.")
                    return
                }

                val picker = UIImagePickerController()
                picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                picker.allowsEditing = true
                picker.delegate = delegate
                delegate.picker = picker
                rootController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

private class IosPhotoPickerDelegate(
    private val onPhotoPicked: (PickedPhoto) -> Unit,
    private val onError: (String) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    var picker: UIImagePickerController? = null

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage

        if (image == null) {
            picker.dismissViewControllerAnimated(true, completion = null)
            onError("Could not read selected photo.")
            return
        }

        val data = UIImageJPEGRepresentation(image, 0.9)
        if (data == null) {
            picker.dismissViewControllerAnimated(true, completion = null)
            onError("Could not prepare selected photo.")
            return
        }

        onPhotoPicked(
            PickedPhoto(
                bytes = data.toByteArray(),
                fileName = "photo.jpg",
                mimeType = "image/jpeg",
            )
        )
        picker.dismissViewControllerAnimated(true, completion = null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    platform.posix.memcpy(bytes.refTo(0), this.bytes, length)
    return bytes
}
