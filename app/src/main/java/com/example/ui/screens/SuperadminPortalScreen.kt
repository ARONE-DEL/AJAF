package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.data.model.*
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.KpiCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@Composable
fun SuperadminPortalScreen(
    session: ActiveSession.StaffSession,
    uiState: AjafUiState,
    onCreateInternalStaff: (fullName: String, email: String, password: String, role: UserRole) -> Unit,
    onToggleStaffActive: (StaffUserEntity) -> Unit,
    onCreateCourse: (name: String, category: String, desc: String, hours: Int, months: Int, enrollFee: Double, monthlyFee: Double, examFee: Double, certFee: Double, modalities: String) -> Unit,
    onPublishVacancy: (courseId: String, title: String, summary: String, startDate: String, endDate: String, modalities: String, count: Int) -> Unit,
    onDeleteVacancy: (String) -> Unit,
    onPublishNotice: (title: String, message: String, category: String) -> Unit,
    onPublishLibraryDoc: (title: String, category: String, courseName: String, desc: String, content: String, pdfUri: Uri?) -> Unit,
    onDeleteLibraryDoc: (LibraryDocumentEntity) -> Unit,
    onIssueCertificatePdf: (CertificateRecordEntity) -> Unit,
    onToggleCourseLock: (CourseEntity) -> Unit,
    onTogglePaymentLock: (PaymentRecordEntity) -> Unit,
    onToggleAuditLock: (AuditLogEntity) -> Unit,
    onOpenConsolidatedReportPdf: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 960.dp)
    ) {
        Surface(color = AjafNavyDeep, contentColor = Color.White, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Superadministrador AJAF", style = MaterialTheme.typography.labelMedium, color = AjafAmberBright)
                    Text(session.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(session.email, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                }
                FilledTonalButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.14f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("superadmin_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sair")
                }
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Dashboard") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Utilizadores") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Cursos & Avisos") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Biblioteca") })
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Certificados") })
            Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }, text = { Text("Auditoria & Bloqueio") })
        }

        when (selectedTab) {
            0 -> SuperadminDashboardTab(uiState, onOpenConsolidatedReportPdf)
            1 -> SuperadminUsersTab(uiState, onCreateInternalStaff, onToggleStaffActive)
            2 -> SuperadminCoursesNoticesTab(uiState, onCreateCourse, onPublishVacancy, onDeleteVacancy, onPublishNotice)
            3 -> SuperadminLibraryTab(uiState, onPublishLibraryDoc, onDeleteLibraryDoc)
            4 -> SuperadminCertificatesTab(uiState, onIssueCertificatePdf)
            5 -> SuperadminAuditLockTab(uiState, onToggleCourseLock, onTogglePaymentLock, onToggleAuditLock, onOpenConsolidatedReportPdf)
        }
    }
}

@Composable
private fun SuperadminDashboardTab(
    uiState: AjafUiState,
    onOpenConsolidatedReportPdf: () -> Unit
) {
    val totalRevenue = uiState.payments.filter { it.status == PaymentStatus.APROVADO }.sumOf { it.totalAmountMzn }
    val pendingPayments = uiState.payments.count { it.status == PaymentStatus.PENDENTE }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Alunos Inscritos",
                    value = "${uiState.students.size}",
                    icon = Icons.Filled.People,
                    accentColor = AjafNavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Receita Total",
                    value = AjafFormatters.formatMzn(totalRevenue),
                    icon = Icons.Filled.Payments,
                    accentColor = AjafEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Pagamentos Pendentes",
                    value = "$pendingPayments",
                    icon = Icons.Filled.PendingActions,
                    accentColor = AjafAmber,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Cursos & Manuais",
                    value = "${uiState.courses.count { it.active }} / ${uiState.libraryDocs.size}",
                    icon = Icons.Filled.LibraryBooks,
                    accentColor = AjafNavyPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Button(
                onClick = onOpenConsolidatedReportPdf,
                colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar Relatório Consolidado Geral (PDF)")
            }
        }
        item {
            Text("Logs Recentes das Interfaces", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(uiState.auditLogs.take(12), key = { it.id }) { log ->
            ElevatedCard(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        StatusBadge(
                            label = log.actorRole,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(AjafFormatters.formatDateTime(log.timestamp), style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${log.action} — ${log.actorName}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(log.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SuperadminUsersTab(
    uiState: AjafUiState,
    onCreateInternalStaff: (String, String, String, UserRole) -> Unit,
    onToggleStaffActive: (StaffUserEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.SECRETARIA) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cadastrar Conta Interna", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(UserRole.SECRETARIA, UserRole.DIRECTOR, UserRole.FORMADOR).forEach { r ->
                            FilterChip(
                                selected = selectedRole == r,
                                onClick = { selectedRole = r },
                                label = { Text(r.labelPt) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("sa_create_user_name")
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail Institucional *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("sa_create_user_email")
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Palavra-passe (mín. 6 caracteres) *") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("sa_create_user_password")
                    )
                    Button(
                        onClick = {
                            if (name.isNotBlank() && email.contains("@") && password.length >= 6) {
                                onCreateInternalStaff(name, email, password, selectedRole)
                                name = ""
                                email = ""
                                password = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sa_create_user_submit")
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Criar Conta (${selectedRole.labelPt})")
                    }
                }
            }
        }

        item {
            Text("Contas Registadas (${uiState.staffUsers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(uiState.staffUsers, key = { it.id }) { u ->
            ElevatedCard(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(u.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(u.email, style = MaterialTheme.typography.bodySmall)
                        StatusBadge(
                            label = "${u.role.labelPt} • ${if (u.active) "Activo" else "Desactivado"}",
                            containerColor = if (u.active) AjafEmeraldContainer else AjafErrorContainer,
                            contentColor = if (u.active) AjafOnEmeraldContainer else AjafOnErrorContainer
                        )
                    }
                    if (u.role != UserRole.SUPERADMIN) {
                        Switch(checked = u.active, onCheckedChange = { onToggleStaffActive(u) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SuperadminCoursesNoticesTab(
    uiState: AjafUiState,
    onCreateCourse: (String, String, String, Int, Int, Double, Double, Double, Double, String) -> Unit,
    onPublishVacancy: (String, String, String, String, String, String, Int) -> Unit,
    onDeleteVacancy: (String) -> Unit,
    onPublishNotice: (String, String, String) -> Unit
) {
    // Curso
    var cName by remember { mutableStateOf("") }
    var cCat by remember { mutableStateOf("Tecnologia & TI") }
    var cDesc by remember { mutableStateOf("") }
    var cHours by remember { mutableStateOf("120") }
    var cMonths by remember { mutableStateOf("3") }
    var cEnroll by remember { mutableStateOf("500") }
    var cMonthly by remember { mutableStateOf("1800") }
    var cExam by remember { mutableStateOf("650") }
    var cCert by remember { mutableStateOf("850") }

    // Vaga
    var vacCourseId by remember(uiState.courses) { mutableStateOf(uiState.courses.firstOrNull()?.id.orEmpty()) }
    var vacTitle by remember { mutableStateOf("") }
    var vacSummary by remember { mutableStateOf("") }
    var vacStart by remember { mutableStateOf("15/01/2027") }
    var vacEnd by remember { mutableStateOf("15/04/2027") }
    var vacCount by remember { mutableStateOf("25") }

    // Aviso
    var nTitle by remember { mutableStateOf("") }
    var nMsg by remember { mutableStateOf("") }
    var nCat by remember { mutableStateOf("Propinas") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Criar e Publicar Novo Curso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = cName, onValueChange = { cName = it }, label = { Text("Nome do Curso *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cCat, onValueChange = { cCat = it }, label = { Text("Área / Categoria") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cDesc, onValueChange = { cDesc = it }, label = { Text("Descrição") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cHours, onValueChange = { cHours = it }, label = { Text("Horas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cMonths, onValueChange = { cMonths = it }, label = { Text("Meses") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cEnroll, onValueChange = { cEnroll = it }, label = { Text("Inscrição MT") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cMonthly, onValueChange = { cMonthly = it }, label = { Text("Mensalidade MT") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cExam, onValueChange = { cExam = it }, label = { Text("Exame MT") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cCert, onValueChange = { cCert = it }, label = { Text("Certificado MT") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    }
                    Button(
                        onClick = {
                            if (cName.isNotBlank()) {
                                onCreateCourse(
                                    cName, cCat, cDesc.ifBlank { "Curso técnico profissional AJAF." },
                                    cHours.toIntOrNull() ?: 120, cMonths.toIntOrNull() ?: 3,
                                    cEnroll.toDoubleOrNull() ?: 500.0, cMonthly.toDoubleOrNull() ?: 1800.0,
                                    cExam.toDoubleOrNull() ?: 600.0, cCert.toDoubleOrNull() ?: 800.0,
                                    "Online, Presencial"
                                )
                                cName = ""
                                cDesc = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Publicar Curso")
                    }
                }
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Publicar Anúncio de Vagas (Home Pública)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = vacTitle, onValueChange = { vacTitle = it }, label = { Text("Título do Anúncio *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = vacSummary, onValueChange = { vacSummary = it }, label = { Text("Resumo / Informações") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = vacStart, onValueChange = { vacStart = it }, label = { Text("Início") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = vacEnd, onValueChange = { vacEnd = it }, label = { Text("Fim") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = vacCount, onValueChange = { vacCount = it }, label = { Text("Vagas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(0.8f))
                    }
                    Button(
                        onClick = {
                            if (vacTitle.isNotBlank() && vacCourseId.isNotBlank()) {
                                onPublishVacancy(vacCourseId, vacTitle, vacSummary.ifBlank { "Inscrições abertas." }, vacStart, vacEnd, "Online e Presencial", vacCount.toIntOrNull() ?: 20)
                                vacTitle = ""
                                vacSummary = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Publicar Vaga na Home")
                    }
                }
            }
        }

        items(uiState.vacancies, key = { it.id }) { v ->
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(v.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("${v.startDate} – ${v.endDate} • ${v.vacanciesCount} vagas", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDeleteVacancy(v.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remover vaga", tint = AjafError)
                    }
                }
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Publicar Comunicado aos Alunos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Propinas", "Exames", "Certificados").forEach { cat ->
                            FilterChip(selected = nCat == cat, onClick = { nCat = cat }, label = { Text(cat) })
                        }
                    }
                    OutlinedTextField(value = nTitle, onValueChange = { nTitle = it }, label = { Text("Título do Comunicado *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nMsg, onValueChange = { nMsg = it }, label = { Text("Mensagem *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (nTitle.isNotBlank() && nMsg.isNotBlank()) {
                                onPublishNotice(nTitle, nMsg, nCat)
                                nTitle = ""
                                nMsg = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafAmber),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Enviar para Painel dos Alunos")
                    }
                }
            }
        }
    }
}

@Composable
private fun SuperadminLibraryTab(
    uiState: AjafUiState,
    onPublishLibraryDoc: (String, String, String, String, String, Uri?) -> Unit,
    onDeleteLibraryDoc: (LibraryDocumentEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tecnologia & TI") }
    var courseName by remember { mutableStateOf("Informática Aplicada e Redes") }
    var description by remember { mutableStateOf("") }
    var contentSummary by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) selectedPdfUri = uri
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Carregar Manual na Biblioteca Pública", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título do Manual / Livro *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria / Área *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = courseName, onValueChange = { courseName = it }, label = { Text("Curso Associado *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição Pública *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = contentSummary, onValueChange = { contentSummary = it }, label = { Text("Conteúdo / Tópicos (para geração PDF)") }, minLines = 3, modifier = Modifier.fillMaxWidth())

                    OutlinedButton(
                        onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (selectedPdfUri == null) "Anexar Ficheiro PDF (Opcional)" else "PDF Seleccionado ✓")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank()) {
                                onPublishLibraryDoc(title, category, courseName, description, contentSummary, selectedPdfUri)
                                title = ""
                                description = ""
                                contentSummary = ""
                                selectedPdfUri = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Publicar na Biblioteca")
                    }
                }
            }
        }

        items(uiState.libraryDocs, key = { it.id }) { doc ->
            ElevatedCard(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(doc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${doc.category} • ${doc.courseName}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDeleteLibraryDoc(doc) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remover documento", tint = AjafError)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuperadminCertificatesTab(
    uiState: AjafUiState,
    onIssueCertificatePdf: (CertificateRecordEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.certificates.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.WorkspacePremium,
                    title = "Nenhum certificado em fila",
                    description = "Certificados aparecem aqui quando o aluno conclui com aprovação e liquida a Taxa de Certificado."
                )
            }
        } else {
            items(uiState.certificates, key = { it.id }) { cert ->
                val feeApproved = uiState.payments.any {
                    it.studentId == cert.studentId &&
                        it.feeType == FeeType.CERTIFICADO &&
                        it.status == PaymentStatus.APROVADO
                }
                val canIssue = feeApproved && cert.directorApproved

                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(cert.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${cert.studentNumber} • ${cert.courseName} (${cert.durationHours}h)", style = MaterialTheme.typography.bodySmall)
                        Text("Nota Final: ${AjafFormatters.formatGrade(cert.finalGrade)} val.  •  Código: ${cert.certificateCode}", style = MaterialTheme.typography.labelMedium)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(
                                label = if (feeApproved) "Secretaria: Pago ✓" else "Taxa Pendente",
                                containerColor = if (feeApproved) AjafEmeraldContainer else AjafAmberContainer,
                                contentColor = if (feeApproved) AjafOnEmeraldContainer else AjafOnAmberContainer
                            )
                            StatusBadge(
                                label = if (cert.directorApproved) "Director: Assinado ✓" else "Aguarda Director",
                                containerColor = if (cert.directorApproved) AjafEmeraldContainer else AjafAmberContainer,
                                contentColor = if (cert.directorApproved) AjafOnEmeraldContainer else AjafOnAmberContainer
                            )
                        }

                        Button(
                            onClick = { onIssueCertificatePdf(cert) },
                            enabled = canIssue,
                            colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (cert.issuedBySuperadmin) "Reemitir Certificado Oficial (PDF)" else "Emitir Certificado Oficial (PDF)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuperadminAuditLockTab(
    uiState: AjafUiState,
    onToggleCourseLock: (CourseEntity) -> Unit,
    onTogglePaymentLock: (PaymentRecordEntity) -> Unit,
    onToggleAuditLock: (AuditLogEntity) -> Unit,
    onOpenConsolidatedReportPdf: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Button(
                onClick = onOpenConsolidatedReportPdf,
                colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Baixar Relatório Consolidado de Auditoria (PDF)")
            }
        }

        item {
            Text("Controlo de Edição das Pautas (Editável / Não Editável)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(uiState.courses, key = { "lock_${it.id}" }) { c ->
            ElevatedCard(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(c.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            if (c.gradesLocked) "Não Editável (Bloqueada)" else "Editável pelo Formador",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (c.gradesLocked) AjafError else AjafEmerald
                        )
                    }
                    FilledTonalButton(onClick = { onToggleCourseLock(c) }) {
                        Icon(if (c.gradesLocked) Icons.Filled.Lock else Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (c.gradesLocked) "Desbloquear" else "Trancar")
                    }
                }
            }
        }

        item {
            Text("Controlo de Registos Financeiros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(uiState.payments.take(8), key = { "paylock_${it.id}" }) { p ->
            ElevatedCard(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${p.studentName} • ${p.referenceLabel}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text("${AjafFormatters.formatMzn(p.totalAmountMzn)} • ${p.status.labelPt}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onTogglePaymentLock(p) }) {
                        Icon(
                            if (p.lockedByAdmin) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = "Alternar edição",
                            tint = if (p.lockedByAdmin) AjafError else AjafEmerald
                        )
                    }
                }
            }
        }

        item {
            Text("Histórico Completo de Auditoria (${uiState.auditLogs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(uiState.auditLogs, key = { "audit_${it.id}" }) { log ->
            ElevatedCard(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("[${log.actorRole}] ${log.action}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(log.details, style = MaterialTheme.typography.bodySmall)
                        Text("${log.actorName} • ${AjafFormatters.formatDateTime(log.timestamp)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onToggleAuditLock(log) }) {
                        Icon(
                            if (log.locked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = "Selar registo",
                            tint = if (log.locked) AjafAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
