package dev.tsuyosh.embedphotopickersample

import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.ViewModelInitializer
import dev.tsuyosh.embedphotopickersample.ui.main.MainScreen
import dev.tsuyosh.embedphotopickersample.ui.main.MainScreenViewModel
import dev.tsuyosh.embedphotopickersample.ui.main.SaveImageUseCase
import dev.tsuyosh.embedphotopickersample.ui.main.TakeScreenShotUseCase
import dev.tsuyosh.embedphotopickersample.ui.theme.EmbedPhotoPickerSampleTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainScreenViewModel by viewModels(
        factoryProducer = {
            ViewModelProvider.Factory.from(
                ViewModelInitializer(
                    MainScreenViewModel::class,
                    initializer = {
                        MainScreenViewModel(
                            TakeScreenShotUseCase(contentResolver),
                            SaveImageUseCase(applicationContext)
                        )
                    }
                )
            )
        }
    )

    private lateinit var composeView: ComposeView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        composeView = ComposeView(this).apply {
            setContent {
                EmbedPhotoPickerSampleTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MainScreen(
                            viewModel = viewModel,
                            onTakeScreenshotClick = {
                                val surfaceView = findViewById<SurfaceView>(R.id.embeddedPhotoPickerView)
                                viewModel.takeScreenShot(surfaceView)
                            },
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
}
