package dev.tsuyosh.embedphotopickersample

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.drawToBitmap
import dev.tsuyosh.embedphotopickersample.ui.main.MainScreen
import dev.tsuyosh.embedphotopickersample.ui.theme.EmbedPhotoPickerSampleTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                Timber.d("Selected URIs: $uris")
            }
        }

    private lateinit var composeView: ComposeView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        composeView = ComposeView(this).apply {
            setContent {
                EmbedPhotoPickerSampleTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MainScreen(
                            onTakeScreenshotClick = { takeScreenshot() },
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
        setContentView(composeView)
    }

    private fun takeScreenshot() {
        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val contentResolver = applicationContext.contentResolver
        val imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return

        contentResolver.openOutputStream(imageUri)?.use { os ->
            composeView.drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            true
        } ?: return

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        contentResolver.update(imageUri, contentValues, null, null)
    }

    private fun openExternalPhotoPicker() {
        pickMultipleMedia
            .launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
