package dev.tsuyosh.embedphotopickersample.ui.main

import android.net.Uri
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
import androidx.compose.ui.layout.onPlaced
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
                EmbeddedPhotoPicker(
                    isExpanded = isExpanded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Text(
                    text = "EmbeddedPhotoPicker is not supported on this device",
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
fun EmbeddedPhotoPicker(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    // TODO: Add onPhotoSelected
    onPhotoSelected: (List<Uri>) -> Unit = {},
) {
    // TODO: Hold EmbeddedPhotoPicker state outside of this composable to retain the photo picker state
    // TODO: Create EmbeddedPhotoPickerProvider
    // TODO: Hold EmbeddedPhotoPickerSession

    val context = LocalContext.current
    // TODO: AndroidView の外部でビュー参照を保持するために remember を使用する代わりに、AndroidView factory ラムダでビューを作成することをおすすめします。
    val embeddedPhotoPickerController = remember { EmbeddedPhotoPickerController(context, SurfaceView(context)) }
    LaunchedEffect(isExpanded) {
        Timber.d("LaunchedEffect: isExpanded=$isExpanded")
        embeddedPhotoPickerController.setExpanded(isExpanded)
    }

    AndroidView(
        factory = {
            Timber.d("factory: isExpanded=$isExpanded")
            embeddedPhotoPickerController.surfaceView
        },
        update = {
            Timber.d("update: isExpanded=$isExpanded")
            // width and height is not yet decided
            Timber.d("update: width=${it.width}, height=${it.height}")
//            embeddedPhotoPickerController.setExpanded(isExpanded)
        },
        onRelease = { surfaceView ->
            Timber.d("onRelease")
            embeddedPhotoPickerController.release()
            // TODO: Detach surfaceView from EmbeddedPhotoPickerProvider
            // SurfaceView#clearChildSurfacePackage() cannot be called in API 35
        },
        modifier = modifier
            .onPlaced {
                // onPlaced is called after onSizeChanged
                Timber.d("onPlaced: size=${it.size}, $it")
            }
            .onSizeChanged { size ->
                Timber.d("onSizeChanged: size=$size")
                // TODO: Open session or notifySizeChanged
                embeddedPhotoPickerController.notifySizeChanged(
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