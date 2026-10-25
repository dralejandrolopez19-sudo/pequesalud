package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalInformation
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.ChildProfile
import com.example.data.ClinicalModule
import com.example.data.HealthRecord
import com.example.data.PediatricianProfile
import com.example.data.PequeSaludRepository
import com.example.data.WorkspaceMode
import com.example.data.configureAppCheck
import com.example.ui.DataUiState
import com.example.ui.PequeSaludViewModel
import com.example.ui.components.PediatricPushPermissionAndRealtimeObserver
import com.example.ui.screens.AddOrEditChildDialog
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AuthFlowRoot
import com.example.ui.screens.ChildProfileScreen
import com.example.ui.screens.ClinicalModuleScreen
import com.example.ui.screens.EmptyChildrenProfileState
import com.example.ui.screens.HybridPwaArchitectureScreen
import com.example.ui.screens.MoreAndFullStructureScreen
import com.example.ui.screens.ParentHomeDashboardScreen
import com.example.ui.screens.ParentHomeTrackerScreen
import com.example.ui.screens.ParentSimpleReadOnlyScreen
import com.example.ui.screens.PediatricianClinicalSuiteScreen
import com.example.ui.screens.PediatricianConsultationAndDoseScreen
import com.example.ui.screens.PediatricianGrowthAndVaccinesScreen
import com.example.ui.screens.PediatricianPinDialog
import com.example.ui.screens.PediatricianTriageAndPatientsScreen
import com.example.ui.screens.VaccinesScreen
import com.example.ui.screens.signOut
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val PREFS_NAME = "pequesalud_prefs"
private const val KEY_DARK_MODE = "dark_mode_enabled"
private const val KEY_PEDIATRICIAN_PIN = "pediatrician_pin"
private const val KEY_DOC_NAME = "pediatrician_name"
private const val KEY_DOC_LICENSE = "pediatrician_license"
private const val KEY_DOC_SPECIALTY = "pediatrician_specialty"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val initialDark = prefs.getBoolean(KEY_DARK_MODE, false)

        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(initialDark) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { enabled ->
                        isDarkTheme = enabled
                        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
                    }
                )
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

sealed interface SubScreenDestination {
    data object None : SubScreenDestination
    data class ChildDetail(val childId: String) : SubScreenDestination
    data object Vaccines : SubScreenDestination
    data class Module(val module: ClinicalModule) : SubScreenDestination
    data object AiAssistant : SubScreenDestination
    data object HybridPwaHub : SubScreenDestination
}

@Composable
fun AppNavigation(
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    auth: FirebaseAuth = Firebase.auth
) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthFlowRoot(
            onAuthSuccess = {}
        )
    } else {
        PequeSaludAuthenticatedApp(
            currentUserId = user.uid,
            userDisplayName = user.displayName?.substringBefore(" ")?.ifBlank { "Alejandro" } ?: "Alejandro",
            userEmail = user.email ?: "dr.alejandrolopez19@gmail.com",
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = onToggleDarkTheme
        )
    }
}

@Composable
fun PequeSaludAuthenticatedApp(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: PequeSaludViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                PequeSaludViewModel(PequeSaludRepository(db), currentUserId)
            }
        }
    )

    val childrenUiState by viewModel.childrenState.collectAsStateWithLifecycle()
    val recordsUiState by viewModel.recordsState.collectAsStateWithLifecycle()
    val selectedChildId by viewModel.selectedChildId.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage, actionError) {
        val msg = actionError ?: actionMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    // Estado de Doble Escritorio (Escritorio de Padres vs Estación Clínica del Pediatra)
    var workspaceMode by remember {
        val savedMode = prefs.getString("workspace_mode", WorkspaceMode.PARENT.name)
        mutableStateOf(
            if (savedMode == WorkspaceMode.PEDIATRICIAN.name) WorkspaceMode.PEDIATRICIAN
            else WorkspaceMode.PARENT
        )
    }
    var showPediatricianPinModal by remember { mutableStateOf(false) }
    var openPinModalOnConfigTab by remember { mutableStateOf(false) }
    var pediatricianPin by remember {
        mutableStateOf(prefs.getString(KEY_PEDIATRICIAN_PIN, "1234") ?: "1234")
    }
    var pediatricianProfile by remember {
        mutableStateOf(
            PediatricianProfile(
                name = prefs.getString(KEY_DOC_NAME, "Dr. Alejandro López") ?: "Dr. Alejandro López",
                licenseId = prefs.getString(KEY_DOC_LICENSE, "Exequátur 482-19 • CMD 19402") ?: "Exequátur 482-19 • CMD 19402",
                specialty = prefs.getString(KEY_DOC_SPECIALTY, "Pediatría y Puericultura") ?: "Pediatría y Puericultura"
            )
        )
    }

    var bottomTab by remember { mutableIntStateOf(0) }
    var parentTrackerFilterIndex by remember { mutableIntStateOf(0) }
    var parentReadOnlySectionIndex by remember { mutableIntStateOf(0) }
    var subScreen by remember { mutableStateOf<SubScreenDestination>(SubScreenDestination.None) }
    var showAddChildDialog by remember { mutableStateOf(false) }

    val children: List<ChildProfile> = when (val st = childrenUiState) {
        is DataUiState.Success -> st.data
        else -> emptyList()
    }
    val records: List<HealthRecord> = when (val st = recordsUiState) {
        is DataUiState.Success -> st.data
        else -> emptyList()
    }
    val activeChild = children.find { it.id == selectedChildId } ?: children.firstOrNull()

    val isPediatricianMode = workspaceMode == WorkspaceMode.PEDIATRICIAN

    PediatricPushPermissionAndRealtimeObserver(records = records)

    val requestWorkspaceToggle: () -> Unit = {
        if (workspaceMode == WorkspaceMode.PEDIATRICIAN) {
            workspaceMode = WorkspaceMode.PARENT
            prefs.edit().putString("workspace_mode", WorkspaceMode.PARENT.name).apply()
            bottomTab = 0
            subScreen = SubScreenDestination.None
        } else {
            openPinModalOnConfigTab = false
            showPediatricianPinModal = true
        }
    }

    val openPinSettingsModal: () -> Unit = {
        openPinModalOnConfigTab = true
        showPediatricianPinModal = true
    }

    if (showPediatricianPinModal) {
        PediatricianPinDialog(
            expectedPin = pediatricianPin,
            pediatricianProfile = pediatricianProfile,
            initialConfigTab = openPinModalOnConfigTab,
            onDismiss = { showPediatricianPinModal = false },
            onUnlockSuccess = {
                showPediatricianPinModal = false
                workspaceMode = WorkspaceMode.PEDIATRICIAN
                prefs.edit().putString("workspace_mode", WorkspaceMode.PEDIATRICIAN.name).apply()
                bottomTab = 0
                subScreen = SubScreenDestination.None
            },
            onUpdatePinAndProfile = { updatedPin, updatedProfile ->
                pediatricianPin = updatedPin
                pediatricianProfile = updatedProfile
                prefs.edit()
                    .putString(KEY_PEDIATRICIAN_PIN, updatedPin)
                    .putString(KEY_DOC_NAME, updatedProfile.name)
                    .putString(KEY_DOC_LICENSE, updatedProfile.licenseId)
                    .putString(KEY_DOC_SPECIALTY, updatedProfile.specialty)
                    .apply()
            }
        )
    }

    if (showAddChildDialog) {
        AddOrEditChildDialog(
            initialChild = null,
            onDismiss = { showAddChildDialog = false },
            onConfirm = { newChild ->
                viewModel.addChild(newChild)
                showAddChildDialog = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                val navItems = if (isPediatricianMode) {
                    listOf(
                        Triple("Triaje", Icons.Filled.Groups, Icons.Outlined.Groups),
                        Triple("Consulta", Icons.Filled.Calculate, Icons.Outlined.Calculate),
                        Triple("Percentiles", Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp),
                        Triple("Clínica", Icons.Filled.MedicalInformation, Icons.Outlined.MedicalInformation),
                        Triple("Más", Icons.Filled.Menu, Icons.Outlined.Menu)
                    )
                } else {
                    listOf(
                        Triple("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
                        Triple("Bitácora", Icons.Filled.EditNote, Icons.Outlined.EditNote),
                        Triple("Salud", Icons.Filled.Medication, Icons.Outlined.Medication),
                        Triple("Hijos", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment),
                        Triple("Más", Icons.Filled.Menu, Icons.Outlined.Menu)
                    )
                }

                navItems.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
                    val isSelected = bottomTab == index && subScreen == SubScreenDestination.None
                    val activeAccent = if (isPediatricianMode) PequeBlue else PequeGreen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            subScreen = SubScreenDestination.None
                            bottomTab = index
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) filledIcon else outlinedIcon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeAccent,
                            selectedTextColor = activeAccent,
                            indicatorColor = activeAccent.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("bottom_nav_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (childrenUiState is DataUiState.Loading && children.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PequeBlue)
                }
            } else {
                when (val currentSub = subScreen) {
                    is SubScreenDestination.ChildDetail -> {
                        val targetChild = children.find { it.id == currentSub.childId } ?: activeChild
                        if (targetChild != null) {
                            ChildProfileScreen(
                                child = targetChild,
                                allChildren = children,
                                records = records,
                                showBackButton = true,
                                onBack = { subScreen = SubScreenDestination.None },
                                onSelectChild = { viewModel.selectChild(it) },
                                onAddChildClicked = { showAddChildDialog = true },
                                onEditChild = { viewModel.updateChild(it) },
                                onOpenVaccines = { subScreen = SubScreenDestination.Vaccines },
                                onDeleteChild = {
                                    viewModel.deleteChild(it)
                                    subScreen = SubScreenDestination.None
                                }
                            )
                        } else {
                            EmptyChildrenProfileState(
                                onAddChildClicked = { showAddChildDialog = true }
                            )
                        }
                    }

                    is SubScreenDestination.Vaccines -> {
                        VaccinesScreen(
                            activeChild = activeChild,
                            allChildren = children,
                            records = records,
                            onBack = { subScreen = SubScreenDestination.None },
                            onSelectChild = { viewModel.selectChild(it) },
                            onAddVaccine = { viewModel.addRecord(it) },
                            onToggleVaccineStatus = { viewModel.toggleRecordStatus(it) },
                            onDeleteVaccine = { viewModel.deleteRecord(it) }
                        )
                    }

                    is SubScreenDestination.Module -> {
                        if (currentSub.module == ClinicalModule.VACUNA) {
                            VaccinesScreen(
                                activeChild = activeChild,
                                allChildren = children,
                                records = records,
                                onBack = { subScreen = SubScreenDestination.None },
                                onSelectChild = { viewModel.selectChild(it) },
                                onAddVaccine = { viewModel.addRecord(it) },
                                onToggleVaccineStatus = { viewModel.toggleRecordStatus(it) },
                                onDeleteVaccine = { viewModel.deleteRecord(it) }
                            )
                        } else {
                            ClinicalModuleScreen(
                                module = currentSub.module,
                                activeChild = activeChild,
                                allChildren = children,
                                records = records,
                                onBack = { subScreen = SubScreenDestination.None },
                                onSelectChild = { viewModel.selectChild(it) },
                                onAddRecord = { viewModel.addRecord(it) },
                                onToggleRecordStatus = { viewModel.toggleRecordStatus(it) },
                                onDeleteRecord = { viewModel.deleteRecord(it) }
                            )
                        }
                    }

                    is SubScreenDestination.AiAssistant -> {
                        AiAssistantScreen(
                            activeChild = activeChild,
                            allChildren = children,
                            records = records,
                            onBack = { subScreen = SubScreenDestination.None },
                            onSelectChild = { viewModel.selectChild(it) },
                            isPediatricianMode = isPediatricianMode
                        )
                    }

                    is SubScreenDestination.HybridPwaHub -> {
                        HybridPwaArchitectureScreen(
                            children = children,
                            records = records,
                            selectedChildId = selectedChildId,
                            isPediatricianMode = isPediatricianMode,
                            onBack = { subScreen = SubScreenDestination.None },
                            onAddChild = { viewModel.addChild(it) },
                            onAddRecord = { viewModel.addRecord(it) }
                        )
                    }

                    is SubScreenDestination.None -> {
                        if (!isPediatricianMode) {
                            // =========================================================================
                            // ESCRITORIO DE PADRES (Lenguaje sencillo, cálido y guiado)
                            // =========================================================================
                            when (bottomTab) {
                                0 -> {
                                    ParentHomeDashboardScreen(
                                        parentDisplayName = userDisplayName,
                                        children = children,
                                        selectedChildId = selectedChildId,
                                        records = records,
                                        onSelectChild = { viewModel.selectChild(it) },
                                        onAddChildClicked = { showAddChildDialog = true },
                                        onEditActiveChild = { viewModel.updateChild(it) },
                                        onAddRecord = { viewModel.addRecord(it) },
                                        onOpenTrackerWithFilter = { filterIdx ->
                                            parentTrackerFilterIndex = filterIdx
                                            bottomTab = 1
                                        },
                                        onOpenReadOnlySection = { secIdx ->
                                            parentReadOnlySectionIndex = secIdx
                                            bottomTab = 2
                                        },
                                        onOpenAiAssistant = {
                                            subScreen = SubScreenDestination.AiAssistant
                                        },
                                        onRequestPediatricianMode = requestWorkspaceToggle,
                                        onOpenPinSettings = openPinSettingsModal,
                                        onConfirmPushNotification = { pushRec ->
                                            viewModel.replyOrUpdateRecord(
                                                pushRec.id,
                                                "Confirmado por padres",
                                                pushRec.notes,
                                                "Confirmación de aviso enviada al pediatra"
                                            )
                                        },
                                        onOpenHybridPwaHub = {
                                            subScreen = SubScreenDestination.HybridPwaHub
                                        }
                                    )
                                }

                                1 -> {
                                    ParentHomeTrackerScreen(
                                        activeChild = activeChild,
                                        allChildren = children,
                                        records = records,
                                        initialFilterIndex = parentTrackerFilterIndex,
                                        onSelectChild = { viewModel.selectChild(it) },
                                        onAddRecord = { viewModel.addRecord(it) },
                                        onDeleteRecord = { viewModel.deleteRecord(it) },
                                        onAddChildClicked = { showAddChildDialog = true }
                                    )
                                }

                                2 -> {
                                    ParentSimpleReadOnlyScreen(
                                        activeChild = activeChild,
                                        allChildren = children,
                                        records = records,
                                        initialSection = parentReadOnlySectionIndex,
                                        onSelectChild = { viewModel.selectChild(it) }
                                    )
                                }

                                3 -> {
                                    if (activeChild != null) {
                                        ChildProfileScreen(
                                            child = activeChild,
                                            allChildren = children,
                                            records = records,
                                            showBackButton = false,
                                            onBack = { bottomTab = 0 },
                                            onSelectChild = { viewModel.selectChild(it) },
                                            onAddChildClicked = { showAddChildDialog = true },
                                            onEditChild = { viewModel.updateChild(it) },
                                            onOpenVaccines = {
                                                parentReadOnlySectionIndex = 1
                                                bottomTab = 2
                                            },
                                            onDeleteChild = { viewModel.deleteChild(it) }
                                        )
                                    } else {
                                        EmptyChildrenProfileState(
                                            onAddChildClicked = { showAddChildDialog = true }
                                        )
                                    }
                                }

                                4 -> {
                                    MoreAndFullStructureScreen(
                                        userEmail = userEmail,
                                        isDarkTheme = isDarkTheme,
                                        onToggleDarkTheme = onToggleDarkTheme,
                                        onNavigateHome = { bottomTab = 0 },
                                        onNavigateChildren = { bottomTab = 3 },
                                        onOpenModule = { module ->
                                            subScreen = if (module == ClinicalModule.VACUNA) {
                                                SubScreenDestination.Vaccines
                                            } else {
                                                SubScreenDestination.Module(module)
                                            }
                                        },
                                        onOpenAiAssistant = {
                                            subScreen = SubScreenDestination.AiAssistant
                                        },
                                        onSignOut = {
                                            signOut(
                                                credentialManager = credentialManager,
                                                onSignOutComplete = {},
                                                scope = scope
                                            )
                                        },
                                        isPediatricianMode = false,
                                        pediatricianName = pediatricianProfile.name,
                                        currentPin = pediatricianPin,
                                        onRequestSwitchWorkspace = requestWorkspaceToggle,
                                        onOpenPinSettings = openPinSettingsModal,
                                        onClearAllData = { viewModel.clearAllData() },
                                        onOpenHybridPwaHub = {
                                            subScreen = SubScreenDestination.HybridPwaHub
                                        }
                                    )
                                }
                            }
                        } else {
                            // =========================================================================
                            // ESTACIÓN CLÍNICA DEL PEDIATRA (Uso exclusivo especialista)
                            // =========================================================================
                            when (bottomTab) {
                                0 -> {
                                    PediatricianTriageAndPatientsScreen(
                                        pediatricianProfile = pediatricianProfile,
                                        children = children,
                                        activeChild = activeChild,
                                        records = records,
                                        onSelectChild = { viewModel.selectChild(it) },
                                        onAddChildClicked = { showAddChildDialog = true },
                                        onEditActiveChild = { viewModel.updateChild(it) },
                                        onAddRecord = { viewModel.addRecord(it) },
                                        onReplyOrUpdateRecord = { id, status, notes, msg ->
                                            viewModel.replyOrUpdateRecord(id, status, notes, msg)
                                        },
                                        onSwitchToParentMode = requestWorkspaceToggle,
                                        onNavigateStation = { stationIdx -> bottomTab = stationIdx },
                                        onOpenAiAssistant = {
                                            subScreen = SubScreenDestination.AiAssistant
                                        },
                                        onOpenHybridPwaHub = {
                                            subScreen = SubScreenDestination.HybridPwaHub
                                        }
                                    )
                                }

                                1 -> {
                                    PediatricianConsultationAndDoseScreen(
                                        pediatricianProfile = pediatricianProfile,
                                        children = children,
                                        activeChild = activeChild,
                                        records = records,
                                        onSelectChild = { viewModel.selectChild(it) },
                                        onAddChildClicked = { showAddChildDialog = true },
                                        onEditActiveChild = { viewModel.updateChild(it) },
                                        onAddRecord = { viewModel.addRecord(it) },
                                        onSwitchToParentMode = requestWorkspaceToggle
                                    )
                                }

                                2 -> {
                                    PediatricianGrowthAndVaccinesScreen(
                                        pediatricianProfile = pediatricianProfile,
                                        children = children,
                                        activeChild = activeChild,
                                        records = records,
                                        onSelectChild = { viewModel.selectChild(it) },
                                        onAddChildClicked = { showAddChildDialog = true },
                                        onEditActiveChild = { viewModel.updateChild(it) },
                                        onAddRecord = { viewModel.addRecord(it) },
                                        onToggleRecordStatus = { viewModel.toggleRecordStatus(it) },
                                        onDeleteRecord = { viewModel.deleteRecord(it) },
                                        onSwitchToParentMode = requestWorkspaceToggle
                                    )
                                }

                                3 -> {
                                    PediatricianClinicalSuiteScreen(
                                        pediatricianProfile = pediatricianProfile,
                                        children = children,
                                        activeChild = activeChild,
                                        records = records,
                                        onSelectChild = { viewModel.selectChild(it) },
                                        onAddChildClicked = { showAddChildDialog = true },
                                        onEditActiveChild = { viewModel.updateChild(it) },
                                        onAddRecord = { viewModel.addRecord(it) },
                                        onOpenModule = { module ->
                                            subScreen = if (module == ClinicalModule.VACUNA) {
                                                SubScreenDestination.Vaccines
                                            } else {
                                                SubScreenDestination.Module(module)
                                            }
                                        },
                                        onOpenAiAssistant = {
                                            subScreen = SubScreenDestination.AiAssistant
                                        },
                                        onSwitchToParentMode = requestWorkspaceToggle
                                    )
                                }

                                4 -> {
                                    MoreAndFullStructureScreen(
                                        userEmail = userEmail,
                                        isDarkTheme = isDarkTheme,
                                        onToggleDarkTheme = onToggleDarkTheme,
                                        onNavigateHome = { bottomTab = 0 },
                                        onNavigateChildren = { bottomTab = 0 },
                                        onOpenModule = { module ->
                                            subScreen = if (module == ClinicalModule.VACUNA) {
                                                SubScreenDestination.Vaccines
                                            } else {
                                                SubScreenDestination.Module(module)
                                            }
                                        },
                                        onOpenAiAssistant = {
                                            subScreen = SubScreenDestination.AiAssistant
                                        },
                                        onSignOut = {
                                            signOut(
                                                credentialManager = credentialManager,
                                                onSignOutComplete = {},
                                                scope = scope
                                            )
                                        },
                                        isPediatricianMode = true,
                                        pediatricianName = pediatricianProfile.name,
                                        currentPin = pediatricianPin,
                                        onRequestSwitchWorkspace = requestWorkspaceToggle,
                                        onOpenPinSettings = openPinSettingsModal,
                                        onClearAllData = { viewModel.clearAllData() },
                                        onOpenHybridPwaHub = {
                                            subScreen = SubScreenDestination.HybridPwaHub
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
