package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.ActiveSession
import com.example.data.model.UserRole
import com.example.ui.components.OfflineBanner
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.screens.*
import com.example.ui.theme.AjafTheme
import com.example.ui.viewmodel.AjafViewModel

object AjafRoutes {
    const val HOME = "inicio"
    const val LIBRARY = "biblioteca"
    const val ENROLL = "inscrever"
    const val PORTAL = "portal"
}

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val primaryDestinations = listOf(
    NavDestination(AjafRoutes.HOME, "Início", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    NavDestination(AjafRoutes.LIBRARY, "Biblioteca", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_library"),
    NavDestination(AjafRoutes.ENROLL, "Inscrever", Icons.Filled.AppRegistration, Icons.Outlined.AppRegistration, "nav_enroll"),
    NavDestination(AjafRoutes.PORTAL, "Portal", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "nav_portal")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AjafTheme {
                AjafAppRoot()
            }
        }
    }
}

@Composable
fun AjafAppRoot(viewModel: AjafViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AjafRoutes.HOME

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackMessage, uiState.errorMessage) {
        val msg = uiState.errorMessage ?: uiState.feedbackMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(AjafRoutes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        primaryDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label, maxLines = 1) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        windowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Vertical)
                    ) {
                        Spacer(Modifier.height(12.dp))
                        primaryDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    OfflineBanner(isOnline = uiState.isOnline)

                    NavHost(
                        navController = navController,
                        startDestination = AjafRoutes.HOME,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        composable(AjafRoutes.HOME) {
                            PublicHomeScreen(
                                uiState = uiState,
                                onNavigateToLibrary = { navigateToTab(AjafRoutes.LIBRARY) },
                                onNavigateToEnroll = { courseId ->
                                    viewModel.setPreselectedCourse(courseId)
                                    navigateToTab(AjafRoutes.ENROLL)
                                },
                                onNavigateToLogin = { navigateToTab(AjafRoutes.PORTAL) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        composable(AjafRoutes.LIBRARY) {
                            BackHandler { navigateToTab(AjafRoutes.HOME) }
                            LibraryScreen(
                                uiState = uiState,
                                onDownloadPdf = { doc -> viewModel.openLibraryManualPdf(doc) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        composable(AjafRoutes.ENROLL) {
                            BackHandler { navigateToTab(AjafRoutes.HOME) }
                            EnrollmentScreen(
                                uiState = uiState,
                                onSubmitEnrollment = { fullName, birthDate, nationality, province, courseId, modality, phone, method, txId, sms, uri, onProtocol ->
                                    viewModel.submitEnrollment(
                                        fullName, birthDate, nationality, province,
                                        courseId, modality, phone, method, txId, sms, uri, onProtocol
                                    )
                                },
                                onGoToLogin = { navigateToTab(AjafRoutes.PORTAL) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        composable(AjafRoutes.PORTAL) {
                            BackHandler { navigateToTab(AjafRoutes.HOME) }
                            when (val session = uiState.session) {
                                ActiveSession.Guest -> {
                                    LoginScreen(
                                        uiState = uiState,
                                        onLoginStudent = viewModel::loginStudent,
                                        onLoginStaff = viewModel::loginStaff,
                                        onRegisterFirstSuperadmin = viewModel::registerFirstSuperadmin,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }

                                is ActiveSession.StudentSession -> {
                                    StudentPortalScreen(
                                        session = session,
                                        uiState = uiState,
                                        onSubmitPayment = viewModel::submitStudentPayment,
                                        onOpenReceiptPdf = viewModel::openReceiptPdf,
                                        onOpenPautaPdf = viewModel::openPautaPdf,
                                        onLogout = viewModel::logout,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }

                                is ActiveSession.StaffSession -> {
                                    when (session.role) {
                                        UserRole.FORMADOR -> TrainerPortalScreen(
                                            session = session,
                                            uiState = uiState,
                                            onSaveGrade = viewModel::saveGrade,
                                            onOpenPautaPdf = viewModel::openPautaPdf,
                                            onLogout = viewModel::logout,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )

                                        UserRole.SECRETARIA -> SecretariatPortalScreen(
                                            session = session,
                                            uiState = uiState,
                                            onCreateTrainer = { n, e, p ->
                                                viewModel.createInternalStaff(n, e, p, UserRole.FORMADOR)
                                            },
                                            onAssignTrainerToCourse = viewModel::assignTrainerToCourse,
                                            onReviewPayment = viewModel::reviewPayment,
                                            onOpenReceiptPdf = viewModel::openReceiptPdf,
                                            onOpenPeriodicReportPdf = viewModel::openPeriodicReportPdf,
                                            onLogout = viewModel::logout,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )

                                        UserRole.DIRECTOR -> DirectorPortalScreen(
                                            session = session,
                                            uiState = uiState,
                                            onApproveCertificate = viewModel::approveCertificateByDirector,
                                            onOpenAcademicPerformancePdf = viewModel::openAcademicPerformancePdf,
                                            onLogout = viewModel::logout,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )

                                        UserRole.SUPERADMIN -> SuperadminPortalScreen(
                                            session = session,
                                            uiState = uiState,
                                            onCreateInternalStaff = viewModel::createInternalStaff,
                                            onToggleStaffActive = viewModel::toggleStaffActive,
                                            onCreateCourse = viewModel::createCourse,
                                            onPublishVacancy = viewModel::publishVacancy,
                                            onDeleteVacancy = viewModel::deleteVacancy,
                                            onPublishNotice = viewModel::publishStudentNotice,
                                            onPublishLibraryDoc = viewModel::publishLibraryDocument,
                                            onDeleteLibraryDoc = viewModel::deleteLibraryDocument,
                                            onIssueCertificatePdf = viewModel::issueAndOpenOfficialCertificatePdf,
                                            onToggleCourseLock = viewModel::toggleCourseGradesLock,
                                            onTogglePaymentLock = viewModel::togglePaymentLock,
                                            onToggleAuditLock = viewModel::toggleAuditLock,
                                            onOpenConsolidatedReportPdf = viewModel::openConsolidatedAdminReportPdf,
                                            onLogout = viewModel::logout,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )

                                        UserRole.ALUNO -> Unit
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.activePdfPreview?.let { (title, file) ->
        PdfPreviewDialog(
            title = title,
            pdfFile = file,
            onDismiss = viewModel::closePdfPreview,
            onSaveToDownloads = viewModel::savePreviewedPdfToDownloads
        )
    }
}
