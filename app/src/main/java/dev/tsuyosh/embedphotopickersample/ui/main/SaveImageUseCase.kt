package dev.tsuyosh.embedphotopickersample.ui.main

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SaveImageUseCase(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun execute(contentUri: Uri): Uri = withContext(ioDispatcher) {
        val file = File.createTempFile("image-", null, context.cacheDir)
        context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
            inputStream.copyTo(file.outputStream())
        }
        Uri.fromFile(file)
    }
}
