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
import androidx.core.view.doOnLayout
import timber.log.Timber
import java.util.concurrent.Executors

@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
class EmbeddedPhotoPickerController(
    private val context: Context,
    private val embeddedPhotoPickerProvider: EmbeddedPhotoPickerProvider =
        EmbeddedPhotoPickerProviderFactory.create(context),
    private val onPhotoSelected: (List<Uri>, () -> Unit) -> Unit = { _, _ -> }
) {
    private var surfaceView: SurfaceView? = null
    private var session: EmbeddedPhotoPickerSession? = null

    fun notifySizeChanged(@Px widthPx: Int, @Px heightPx: Int) {
        session?.notifyResized(widthPx, heightPx) ?: {
            Timber.w("session is null")
        }
    }

    fun setExpanded(expanded: Boolean) {
        session?.notifyPhotoPickerExpanded(expanded) ?: {
            Timber.w("session is null")
        }
    }

    fun notifyVisibility(visible: Boolean) {
        session?.notifyVisibilityChanged(visible) ?: {
            Timber.w("session is null")
        }
    }

    fun attach(surfaceView: SurfaceView) {
        if (!openSession(surfaceView)) {
            // pending
            surfaceView.doOnLayout {
                openSession(surfaceView)
            }
        }
    }

    private fun openSession(surfaceView: SurfaceView): Boolean {
        Timber.d("openSession")
        val widthPx = surfaceView.width
        val heightPx = surfaceView.height
        if (widthPx == 0 || heightPx == 0) {
            Timber.w("width and height is not set. widthPx=$widthPx, heightPx=$heightPx")
            return false
        }

        // Maybe it's correct way to get hostToken. But InputTransferToken#getToken() is hidden
//        val hostToken = surfaceView.rootSurfaceControl.inputTransferToken.token

        // It's deprecated method, but there is no other way to open photo picker session
        val hostToken = surfaceView.hostToken
        if (hostToken == null) {
            Timber.w("hostToken is null")
            return false
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
        this.surfaceView = surfaceView
        return true
    }

    fun release(surfaceView: SurfaceView) {
        // TODO: Detach surfaceView from EmbeddedPhotoPickerProvider
        // SurfaceView#clearChildSurfacePackage() cannot be called in API 35
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
            surfaceView?.setChildSurfacePackage(session.surfacePackage)
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