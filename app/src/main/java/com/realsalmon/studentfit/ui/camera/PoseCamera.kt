package com.realsalmon.studentfit.ui.camera

import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.realsalmon.studentfit.domain.pose.BodyPoint
import com.realsalmon.studentfit.domain.pose.PosePoint
import com.realsalmon.studentfit.domain.pose.PoseSample
import java.util.concurrent.Executors

/** 한 프레임의 인식 결과. 좌표는 회전을 반영한 이미지 기준이에요. */
data class PoseFrame(val sample: PoseSample, val imageWidth: Int, val imageHeight: Int)

private val landmarkMap = mapOf(
    PoseLandmark.NOSE to BodyPoint.NOSE,
    PoseLandmark.LEFT_EAR to BodyPoint.LEFT_EAR,
    PoseLandmark.RIGHT_EAR to BodyPoint.RIGHT_EAR,
    PoseLandmark.LEFT_SHOULDER to BodyPoint.LEFT_SHOULDER,
    PoseLandmark.RIGHT_SHOULDER to BodyPoint.RIGHT_SHOULDER,
    PoseLandmark.LEFT_HIP to BodyPoint.LEFT_HIP,
    PoseLandmark.RIGHT_HIP to BodyPoint.RIGHT_HIP,
    PoseLandmark.LEFT_KNEE to BodyPoint.LEFT_KNEE,
    PoseLandmark.RIGHT_KNEE to BodyPoint.RIGHT_KNEE,
    PoseLandmark.LEFT_ANKLE to BodyPoint.LEFT_ANKLE,
    PoseLandmark.RIGHT_ANKLE to BodyPoint.RIGHT_ANKLE,
)

/**
 * 카메라 미리보기 + ML Kit 포즈 인식. 영상은 저장하지 않고 폰 안에서만 분석해요.
 */
@Composable
fun PoseCamera(
    frontCamera: Boolean,
    onFrame: (PoseFrame) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnFrame = rememberUpdatedState(onFrame)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val detector = remember {
        PoseDetection.getClient(
            PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build(),
        )
    }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(frontCamera, lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        providerFuture.addListener({
            val p = providerFuture.get()
            provider = p
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            @Suppress("DEPRECATION")
            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(720, 1280))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor) { proxy -> analyze(proxy, detector) { latestOnFrame.value(it) } }
            val selector = if (frontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            runCatching {
                p.unbindAll()
                p.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose { provider?.unbindAll() }
    }

    DisposableEffect(Unit) {
        onDispose {
            detector.close()
            executor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

@OptIn(ExperimentalGetImage::class)
private fun analyze(
    proxy: ImageProxy,
    detector: com.google.mlkit.vision.pose.PoseDetector,
    onFrame: (PoseFrame) -> Unit,
) {
    val media = proxy.image
    if (media == null) {
        proxy.close(); return
    }
    val rotation = proxy.imageInfo.rotationDegrees
    val input = InputImage.fromMediaImage(media, rotation)
    val (w, h) = if (rotation == 90 || rotation == 270) proxy.height to proxy.width else proxy.width to proxy.height
    detector.process(input)
        .addOnSuccessListener { pose ->
            val sample = landmarkMap.mapNotNull { (type, bp) ->
                pose.getPoseLandmark(type)?.let { lm ->
                    bp to PosePoint(lm.position.x, lm.position.y, lm.inFrameLikelihood)
                }
            }.toMap()
            onFrame(PoseFrame(sample, w, h))
        }
        .addOnCompleteListener { proxy.close() }
}
