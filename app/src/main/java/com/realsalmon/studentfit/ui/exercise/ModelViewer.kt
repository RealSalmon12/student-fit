package com.realsalmon.studentfit.ui.exercise

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.PixelCopy
import android.view.View
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
import android.webkit.WebResourceError
import android.util.Log
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.delay
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
import androidx.compose.ui.graphics.toArgb
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
    // 투명 WebView 위의 3D가 안 보이는 기기가 있어서, 배경을 불투명한 색으로 칠해요.
    val bg = MaterialTheme.colorScheme.surfaceContainerLow
    val viewBg = MaterialTheme.colorScheme.surfaceContainer
    val bgHex = String.format(Locale.US, "#%06X", bg.toArgb() and 0xFFFFFF)
    var ready by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var lastProbe by remember { mutableStateOf("") }

    val webView = remember {
        WebView(context).apply {
            // 페이지(흰색)와 살짝 다른 색: 화면 캡처로 페이지가 붙었는지 구분할 수 있어요.
            setBackgroundColor(viewBg.toArgb())
            settings.javaScriptEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            val main = Handler(Looper.getMainLooper())
            addJavascriptInterface(
                ViewerBridge(
                    readyCb = { main.post { ready = true } },
                    loadedCb = { main.post { loaded = true } },
                    errorCb = { msg -> main.post { error = msg } },
                ),
                "Android",
            )
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                    Log.d("StudentFit3D", "${m.messageLevel()} ${m.message()} (${m.sourceId()}:${m.lineNumber()})")
                    return true
                }
            }
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
                        WebResourceResponse(
                            mime, if (mime.startsWith("text/")) "utf-8" else null, 200, "OK",
                            mapOf("Access-Control-Allow-Origin" to "*", "Cache-Control" to "no-cache"),
                            view.context.assets.open(path),
                        )
                    } catch (e: Exception) {
                        Log.w("StudentFit3D", "asset $path", e)
                        null
                    }
                }

                override fun onReceivedError(view: WebView, request: WebResourceRequest, err: WebResourceError) {
                    if (request.isForMainFrame) main.post { error = "페이지 오류 ${err.errorCode}" }
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
            // 화면에 붙은 뒤에 불러와요(안 붙은 상태로 열면 그림이 안 나오는 기기가 있어요).
            post { loadUrl(VIEWER_URL) }
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

    // 다리(JS → 앱 알림)가 안 통하는 기기도 있어서, 준비됐는지 직접 물어보기도 해요.
    LaunchedEffect(webView) {
        var waited = 0
        while (!ready && error == null && waited < 20_000) {
            delay(400)
            waited += 400
            webView.evaluateJavascript("(window.SF_READY===true)+'|'+(window.SF_ERROR||'')") { r ->
                val v = r?.trim('"').orEmpty()
                if (v.startsWith("true")) ready = true
                val e = v.substringAfter('|', "")
                if (e.isNotBlank()) error = e
            }
        }
        if (!ready && error == null) error = "시간 초과"
    }
    LaunchedEffect(ready, json) {
        // 불러오기 요청 뒤에도 그려졌다는 소식이 없으면 확인해요.
        if (!ready || json == null) return@LaunchedEffect
        delay(1500)
        webView.evaluateJavascript("(window.SF_LOADED===true)+''") { r -> if (r?.contains("true") == true) loaded = true }
        delay(6000)
        if (!loaded && error == null) {
            webView.evaluateJavascript("(window.SF_LOADED===true)+''") { r ->
                if (r?.contains("true") == true) loaded = true else error = "동작을 그리지 못했어요"
            }
        }
    }
    // 실제로 사람이 그려졌는지 화면 픽셀을 확인해요. 계속 비어 있으면 간단한 모형으로 바꿔요.
    LaunchedEffect(loaded) {
        if (!loaded) return@LaunchedEffect
        var empty = 0
        repeat(6) {
            delay(1200)
            if (error != null) return@LaunchedEffect
            webView.evaluateJavascript("(function(){try{return SF.probe()}catch(e){return 'ERR '+e}})()") { r ->
                val v = (r ?: "").trim('"').replace("\\\"", "\"")
                Log.d("StudentFit3D", "probe $v")
                lastProbe = v.take(70)
                val drawn = Regex("\"drawn\":([0-9.eE-]+)").find(v)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                if (drawn < 0.005) {
                    empty++
                    if (empty >= 3 && error == null) error = "빈 화면 " + v.take(80)
                } else {
                    empty = 0
                }
            }
        }
    }

    // 실제 화면에 사람이 보이는지(가운데 부분 픽셀)도 확인해요. 계속 비어 있으면 간단한 모형으로 바꿔요.
    LaunchedEffect(loaded) {
        if (!loaded) return@LaunchedEffect
        delay(2500)
        var blank = 0
        repeat(4) {
            if (error != null) return@LaunchedEffect
            when (screenBlank(webView)) {
                true -> blank++
                false -> return@LaunchedEffect
                null -> {}
            }
            if (blank >= 2) {
                error = "화면에 안 그려짐 · $lastProbe"
                return@LaunchedEffect
            }
            delay(1500)
        }
    }

    fun js(code: String) = webView.evaluateJavascript(code, null)
    fun f(v: Float) = String.format(Locale.US, "%.3f", v)

    LaunchedEffect(ready, json, focusUpper) {
        val data = json
        if (ready && data != null) {
            val opts = "{plumb:$plumbLine,focusUpper:$focusUpper${if (time != null) ",time:${f(time)}" else ""}}"
            js(
                "try{SF.setBg('$bgHex');SF.setView(${f(view.yaw)},${f(view.elevation)},true);SF.load($data,$opts);window.SF_LOADED=true;" +
                    "window.Android&&Android.onLoaded&&Android.onLoaded()}catch(e){window.SF_ERROR=String(e);window.Android&&Android.onError&&Android.onError(String(e))}",
            )
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
        val failed = error != null
        if (!failed) {
            AndroidView(factory = { webView }, modifier = Modifier.matchParentSize())
        } else {
            // 3D를 못 띄우는 경우에도 동작은 볼 수 있게 간단한 모형으로 대신 보여줘요.
            SkeletonViewer(
                motion, skeletonColors(), Modifier.matchParentSize(),
                view = view, focusUpper = focusUpper, playing = playing, speed = speed, time = time,
            )
        }
        AnimatedVisibility(!loaded && !failed, Modifier.align(Alignment.Center), exit = fadeOut()) {
            CircularProgressIndicator(Modifier.size(36.dp))
        }
        error?.let {
            Text(
                "3D 모델을 띄우지 못해 간단한 모형으로 보여줘요 ($it)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            )
        }
    }
}

/** JS → 앱 알림. WebView가 접근할 수 있게 공개 클래스로 둬요(익명 객체는 기기에 따라 호출이 막혀요). */
class ViewerBridge(
    private val readyCb: () -> Unit,
    private val loadedCb: () -> Unit,
    private val errorCb: (String) -> Unit,
) {
    @JavascriptInterface
    fun onReady() { readyCb() }

    @JavascriptInterface
    fun onLoaded() { loadedCb() }

    @JavascriptInterface
    fun onError(msg: String) { errorCb(msg) }

    @JavascriptInterface
    fun onYaw(yaw: Double) { }
}

private fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** 화면에 실제로 그려진 3D 영역 가운데를 찍어 보고, 한 가지 색뿐이면 true(비어 있음). 확인 못 하면 null. */
private suspend fun screenBlank(view: View): Boolean? = suspendCancellableCoroutine { cont ->
    fun done(v: Boolean?) { if (cont.isActive) cont.resume(v) }
    val act = view.context.findActivity()
    if (act == null || !view.isAttachedToWindow || view.width < 8 || view.height < 8) { done(null); return@suspendCancellableCoroutine }
    val loc = IntArray(2)
    view.getLocationInWindow(loc)
    val r = Rect(
        loc[0] + view.width / 4, loc[1] + view.height * 15 / 100,
        loc[0] + view.width * 3 / 4, loc[1] + view.height * 80 / 100,
    )
    val bmp = Bitmap.createBitmap(48, 64, Bitmap.Config.ARGB_8888)
    try {
        PixelCopy.request(act.window, r, bmp, { res ->
            if (res != PixelCopy.SUCCESS) { done(null); return@request }
            val px = IntArray(bmp.width * bmp.height)
            bmp.getPixels(px, 0, bmp.width, 0, 0, bmp.width, bmp.height)
            val ref = px[0]
            var diff = 0
            for (p in px) {
                val d = abs(((p shr 16) and 255) - ((ref shr 16) and 255)) +
                    abs(((p shr 8) and 255) - ((ref shr 8) and 255)) + abs((p and 255) - (ref and 255))
                if (d > 40) diff++
            }
            Log.d("StudentFit3D", "screen diff $diff/${px.size}")
            done(diff < px.size / 100)
        }, Handler(Looper.getMainLooper()))
    } catch (e: Exception) {
        Log.w("StudentFit3D", "pixelcopy", e)
        done(null)
    }
}
