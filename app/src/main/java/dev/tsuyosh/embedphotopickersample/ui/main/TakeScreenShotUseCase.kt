package dev.tsuyosh.embedphotopickersample.ui.main

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.SurfaceView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import timber.log.Timber
import kotlin.coroutines.resume

class TakeScreenShotUseCase(
    private val contentResolver: ContentResolver
) {
    suspend fun execute(targetView: SurfaceView) = withContext(Dispatchers.IO) {
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

        val bitmap = copyPixel(targetView) ?: return@withContext
        try {
            contentResolver.openOutputStream(imageUri)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, os)
                os.flush()
                true
            } ?: return@withContext

            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(imageUri, contentValues, null, null)
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun copyPixel(targetView: SurfaceView): Bitmap? =
        suspendCancellableCoroutine { continuation ->
            val bitmap = createBitmap(targetView.width, targetView.height)
            PixelCopy.request(
                targetView,
                bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        Timber.e("PixelCopy failed: $result")
                        continuation.resume(null)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }
}