package dev.tsuyosh.embedphotopickersample.ui.main

import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions
import android.view.SurfaceView
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import dev.tsuyosh.embedphotopickersample.R
import dev.tsuyosh.embedphotopickersample.ui.theme.EmbedPhotoPickerSampleTheme
import timber.log.Timber

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onTakeScreenshotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        MessageList(
            messages = uiState.messages,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Buttons(
            isOpened = uiState.isPhotoPickerOpened,
            isExpanded = uiState.isPhotoPickerExpanded,
            onOpenCloseButtonClick = { viewModel.togglePhotoPicker() },
            onExpandCollapseButtonClick = { viewModel.togglePhotoPickerExpanded() },
            onTakeScreenshotClick = onTakeScreenshotClick,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.isPhotoPickerOpened) {
            if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 15) {
                EmbeddedPhotoPicker(
                    isExpanded = uiState.isPhotoPickerExpanded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onPhotoSelected = { selectedPhotoUris, callback ->
                        viewModel.sendPhotoMessage(selectedPhotoUris, callback)
                    }
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

@Composable
fun MessageList(messages: List<MessageUiState>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        itemsIndexed(messages) { index, message ->
            ImageMessage(message = message)
            if (index < messages.lastIndex) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ImageMessage(message: MessageUiState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .widthIn(max = 300.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        ) {
            itemsIndexed(message.photoUris) { index, uri ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri.toString())
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                )
                if (index < message.photoUris.lastIndex) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
@Composable
fun EmbeddedPhotoPicker(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onPhotoSelected: (List<Uri>, () -> Unit) -> Unit = { _, _ -> },
) {
    // TODO: Hold EmbeddedPhotoPicker state outside of this composable to retain the photo picker state
    // TODO: Create EmbeddedPhotoPickerProvider
    // TODO: Hold EmbeddedPhotoPickerSession

    val context = LocalContext.current
    // TODO: AndroidView の外部でビュー参照を保持するために remember を使用する代わりに、AndroidView factory ラムダでビューを作成することをおすすめします。
    val embeddedPhotoPickerController = remember {
        val surfaceView = SurfaceView(context)
        surfaceView.id = R.id.embeddedPhotoPickerView
        EmbeddedPhotoPickerController(context, surfaceView, onPhotoSelected)
    }
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

@Preview(showBackground = true, widthDp = 360)
@Composable
fun MessagePreview() {
    val message = MessageUiState(
        photoUris = listOf(
            Uri.parse("https://example.com/image1.jpg"),
            Uri.parse("https://example.com/image2.jpg"),
            Uri.parse("https://example.com/image3.jpg"),
            Uri.parse("https://example.com/image4.jpg"),
            Uri.parse("https://example.com/image5.jpg"),
            Uri.parse("https://example.com/image6.jpg"),
        )
    )
    EmbedPhotoPickerSampleTheme {
        ImageMessage(message)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun MessageListPreview() {
    val messages = listOf(
        MessageUiState(
            photoUris = listOf(
                Uri.parse("https://example.com/image1.jpg"),
            )
        ),
        MessageUiState(
            photoUris = listOf(
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
            )
        ),
        MessageUiState(
            photoUris = listOf(
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
            )
        ),
        MessageUiState(
            photoUris = listOf(
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
            )
        ),
        MessageUiState(
            photoUris = listOf(
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
            )
        ),
        MessageUiState(
            photoUris = listOf(
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
            )
        ),
        MessageUiState(
            photoUris = listOf(
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
            )
        ),
        MessageUiState(
            photoUris = listOf(
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
                Uri.parse("https://example.com/image1.jpg"),
            )
        ),
    )
    EmbedPhotoPickerSampleTheme {
        MessageList(messages)
    }
}