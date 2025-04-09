package dev.tsuyosh.embedphotopickersample.ui.main

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.view.SurfaceView
import android.widget.photopicker.EmbeddedPhotoPickerClient
import android.widget.photopicker.EmbeddedPhotoPickerFeatureInfo
import android.widget.photopicker.EmbeddedPhotoPickerProvider
import android.widget.photopicker.EmbeddedPhotoPickerProviderFactory
import android.widget.photopicker.EmbeddedPhotoPickerSession
import androidx.annotation.Px
import androidx.annotation.RequiresExtension
import timber.log.Timber
import java.util.concurrent.Executors

@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
class EmbeddedPhotoPickerController(
    private val context: Context,
    val surfaceView: SurfaceView,
    private val onPhotoSelected: (List<Uri>, () -> Unit) -> Unit = { _, _ -> }
) {
    init {
        // It's necessary for touch and click events
        surfaceView.setZOrderOnTop(true)
    }

    private val embeddedPhotoPickerProvider: EmbeddedPhotoPickerProvider =
        EmbeddedPhotoPickerProviderFactory.create(context)

    private var session: EmbeddedPhotoPickerSession? = null

    fun notifySizeChanged(
        @Px widthPx: Int,
        @Px heightPx: Int
    ) {
        Timber.d("Opening session")
        if (session != null) {
            session?.notifyResized(widthPx, heightPx)
            return
        }

        // Maybe it's correct way to get hostToken. But InputTransferToken#getToken() is hidden
//        val hostToken = surfaceView.rootSurfaceControl.inputTransferToken.token

        // It's deprecated method, but there is no other way to open photo picker session
        val hostToken = surfaceView.hostToken
        if (hostToken == null) {
            Timber.w("hostToken is null")
            return
        }
        val themeNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val featureInfo = EmbeddedPhotoPickerFeatureInfo.Builder()
            // The default value is UI_MODE_NIGHT_UNDEFINED, but it doesn't work. So specify the value
            .setThemeNightMode(themeNightMode)
            .build()
        val clientExecutor = Executors.newSingleThreadExecutor()
        embeddedPhotoPickerProvider.openSession(
            hostToken,
            surfaceView.display.displayId,
            widthPx,
            heightPx,
            featureInfo,
            clientExecutor,
            EmbeddedPhotoPickerClientImpl() // callback
        )
    }

    fun setExpanded(expanded: Boolean) {
        session?.notifyPhotoPickerExpanded(expanded)
    }

    fun notifyVisibility(visible: Boolean) {
        session?.notifyVisibilityChanged(visible)
    }

    fun release() {
        session?.close()
        session = null
    }

    private inner class EmbeddedPhotoPickerClientImpl : EmbeddedPhotoPickerClient {
        private var selectedPhotoUris: List<Uri> = emptyList()

        override fun onSelectionComplete() {
            Timber.d("onSelectionComplete")
            val uris = selectedPhotoUris
            onPhotoSelected.invoke(uris) {
                session?.requestRevokeUriPermission(uris)
            }
            selectedPhotoUris = emptyList()
        }

        override fun onSessionError(throwable: Throwable) {
            Timber.e(throwable, "onSessionError")
        }

        override fun onSessionOpened(session: EmbeddedPhotoPickerSession) {
            Timber.d("onSessionOpened")
            surfaceView.setChildSurfacePackage(session.surfacePackage)
            this@EmbeddedPhotoPickerController.session = session
        }

        override fun onUriPermissionGranted(uris: MutableList<Uri>) {
            Timber.d("onUriPermissionGranted: uris=$uris")
            selectedPhotoUris = buildList {
                addAll(selectedPhotoUris)
                uris.forEach {
                    if (!selectedPhotoUris.contains(it)) {
                        add(it)
                    }
                }
            }
        }

        override fun onUriPermissionRevoked(uris: MutableList<Uri>) {
            Timber.d("onUriPermissionRevoked: uris=$uris")
            selectedPhotoUris = buildList {
                addAll(selectedPhotoUris)
                uris.forEach {
                    remove(it)
                }
            }
        }
    }
}