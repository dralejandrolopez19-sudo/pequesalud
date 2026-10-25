package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeOrange
import com.example.ui.theme.PequePurple
import org.json.JSONArray
import org.json.JSONObject

/**
 * Puente JavaScript bidireccional entre el WebView Híbrido (PWA) y el ViewModel nativo de Android.
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
                    title = obj.optString("title", "Registro desde PWA"),
                    subtitle = obj.optString("subtitle", "Sincronizado desde Arquitectura Mixta PWA"),
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
                    birthDate = obj.optString("birthDate", "Registrado en PWA"),
                    gender = obj.optString("gender", "Femenino"),
                    bloodType = obj.optString("bloodType", "O+"),
                    allergies = obj.optString("allergies", "Ninguna"),
                    pediatrician = obj.optString("pediatrician", "Dr. Alejandro López"),
                    emergencyContact = obj.optString("emergencyContact", "Por registrar"),
                    lastCheckupDate = obj.optString("lastCheckupDate", "Recién registrado"),
                    lastCheckupSummary = obj.optString("lastCheckupSummary", "Sincronizado desde PWA"),
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
 * Tarjeta compacta de acceso rápido a la Arquitectura Mixta (Android Nativo + PWA GitHub Pages).
 */
@Composable
fun HybridPwaQuickBannerCard(
    onOpenHybridPwaHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenHybridPwaHub() }
            .testTag("open_hybrid_pwa_hub_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PequeBlue.copy(alpha = 0.10f),
                            PequeGreen.copy(alpha = 0.10f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PequeBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Arquitectura Mixta PWA",
                        tint = PequeBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Arquitectura Mixta: Android + PWA",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Lista para GitHub Pages (/docs) • Misma base Firebase + Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = PequeBlue
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Abrir PWA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Pantalla completa del Hub de Arquitectura Mixta (Visor Híbrido PWA en vivo + Despliegue GitHub Pages).
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

    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Visor PWA en Vivo, 1 = Guía GitHub Pages y Sincronización
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var syncStatusText by remember { mutableStateOf("Sincronizado en tiempo real con Firebase + Android") }

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
        // Barra superior del Hub de Arquitectura Mixta
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PequeGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ARQUITECTURA MIXTA (ANDROID + PWA)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PequeBlue
                                )
                            }
                            Text(
                                text = "PWA Instalable y GitHub Pages",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            pushStateToWebView(webViewRef)
                            syncStatusText = "Sincronizado (${children.size} hijos, ${records.size} registros)"
                        },
                        modifier = Modifier.testTag("hybrid_pwa_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar estado con PWA",
                            tint = PequeBlue
                        )
                    }
                }

                // Selector de pestaña: Visor Interactivo PWA vs Configuración GitHub Pages
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        label = { Text("Visor PWA Interactivo (Padres + Pediatra)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PequeBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hybrid_tab_live_pwa")
                    )
                    FilterChip(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        label = { Text("GitHub Pages (/docs)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PequeGreen,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hybrid_tab_github_guide")
                    )
                }
            }
        }

        if (selectedSection == 0) {
            // Visor Híbrido en Vivo cargando /app/src/main/assets/pwa/index.html (idéntico a /docs/index.html)
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
                                    onRecordCreatedFromPwa = { newRec ->
                                        onAddRecord(newRec)
                                    },
                                    onChildCreatedFromPwa = { newChild ->
                                        onAddChild(newChild)
                                    },
                                    onRequestSyncToPwa = {
                                        pushStateToWebView(this)
                                    }
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
        } else {
            // Pestaña de Diagnóstico de Arquitectura Mixta y Guía de GitHub Pages
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = PequeGreen,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Estado de la Arquitectura Mixta",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = syncStatusText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PequeGreen
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            ArchitectureStatusRow(
                                icon = Icons.Default.InstallMobile,
                                title = "Manifiesto Web Instalable (manifest.webmanifest)",
                                subtitle = "Configurado en /docs/manifest.webmanifest con iconos vectoriales 192x192 y 512x512 y accesos directos a ambos escritorios."
                            )
                            ArchitectureStatusRow(
                                icon = Icons.Default.OfflinePin,
                                title = "Service Worker y Modo Offline (/docs/sw.js)",
                                subtitle = "Estrategia Stale-While-Revalidate + respaldo automático en localStorage para funcionar con o sin internet."
                            )
                            ArchitectureStatusRow(
                                icon = Icons.Default.Sync,
                                title = "Base Firebase Firestore Compartida en Tiempo Real",
                                subtitle = "Proyecto gen-lang-client-0132354751 • Sincroniza /users/{uid}/children y /users/{uid}/records entre Android y la PWA."
                            )
                            ArchitectureStatusRow(
                                icon = Icons.Default.CheckCircle,
                                title = "Ambos Escritorios + Curvas OMS (P3, P15, P50, P85, P97)",
                                subtitle = "Incluye Escritorio de Padres, Estación Clínica del Pediatra (con PIN 1234), Calculadora mg/kg, Vacunas PAI y Alertas Push."
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Cómo activar tu PWA en GitHub Pages",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "El proyecto ya contiene la carpeta estática /docs y el flujo automático .github/workflows/deploy-pwa-pages.yml listos para publicar:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            StepInstructionItem(
                                stepNumber = "1",
                                title = "Exportar a tu repositorio de GitHub",
                                description = "Usa la opción 'Push to GitHub' o 'Export ZIP' en el menú de AI Studio para subir el código a tu repositorio."
                            )
                            StepInstructionItem(
                                stepNumber = "2",
                                title = "Seleccionar carpeta /docs en GitHub Pages",
                                description = "En tu repositorio de GitHub ve a Settings → Pages → Build and deployment. Elige 'Deploy from a branch', rama 'main' y carpeta '/docs' (o elige 'GitHub Actions')."
                            )
                            StepInstructionItem(
                                stepNumber = "3",
                                title = "Autorizar tu dominio github.io en Firebase Auth",
                                description = "Para iniciar sesión con Google desde la web, agrega '<tu-usuario>.github.io' en Firebase Console → Authentication → Settings → Authorized domains."
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Estructura incluida lista para producción:\n" +
                                        "├── app/ (App Android Nativa Kotlin + Compose)\n" +
                                        "├── docs/ (Carpeta PWA para GitHub Pages)\n" +
                                        "│   ├── index.html\n" +
                                        "│   ├── styles.css\n" +
                                        "│   ├── app.js (Curvas OMS + Firebase + Offline)\n" +
                                        "│   ├── manifest.webmanifest\n" +
                                        "│   ├── sw.js (Service Worker Offline)\n" +
                                        "│   └── .nojekyll\n" +
                                        "└── .github/workflows/deploy-pwa-pages.yml",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val summary = "PequeSalud PWA GitHub Pages: Rama 'main' -> Carpeta '/docs' (incluye index.html, manifest.webmanifest, sw.js y conexión Firebase gen-lang-client-0132354751)."
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Configuración GitHub Pages", summary))
                                        syncStatusText = "Instrucciones copiadas al portapapeles"
                                    },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copiar guía")
                                }

                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                "PequeSalud — Arquitectura Mixta (Android + PWA GitHub Pages)"
                                            )
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "PequeSalud cuenta con Arquitectura Mixta: App Android nativa + PWA instalable en /docs para GitHub Pages con Escritorio de Padres, Estación Clínica del Pediatra, Curvas OMS (P3-P97) y sincronización Firebase."
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartir Arquitectura Mixta"))
                                    },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Compartir", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchitectureStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PequeBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PequeBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepInstructionItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PequeGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
