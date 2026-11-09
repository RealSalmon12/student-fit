package com.realsalmon.studentfit.ui.exercise

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.realsalmon.studentfit.domain.motion.Motion
import com.realsalmon.studentfit.domain.motion.MotionExport
import com.realsalmon.studentfit.domain.motion.ViewAngle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

private const val HOST = "appassets.androidplatform.net"
private const val VIEWER_URL = "https://$HOST/viewer/index.html"

/**
 * 실제 사람 3D 모델로 운동 동작을 보여줘요.
 * 앱 안의 assets/viewer(three.js + 사람 모델)를 WebView로 띄우고, 동작 데이터를 넘겨요.
 * 인터넷은 쓰지 않아요.
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun ModelViewer(
    motion: Motion,
    modifier: Modifier = Modifier,
    view: ViewAngle = ViewAngle.THREE_QUARTER,
    focusUpper: Boolean = false,
    playing: Boolean = true,
    speed: Float = 1f,
    time: Float? = null,
    plumbLine: Boolean = true,
    /** 값이 바뀌면 같은 시점 버튼을 다시 눌러도 그 방향으로 돌아가요. */
    viewKey: Int = 0,
) {
    val context = LocalContext.current
    var ready by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val webView = remember {
        WebView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            val main = Handler(Looper.getMainLooper())
            addJavascriptInterface(object {
                @JavascriptInterface
                fun onReady() {
                    main.post { ready = true }
                }

                @JavascriptInterface
                fun onError(msg: String) {
                    main.post { error = msg }
                }
            }, "Android")
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    val url = request.url
                    if (url.host != HOST) return null
                    val path = url.path?.trimStart('/') ?: return null
                    if (!path.startsWith("viewer/") || path.contains("..")) return null
                    val mime = when (path.substringAfterLast('.')) {
                        "html" -> "text/html"
                        "js" -> "text/javascript"
                        "glb" -> "model/gltf-binary"
                        else -> "application/octet-stream"
                    }
                    return try {
                        WebResourceResponse(mime, "utf-8", view.context.assets.open(path))
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            // 가로로 끌면 사람을 돌리고, 세로로 끌면 화면이 스크롤돼요.
            var downX = 0f
            var downY = 0f
            setOnTouchListener { v, e ->
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> { downX = e.x; downY = e.y }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = abs(e.x - downX)
                        val dy = abs(e.y - downY)
                        if (dx > 12 && dx > dy) v.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                false
            }
            loadUrl(VIEWER_URL)
        }
    }
    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)
            webView.destroy()
        }
    }

    // 동작 데이터(관절 위치)는 백그라운드에서 만들어요.
    val json by produceState<String?>(null, motion) {
        value = withContext(Dispatchers.Default) { MotionExport.toJson(motion) }
    }

    fun js(code: String) = webView.evaluateJavascript(code, null)
    fun f(v: Float) = String.format(Locale.US, "%.3f", v)

    LaunchedEffect(ready, json, focusUpper) {
        val data = json
        if (ready && data != null) {
            js("SF.setView(${f(view.yaw)},${f(view.elevation)},true);SF.load($data,{plumb:$plumbLine,focusUpper:$focusUpper${if (time != null) ",time:${f(time)}" else ""}})")
            loaded = true
        }
    }
    LaunchedEffect(ready, view, viewKey) {
        if (ready) js("SF.setView(${f(view.yaw)},${f(view.elevation)},false)")
    }
    LaunchedEffect(ready, playing, speed, time == null) {
        if (ready) js("SF.setPlaying(${playing && time == null});SF.setSpeed(${f(speed)})")
    }
    LaunchedEffect(ready, time) {
        if (ready && time != null) js("SF.setTime(${f(time)})")
    }

    Box(modifier) {
        AndroidView(factory = { webView }, modifier = Modifier.matchParentSize())
        AnimatedVisibility(!loaded && error == null, Modifier.align(Alignment.Center), exit = fadeOut()) {
            CircularProgressIndicator(Modifier.size(36.dp))
        }
        error?.let {
            Text(
                "3D 모델을 불러오지 못했어요",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
