package dev.tsuyosh.embedphotopickersample.ui.main

import android.os.Build
import android.os.ext.SdkExtensions
import android.view.SurfaceView
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import dev.tsuyosh.embedphotopickersample.ui.theme.EmbedPhotoPickerSampleTheme
import timber.log.Timber

@Composable
fun MainScreen(
    onTakeScreenshotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isOpened by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Buttons(
            isOpened = isOpened,
            isExpanded = isExpanded,
            onOpenCloseButtonClick = { isOpened = !isOpened },
            onExpandCollapseButtonClick = { isExpanded = !isExpanded },
            onTakeScreenshotClick = onTakeScreenshotClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isOpened) {
            if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 15) {
                PhotoPicker(
                    isExpanded = isExpanded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Text(
                    text = "PhotoPicker is not supported yet.",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun Buttons(
    isOpened: Boolean,
    isExpanded: Boolean,
    onOpenCloseButtonClick: () -> Unit,
    onExpandCollapseButtonClick: () -> Unit,
    onTakeScreenshotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onOpenCloseButtonClick
        ) {
            Text(text = if (isOpened) "Close" else "Open")
        }
        Button(
            onClick = onExpandCollapseButtonClick
        ) {
            Text(text = if (isExpanded) "Collapse" else "Expand")
        }
        Button(
            onClick = onTakeScreenshotClick
        ) {
            Text(text = "Take Screenshot")
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
@Composable
fun PhotoPicker(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    // TODO: Add onPhotoSelected
) {
    val context = LocalContext.current
    val photoPickerController = remember { PhotoPickerController(context, SurfaceView(context)) }
    LaunchedEffect(isExpanded) {
        Timber.d("LaunchedEffect: isExpanded=$isExpanded")
        photoPickerController.setExpanded(isExpanded)
    }

    AndroidView(
        factory = {
            photoPickerController.surfaceView
        },
        update = {
        },
        onRelease = {
            Timber.d("onRelease")
            photoPickerController.release()
        },
        modifier = modifier
            .onSizeChanged { size ->
                Timber.d("onSizeChanged: size=$size")
                photoPickerController.notifySizeChanged(
                    size.width,
                    size.height
                )
            }
    )
}

@Preview
@Composable
fun ButtonsPreview() {
    var isOpened by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    EmbedPhotoPickerSampleTheme {
        Buttons(
            isOpened = isOpened,
            isExpanded = isExpanded,
            onOpenCloseButtonClick = { isOpened = !isOpened },
            onExpandCollapseButtonClick = { isExpanded = !isExpanded },
            onTakeScreenshotClick = {},
        )
    }
}