package dev.tsuyosh.embedphotopickersample.ui.main

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TakeScreenShotUseCase(
    private val contentResolver: ContentResolver
) {
    suspend fun execute(targetView: View) = withContext(Dispatchers.IO) {
        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext

        contentResolver.openOutputStream(imageUri)?.use { os ->
            targetView.drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            true
        } ?: return@withContext

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        contentResolver.update(imageUri, contentValues, null, null)
    }
}