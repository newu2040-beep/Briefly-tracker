package com.example.data.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ProfilePictureHelper {

    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val picturesDir = File(context.filesDir, "profile_pictures")
            if (!picturesDir.exists()) {
                picturesDir.mkdirs()
            }

            val targetFile = File(picturesDir, "user_avatar_${System.currentTimeMillis()}.jpg")
            
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearSavedProfilePictures(context: Context) {
        try {
            val picturesDir = File(context.filesDir, "profile_pictures")
            if (picturesDir.exists()) {
                picturesDir.listFiles()?.forEach { it.delete() }
            }
        } catch (_: Exception) {}
    }
}
