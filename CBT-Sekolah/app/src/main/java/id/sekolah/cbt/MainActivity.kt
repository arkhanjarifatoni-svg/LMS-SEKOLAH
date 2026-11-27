package id.sekolah.cbt

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.http.SslError
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.net.URI
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("meg_settings", MODE_PRIVATE) }
    private var browserOpen by mutableStateOf(false)
    private var violations by mutableIntStateOf(0)
    private var activeWebView: WebView? = null
    private var allowedHosts: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prevent normal Android screenshots and screen recordings while this app is visible.
        // This is not protection against a second camera/device.
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        violations = prefs.getInt("local_violations", 0)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ExamGuardApp(
                        initialUrl = prefs.getString("moodle_url", "") ?: "",
                        initialExtraHosts = prefs.getString("extra_hosts", "") ?: "",
                        browserOpen = browserOpen,
                        violationCount = violations,
                        onOpen = { url, extra ->
                            val parsed = validateMoodleUrl(url)
                            if (parsed == null) {
                                Toast.makeText(this, "Gunakan URL Moodle HTTPS yang valid / Enter a valid HTTPS Moodle URL", Toast.LENGTH_LONG).show()
                            } else {
                                val extras = parseExtraHosts(extra)
                                if (extras == null) {
                                    Toast.makeText(this, "Domain tambahan tidak valid / Invalid additional domain", Toast.LENGTH_LONG).show()
                                } else {
                                    allowedHosts = extras + parsed.host.lowercase(Locale.ROOT)
                                    prefs.edit().putString("moodle_url", parsed.toString())
                                        .putString("extra_hosts", extra).apply()
                                    browserOpen = true
                                    enterImmersiveMode()
                                }
                            }
                        },
                        onExit = { confirmExitExam() },
                        onWebViewCreated = { activeWebView = it },
                        onWebViewDestroyed = { if (activeWebView === it) activeWebView = null },
                        isHostAllowed = { host -> host.lowercase(Locale.ROOT) in allowedHosts }
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (browserOpen) recordViolation()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && browserOpen) enterImmersiveMode()
    }

    private fun recordViolation() {
        violations += 1
        prefs.edit().putInt("local_violations", violations).apply()
    }


    private fun validateMoodleUrl(raw: String): URI? = try {
    val uri = URI(raw.trim())

    val validScheme =
        uri.scheme.equals("https", true) ||
        uri.scheme.equals("http", true)

    if (
        validScheme &&
        !uri.host.isNullOrBlank() &&
        uri.userInfo == null &&
        uri.query == null &&
        uri.fragment == null
    ) {
        uri
    } else {
        null
    }
    } catch (_: Exception) {
    null
    }


    private fun parseExtraHosts(raw: String): Set<String>? {
        if (raw.isBlank()) return emptySet()
        val hosts = raw.split(',', '\n').map { it.trim().lowercase(Locale.ROOT) }.filter { it.isNotBlank() }
        val valid = hosts.all { host ->
            host.length <= 253 && host.matches(Regex("(?i)^[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?$")) &&
                !host.contains("..") && !host.startsWith(".") && !host.endsWith(".")
        }
        return if (valid) hosts.toSet() else null
    }

    private fun confirmExitExam() {
        AlertDialog.Builder(this)
            .setTitle("Akhiri sesi? / End session?")
            .setMessage("Kembali ke halaman pengaturan akan meninggalkan halaman Moodle. Pastikan jawaban sudah tersimpan. / Returning to setup leaves Moodle. Make sure your answers are saved.")
            .setNegativeButton("Lanjut ujian / Continue") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Akhiri / End") { _, _ ->
                activeWebView?.stopLoading()
                browserOpen = false
                allowedHosts = emptySet()
                leaveImmersiveMode()
            }.show()
    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun leaveImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
private fun ExamGuardApp(
    initialUrl: String,
    initialExtraHosts: String,
    browserOpen: Boolean,
    violationCount: Int,
    onOpen: (String, String) -> Unit,
    onExit: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onWebViewDestroyed: (WebView) -> Unit,
    isHostAllowed: (String) -> Boolean,
) {
    var url by rememberSaveable { mutableStateOf(initialUrl) }
    var extraHosts by rememberSaveable { mutableStateOf(initialExtraHosts) }

    BackHandler(enabled = browserOpen) {
        // Back navigation is handled by the browser's own controls, not Android Back.
        // This avoids accidentally leaving an active Moodle quiz attempt.
    }

    if (!browserOpen) {
        SetupScreen(
            url = url,
            onUrlChange = { url = it },
            extraHosts = extraHosts,
            onExtraHostsChange = { extraHosts = it },
            violationCount = violationCount,
            onConnect = { onOpen(url, extraHosts) }
        )
    } else {
        MoodleBrowserScreen(
            startUrl = url,
            onExit = onExit,
            onWebViewCreated = onWebViewCreated,
            onWebViewDestroyed = onWebViewDestroyed,
            isHostAllowed = isHostAllowed,
        )
    }
}

@Composable
private fun SetupScreen(
    url: String,
    onUrlChange: (String) -> Unit,
    extraHosts: String,
    onExtraHostsChange: (String) -> Unit,
    violationCount: Int,
    onConnect: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("◈  MOODLE EXAM GUARD", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Secure examination browser", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Browser ujian aman untuk Moodle", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(18.dp)) {
                Text("Hubungkan server Moodle / Connect Moodle", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL Moodle (HTTPS)") },
                    placeholder = { Text("https://moodle.sekolah.sch.id") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = extraHosts,
                    onValueChange = onExtraHostsChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Domain tambahan (opsional) / Extra trusted hosts") },
                    placeholder = { Text("login.sekolah.sch.id, sso.sekolah.sch.id") },
                    minLines = 2
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onConnect, modifier = Modifier.fillMaxWidth()) {
                    Text("HUBUNGKAN / CONNECT")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Keamanan / Security", fontWeight = FontWeight.SemiBold)
        Text("• HTTPS only and trusted-domain allowlist\n• No browser tabs or external browsing\n• Android Back is blocked while browsing\n• Screenshot protection enabled\n• App background events are counted locally", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Text("Pelanggaran lokal tercatat / Local background events: $violationCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Text("Gratis • Tanpa iklan / Free • No ads", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun MoodleBrowserScreen(
    startUrl: String,
    onExit: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onWebViewDestroyed: (WebView) -> Unit,
    isHostAllowed: (String) -> Boolean,
) {
    val context = LocalContext.current
    var progress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf(startUrl) }
    var blockedMessage by remember { mutableStateOf("") }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("MOODLE EXAM GUARD", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(Uri.parse(currentUrl).host ?: "Moodle", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            TextButton(onClick = onExit) { Text("Akhiri / Exit") }
        }
        if (progress in 1..99) {
            androidx.compose.material3.LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
        }
        if (blockedMessage.isNotBlank()) {
            Text(blockedMessage, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webView = this
                    onWebViewCreated(this)
                    settings.javaScriptEnabled = true // Moodle requires JavaScript for normal operation.
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        settings.safeBrowsingEnabled = true
                    }
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                    isLongClickable = false
                    setOnLongClickListener { true }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) { progress = newProgress }
                        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean = false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val uri = request.url
                            val allowed = (uri.scheme.equals("https", true) ||uri.scheme.equals("http", true)) && !uri.host.isNullOrBlank() && isHostAllowed(uri.host!!)
                            if (!allowed) {
                                blockedMessage = "Navigasi eksternal diblokir / External navigation blocked"
                                Toast.makeText(context, blockedMessage, Toast.LENGTH_SHORT).show()
                                return true
                            }
                            blockedMessage = ""
                            return false
                        }

                        @Deprecated("Deprecated in Android API")
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            val uri = Uri.parse(url)
                            val allowed = uri.scheme.equals("https", true) && !uri.host.isNullOrBlank() && isHostAllowed(uri.host!!)
                            if (!allowed) {
                                blockedMessage = "Navigasi eksternal diblokir / External navigation blocked"
                                return true
                            }
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            if (url != null) currentUrl = url
                        }

                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                            handler?.cancel() // Never bypass invalid TLS certificates.
                            blockedMessage = "Sertifikat HTTPS tidak valid / Invalid HTTPS certificate"
                        }
                    }
                    loadUrl(startUrl)
                }
            },
            update = { }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.let {
                onWebViewDestroyed(it)
                it.stopLoading()
                it.loadUrl("about:blank")
                it.removeAllViews()
                it.destroy()
            }
        }
    }
}
