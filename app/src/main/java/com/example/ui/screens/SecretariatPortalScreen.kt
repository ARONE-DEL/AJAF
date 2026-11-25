package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.PaymentRecordEntity
import com.example.data.model.*
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.KpiCard
import com.example.ui.components.ScreenshotPreviewDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretariatPortalScreen(
    session: ActiveSession.StaffSession,
    uiState: AjafUiState,
    onCreateTrainer: (fullName: String, email: String, password: String) -> Unit,
    onAssignTrainerToCourse: (courseId: String, trainerId: String?) -> Unit,
    onReviewPayment: (paymentId: String, approved: Boolean) -> Unit,
    onOpenReceiptPdf: (PaymentRecordEntity) -> Unit,
    onOpenPeriodicReportPdf: (ReportPeriod) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var screenshotToView by remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 900.dp)
    ) {
        Surface(color = AjafNavyDeep, contentColor = Color.White, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Portal da Secretaria", style = MaterialTheme.typography.labelMedium, color = AjafEmeraldBright)
                    Text(session.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Gestão Académica, Validações e Tesouraria", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                }
                FilledTonalButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.14f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("secretariat_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sair")
                }
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Aprovações") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Recibos (PDF)") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Formadores & Cursos") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Relatórios") })
        }

        when (selectedTab) {
            0 -> ApprovalsTab(
                uiState = uiState,
                onViewScreenshot = { path, title -> screenshotToView = path to title },
                onReviewPayment = onReviewPayment
            )
            1 -> ReceiptsTab(
                uiState = uiState,
                onOpenReceiptPdf = onOpenReceiptPdf
            )
            2 -> TrainersTab(
                uiState = uiState,
                onCreateTrainer = onCreateTrainer,
                onAssignTrainerToCourse = onAssignTrainerToCourse
            )
            3 -> ReportsTab(
                uiState = uiState,
                onOpenPeriodicReportPdf = onOpenPeriodicReportPdf
            )
        }
    }

    if (screenshotToView != null) {
        ScreenshotPreviewDialog(
            imagePath = screenshotToView!!.first,
            title = screenshotToView!!.second,
            onDismiss = { screenshotToView = null }
        )
    }
}

@Composable
private fun ApprovalsTab(
    uiState: AjafUiState,
    onViewScreenshot: (String, String) -> Unit,
    onReviewPayment: (String, Boolean) -> Unit
) {
    var selectedCourseId by remember { mutableStateOf("TODOS") }
    var onlyPending by remember { mutableStateOf(true) }

    val filteredPayments = remember(uiState.payments, selectedCourseId, onlyPending) {
        uiState.payments.filter { p ->
            val matchesCourse = selectedCourseId == "TODOS" || p.courseId == selectedCourseId
            val matchesStatus = if (onlyPending) p.status == PaymentStatus.PENDENTE else p.status != PaymentStatus.POR_PAGAR
            matchesCourse && matchesStatus
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = onlyPending,
                    onClick = { onlyPending = true },
                    label = { Text("Pendentes (${uiState.payments.count { it.status == PaymentStatus.PENDENTE }})") }
                )
                FilterChip(
                    selected = !onlyPending,
                    onClick = { onlyPending = false },
                    label = { Text("Todos Submetidos") }
                )
            }
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCourseId == "TODOS",
                        onClick = { selectedCourseId = "TODOS" },
                        label = { Text("Todos Cursos") }
                    )
                }
                items(uiState.courses, key = { it.id }) { c ->
                    FilterChip(
                        selected = selectedCourseId == c.id,
                        onClick = { selectedCourseId = c.id },
                        label = { Text(c.name) }
                    )
                }
            }
        }

        if (filteredPayments.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.TaskAlt,
                    title = "Sem pagamentos pendentes",
                    description = "Todos os comprovativos filtrados já foram analisados."
                )
            }
        } else {
            items(filteredPayments, key = { it.id }) { p ->
                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${p.studentNumber} • ${p.courseName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(
                                label = p.status.labelPt,
                                containerColor = when (p.status) {
                                    PaymentStatus.APROVADO -> AjafEmeraldContainer
                                    PaymentStatus.PENDENTE -> AjafAmberContainer
                                    else -> AjafErrorContainer
                                },
                                contentColor = when (p.status) {
                                    PaymentStatus.APROVADO -> AjafOnEmeraldContainer
                                    PaymentStatus.PENDENTE -> AjafOnAmberContainer
                                    else -> AjafOnErrorContainer
                                }
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${p.feeType.labelPt} — ${p.referenceLabel}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                Text("Valor Total: ${AjafFormatters.formatMzn(p.totalAmountMzn)}" + if (p.lateFeeApplied) " (inclui multa 16%)" else "", style = MaterialTheme.typography.bodySmall)
                                Text("Método: ${p.paymentMethod?.labelPt ?: "—"}  |  ID Transacção: ${p.transactionId ?: "—"}", style = MaterialTheme.typography.bodySmall)
                                if (!p.smsText.isNullOrBlank()) {
                                    Text("SMS: \"${p.smsText}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (!p.screenshotPath.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = { onViewScreenshot(p.screenshotPath, "Comprovativo — ${p.studentName}") },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Visualizar Captura de Ecrã")
                            }
                        }

                        if (p.status == PaymentStatus.PENDENTE && !p.lockedByAdmin) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { onReviewPayment(p.id, false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AjafError),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Rejeitar")
                                }
                                Button(
                                    onClick = { onReviewPayment(p.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Aprovar")
                                }
                            }
                        } else if (p.lockedByAdmin) {
                            StatusBadge(
                                label = "Registo Trancado (Não Editável)",
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                icon = Icons.Filled.Lock
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptsTab(
    uiState: AjafUiState,
    onOpenReceiptPdf: (PaymentRecordEntity) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val approved = remember(uiState.payments, search) {
        uiState.payments.filter { p ->
            p.status == PaymentStatus.APROVADO &&
                (search.isBlank() ||
                    p.studentName.contains(search, ignoreCase = true) ||
                    p.studentNumber.contains(search, ignoreCase = true))
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Pesquisar recibos por nome ou Nº estudante") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (approved.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.ReceiptLong,
                    title = "Sem recibos disponíveis",
                    description = "Os pagamentos aprovados aparecerão aqui para emissão imediata em PDF."
                )
            }
        } else {
            items(approved, key = { it.id }) { p ->
                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${p.referenceLabel} • ${p.courseName}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${AjafFormatters.formatMzn(p.totalAmountMzn)} • ${p.paymentMethod?.labelPt ?: "Numerário"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AjafEmerald
                            )
                        }
                        Button(
                            onClick = { onOpenReceiptPdf(p) },
                            colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Recibo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainersTab(
    uiState: AjafUiState,
    onCreateTrainer: (String, String, String) -> Unit,
    onAssignTrainerToCourse: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val trainers = remember(uiState.staffUsers) {
        uiState.staffUsers.filter { it.role == UserRole.FORMADOR && it.active }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cadastrar Novo Formador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail do Formador *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Palavra-passe (mín. 6 caracteres) *") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (name.isNotBlank() && email.contains("@") && password.length >= 6) {
                                onCreateTrainer(name, email, password)
                                name = ""
                                email = ""
                                password = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Criar Conta de Formador")
                    }
                }
            }
        }

        item {
            Text("Associar Formadores aos Cursos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(uiState.courses, key = { it.id }) { course ->
            var expanded by remember { mutableStateOf(false) }
            val assignedTrainer = trainers.find { it.id == course.trainerId }

            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(course.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Formador actual: ${assignedTrainer?.fullName ?: "Nenhum atribuído"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (assignedTrainer != null) AjafEmerald else AjafAmber
                    )
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (trainers.isEmpty()) "Cadastre um formador primeiro" else "Seleccionar Formador")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Sem formador") },
                                onClick = {
                                    onAssignTrainerToCourse(course.id, null)
                                    expanded = false
                                }
                            )
                            trainers.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.fullName} (${t.email})") },
                                    onClick = {
                                        onAssignTrainerToCourse(course.id, t.id)
                                        expanded = false
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

@Composable
private fun ReportsTab(
    uiState: AjafUiState,
    onOpenPeriodicReportPdf: (ReportPeriod) -> Unit
) {
    val todayStr = AjafFormatters.formatDate(System.currentTimeMillis())
    val approvedToday = remember(uiState.payments, todayStr) {
        uiState.payments.filter { p ->
            p.status == PaymentStatus.APROVADO &&
                AjafFormatters.formatDate(p.reviewedAt ?: p.submittedAt ?: 0L) == todayStr
        }
    }
    val allApproved = remember(uiState.payments) {
        uiState.payments.filter { it.status == PaymentStatus.APROVADO }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Resumo Diário ($todayStr)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Arrecadado Hoje",
                    value = AjafFormatters.formatMzn(approvedToday.sumOf { it.totalAmountMzn }),
                    icon = Icons.Filled.Today,
                    accentColor = AjafEmerald,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Total Acumulado",
                    value = AjafFormatters.formatMzn(allApproved.sumOf { it.totalAmountMzn }),
                    icon = Icons.Filled.AccountBalanceWallet,
                    accentColor = AjafNavyPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Discriminação por Tipo (Hoje / Geral)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    FeeType.entries.forEach { ft ->
                        val todaySum = approvedToday.filter { it.feeType == ft }.sumOf { it.totalAmountMzn }
                        val allSum = allApproved.filter { it.feeType == ft }.sumOf { it.totalAmountMzn }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(ft.labelPt, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${AjafFormatters.formatMzn(todaySum)} / ${AjafFormatters.formatMzn(allSum)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Text("Discriminação por Método (Hoje / Geral)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    PaymentMethod.entries.forEach { pm ->
                        val todaySum = approvedToday.filter { it.paymentMethod == pm }.sumOf { it.totalAmountMzn }
                        val allSum = allApproved.filter { it.paymentMethod == pm }.sumOf { it.totalAmountMzn }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(pm.labelPt, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${AjafFormatters.formatMzn(todaySum)} / ${AjafFormatters.formatMzn(allSum)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Relatórios Periódicos (Exportar PDF)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportPeriod.entries.forEach { period ->
                    Button(
                        onClick = { onOpenPeriodicReportPdf(period) },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gerar Relatório ${period.labelPt} (PDF)")
                    }
                }
            }
        }
    }
}
