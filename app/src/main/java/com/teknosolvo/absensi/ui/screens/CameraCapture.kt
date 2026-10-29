package com.teknosolvo.absensi.ui.screens

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/** Preview kamera (default kamera depan) dengan tombol ambil foto. */
@Composable
fun CameraCapture(
    onCaptured: (File) -> Unit,
    onFailure: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var capturing by remember { mutableStateOf(false) }

    LaunchedEffect(lensFacing) {
        try {
            val provider = context.cameraProvider()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
        } catch (e: Exception) {
            onFailure("Kamera tidak bisa dibuka: ${e.message}")
        }
    }
    DisposableEffect(Unit) {
        onDispose { runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() } }
    }

    Box(modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            }) { Icon(Icons.Default.Refresh, contentDescription = "Ganti kamera") }
            Button(
                enabled = !capturing,
                onClick = {
                    capturing = true
                    val dir = File(context.filesDir, "photos").apply { mkdirs() }
                    val file = File(dir, "absen_${System.currentTimeMillis()}.jpg")
                    val metadata = ImageCapture.Metadata().apply {
                        // Selfie disimpan seperti yang terlihat di preview (cermin).
                        isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                    }
                    val options = ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build()
                    imageCapture.takePicture(
                        options,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                capturing = false
                                onCaptured(file)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                capturing = false
                                onFailure("Gagal mengambil foto: ${exception.message}")
                            }
                        },
                    )
                },
            ) { Text("Ambil Foto") }
        }
    }
}

private suspend fun Context.cameraProvider(): ProcessCameraProvider = suspendCoroutine { cont ->
    val future = ProcessCameraProvider.getInstance(this)
    future.addListener({
        try {
            cont.resume(future.get())
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }, ContextCompat.getMainExecutor(this))
}
