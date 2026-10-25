package com.example.ui.screens

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.example.ui.theme.PequeBlue
import org.json.JSONArray
import org.json.JSONObject

/**
 * Puente JavaScript bidireccional entre el WebView (PWA) y el ViewModel nativo de Android.
 */
private class PequeSaludWebBridge(
    private val mainHandler: Handler,
    private val onRecordCreatedFromPwa: (HealthRecord) -> Unit,
    private val onChildCreatedFromPwa: (ChildProfile) -> Unit,
    private val onRequestSyncToPwa: () -> Unit
) {
    @JavascriptInterface
    fun onPwaRecordCreated(recordJson: String) {
        mainHandler.post {
            try {
                val obj = JSONObject(recordJson)
                val record = HealthRecord(
                    id = obj.optString("id", ""),
                    childId = obj.optString("childId", ""),
                    childName = obj.optString("childName", "Paciente"),
                    category = obj.optString("category", "CONSULTA"),
                    title = obj.optString("title", "Registro"),
                    subtitle = obj.optString("subtitle", ""),
                    dateText = obj.optString("dateText", "Hoy"),
                    status = obj.optString("status", "Activo"),
                    notes = obj.optString("notes", "")
                )
                onRecordCreatedFromPwa(record)
            } catch (_: Exception) {
            }
        }
    }

    @JavascriptInterface
    fun onPwaChildCreated(childJson: String) {
        mainHandler.post {
            try {
                val obj = JSONObject(childJson)
                val child = ChildProfile(
                    name = obj.optString("name", "Paciente"),
                    ageYears = obj.optLong("ageYears", 1L),
                    birthDate = obj.optString("birthDate", "Registrado"),
                    gender = obj.optString("gender", "Femenino"),
                    bloodType = obj.optString("bloodType", "O+"),
                    allergies = obj.optString("allergies", "Ninguna"),
                    pediatrician = obj.optString("pediatrician", "Dr. Alejandro López"),
                    emergencyContact = obj.optString("emergencyContact", "Por registrar"),
                    lastCheckupDate = obj.optString("lastCheckupDate", "Reciente"),
                    lastCheckupSummary = obj.optString("lastCheckupSummary", "Expediente activo"),
                    weightKg = obj.optDouble("weightKg", 11.0),
                    heightCm = obj.optDouble("heightCm", 82.0)
                )
                onChildCreatedFromPwa(child)
            } catch (_: Exception) {
            }
        }
    }

    @JavascriptInterface
    fun requestSyncFromAndroid() {
        mainHandler.post {
            onRequestSyncToPwa()
        }
    }
}

private fun buildAndroidStateSyncJson(
    children: List<ChildProfile>,
    records: List<HealthRecord>,
    selectedChildId: String,
    isPediatricianMode: Boolean
): String {
    val root = JSONObject()
    root.put("selectedChildId", selectedChildId)
    root.put("workspaceMode", if (isPediatricianMode) "PEDIATRICIAN" else "PARENT")

    val childrenArr = JSONArray()
    children.forEach { c ->
        val cObj = JSONObject()
        cObj.put("id", c.id)
        cObj.put("name", c.name)
        cObj.put("ageYears", c.ageYears)
        cObj.put("birthDate", c.birthDate)
        cObj.put("gender", c.gender)
        cObj.put("bloodType", c.bloodType)
        cObj.put("allergies", c.allergies)
        cObj.put("pediatrician", c.pediatrician)
        cObj.put("emergencyContact", c.emergencyContact)
        cObj.put("lastCheckupDate", c.lastCheckupDate)
        cObj.put("lastCheckupSummary", c.lastCheckupSummary)
        cObj.put("weightKg", c.weightKg)
        cObj.put("heightCm", c.heightCm)
        childrenArr.put(cObj)
    }
    root.put("children", childrenArr)

    val recordsArr = JSONArray()
    records.forEach { r ->
        val rObj = JSONObject()
        rObj.put("id", r.id)
        rObj.put("childId", r.childId)
        rObj.put("childName", r.childName)
        rObj.put("category", r.category)
        rObj.put("title", r.title)
        rObj.put("subtitle", r.subtitle)
        rObj.put("dateText", r.dateText)
        rObj.put("status", r.status)
        rObj.put("notes", r.notes)
        recordsArr.put(rObj)
    }
    root.put("records", recordsArr)

    return root.toString()
}

/**
 * Visor limpio y sin ruido visual de la versión PWA integrada.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HybridPwaArchitectureScreen(
    children: List<ChildProfile>,
    records: List<HealthRecord>,
    selectedChildId: String,
    isPediatricianMode: Boolean,
    onBack: () -> Unit,
    onAddChild: (ChildProfile) -> Unit,
    onAddRecord: (HealthRecord) -> Unit
) {
    BackHandler { onBack() }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val pushStateToWebView: (WebView?) -> Unit = { wv ->
        if (wv != null) {
            val payload = buildAndroidStateSyncJson(
                children = children,
                records = records,
                selectedChildId = selectedChildId,
                isPediatricianMode = isPediatricianMode
            )
            val escaped = JSONObject.quote(payload)
            wv.evaluateJavascript("window.receiveAndroidStateSync && window.receiveAndroidStateSync($escaped);", null)
        }
    }

    LaunchedEffect(children, records, selectedChildId, isPediatricianMode) {
        pushStateToWebView(webViewRef)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("hybrid_pwa_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "PequeSalud Web (PWA)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { pushStateToWebView(webViewRef) },
                    modifier = Modifier.testTag("hybrid_pwa_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sincronizar",
                        tint = PequeBlue
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val mainHandler = remember { Handler(Looper.getMainLooper()) }
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("hybrid_pwa_webview"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.mediaPlaybackRequiresUserGesture = false

                        addJavascriptInterface(
                            PequeSaludWebBridge(
                                mainHandler = mainHandler,
                                onRecordCreatedFromPwa = { newRec -> onAddRecord(newRec) },
                                onChildCreatedFromPwa = { newChild -> onAddChild(newChild) },
                                onRequestSyncToPwa = { pushStateToWebView(this) }
                            ),
                            "PequeSaludAndroidBridge"
                        )

                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                pushStateToWebView(view)
                            }
                        }

                        val modeParam = if (isPediatricianMode) "pediatrician" else "parent"
                        loadUrl("file:///android_asset/pwa/index.html?mode=$modeParam")
                        webViewRef = this
                    }
                },
                update = { view ->
                    webViewRef = view
                }
            )

            DisposableEffect(Unit) {
                onDispose {
                    webViewRef?.destroy()
                    webViewRef = null
                }
            }
        }
    }
}
