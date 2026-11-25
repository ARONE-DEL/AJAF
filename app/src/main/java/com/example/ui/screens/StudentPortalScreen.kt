package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.data.local.GradeRecordEntity
import com.example.data.local.PaymentRecordEntity
import com.example.data.model.*
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PaymentSubmissionDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@Composable
fun StudentPortalScreen(
    session: ActiveSession.StudentSession,
    uiState: AjafUiState,
    onSubmitPayment: (paymentId: String, method: PaymentMethod, txId: String?, sms: String?, uri: Uri?) -> Unit,
    onOpenReceiptPdf: (PaymentRecordEntity) -> Unit,
    onOpenPautaPdf: (courseName: String, trainerName: String, grades: List<GradeRecordEntity>) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var paymentTarget by remember { mutableStateOf<PaymentRecordEntity?>(null) }

    val student = uiState.students.find { it.id == session.studentId }
    val course = uiState.courses.find { it.id == student?.courseId }
    val trainerName = uiState.staffUsers.find { it.id == course?.trainerId }?.fullName ?: "Coordenação Pedagógica"
    val myPayments = remember(uiState.payments, session.studentId) {
        uiState.payments.filter { it.studentId == session.studentId }
    }
    val myGrade = uiState.grades.find { it.studentId == session.studentId }
    val myCert = uiState.certificates.find { it.studentId == session.studentId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        // Cartão de Identificação do Aluno
        Surface(
            color = AjafNavyDeep,
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Portal do Aluno", style = MaterialTheme.typography.labelMedium, color = AjafEmeraldBright)
                        Text(session.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "${session.studentNumber} • ${course?.name ?: "Curso AJAF"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    FilledTonalButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White.copy(alpha = 0.14f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("student_logout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sair")
                    }
                }
                if (student != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(
                            label = student.status.labelPt,
                            containerColor = when (student.status) {
                                EnrollmentStatus.ACTIVO, EnrollmentStatus.CONCLUIDO -> AjafEmerald
                                EnrollmentStatus.PENDENTE -> AjafAmber
                                EnrollmentStatus.REJEITADO -> AjafError
                            },
                            contentColor = Color.White
                        )
                        StatusBadge(
                            label = "${student.modality} • ${student.province}",
                            containerColor = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White
                        )
                    }
                }
            }
        }

        // Navegação Interna do Aluno
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Avisos") },
                icon = { Icon(Icons.Filled.Campaign, contentDescription = null) },
                modifier = Modifier.testTag("student_tab_notices")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Minha Pauta") },
                icon = { Icon(Icons.Filled.Grading, contentDescription = null) },
                modifier = Modifier.testTag("student_tab_grades")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Propinas & Taxas") },
                icon = { Icon(Icons.Filled.Payments, contentDescription = null) },
                modifier = Modifier.testTag("student_tab_fees")
            )
        }

        when (selectedTab) {
            0 -> {
                // Painel de Avisos
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.notices.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.NotificationsNone,
                                title = "Sem avisos publicados",
                                description = "Os comunicados sobre propinas, exames e certificados aparecerão aqui."
                            )
                        }
                    } else {
                        items(uiState.notices, key = { it.id }) { notice ->
                            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        StatusBadge(
                                            label = notice.category,
                                            containerColor = when (notice.category) {
                                                "Propinas" -> AjafAmberContainer
                                                "Certificados" -> AjafEmeraldContainer
                                                else -> MaterialTheme.colorScheme.primaryContainer
                                            },
                                            contentColor = when (notice.category) {
                                                "Propinas" -> AjafOnAmberContainer
                                                "Certificados" -> AjafOnEmeraldContainer
                                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                                            }
                                        )
                                        Text(
                                            AjafFormatters.formatDate(notice.createdAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(notice.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Por: ${notice.authorName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Minha Pauta
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (myGrade == null) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.Grading,
                                title = "Pauta ainda não disponível",
                                description = "Assim que a matrícula for activada e as avaliações lançadas, a sua pauta aparecerá aqui."
                            )
                        }
                    } else {
                        item {
                            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(myGrade.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("Formador: $trainerName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        StatusBadge(
                                            label = myGrade.academicStatus.labelPt,
                                            containerColor = when (myGrade.academicStatus) {
                                                AcademicStatus.APROVADO -> AjafEmeraldContainer
                                                AcademicStatus.REPROVADO -> AjafErrorContainer
                                                AcademicStatus.EM_CURSO -> AjafAmberContainer
                                            },
                                            contentColor = when (myGrade.academicStatus) {
                                                AcademicStatus.APROVADO -> AjafOnEmeraldContainer
                                                AcademicStatus.REPROVADO -> AjafOnErrorContainer
                                                AcademicStatus.EM_CURSO -> AjafOnAmberContainer
                                            }
                                        )
                                    }

                                    HorizontalDivider()

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        GradeMetricBox("Avaliação 1", AjafFormatters.formatGrade(myGrade.eval1), Modifier.weight(1f))
                                        GradeMetricBox("Avaliação 2", AjafFormatters.formatGrade(myGrade.eval2), Modifier.weight(1f))
                                        GradeMetricBox("Exame", AjafFormatters.formatGrade(myGrade.examGrade), Modifier.weight(1f))
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().padding(14.dp)
                                        ) {
                                            Text("Média Final (0 - 20)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${AjafFormatters.formatGrade(myGrade.finalAverage)} val.",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AjafNavyPrimary
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onOpenPautaPdf(myGrade.courseName, trainerName, listOf(myGrade)) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Baixar Minha Pauta (PDF)")
                                    }
                                }
                            }
                        }

                        if (myCert != null) {
                            item {
                                ElevatedCard(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.elevatedCardColors(containerColor = AjafEmeraldContainer.copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = AjafEmerald)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Estado do Certificado Oficial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Text("Código: ${myCert.certificateCode}", style = MaterialTheme.typography.labelMedium)
                                        Text(
                                            when {
                                                myCert.issuedBySuperadmin -> "Certificado oficialmente emitido pelo Superadministrador com assinatura do Director."
                                                myCert.directorApproved -> "Assinatura e carimbo aprovados pelo Director. Aguarda emissão final."
                                                else -> "Taxa de certificado liquidada. Aguarda aprovação da assinatura pelo Director."
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Propinas e Taxas Financeiras separadas + Multa 16% + Recibo PDF
                val mensalidades = myPayments.filter { it.feeType == FeeType.MENSALIDADE }
                val exameFees = myPayments.filter { it.feeType == FeeType.EXAME }
                val certFees = myPayments.filter { it.feeType == FeeType.CERTIFICADO }
                val enrollFees = myPayments.filter { it.feeType == FeeType.INSCRICAO }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Surface(
                            color = AjafAmberContainer.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp).fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = AjafOnAmberContainer)
                                Text(
                                    "Regra de Propinas: Mensalidades pagas após a data de vencimento recebem automaticamente multa de 16%.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AjafOnAmberContainer
                                )
                            }
                        }
                    }

                    if (mensalidades.isNotEmpty()) {
                        item { FeeSectionHeader("Mensalidades", Icons.Filled.CalendarMonth) }
                        items(mensalidades, key = { it.id }) { p ->
                            StudentPaymentCard(
                                payment = p,
                                onPay = { paymentTarget = p },
                                onDownloadReceipt = { onOpenReceiptPdf(p) }
                            )
                        }
                    }

                    if (exameFees.isNotEmpty()) {
                        item { FeeSectionHeader("Taxa de Exame", Icons.Filled.FactCheck) }
                        items(exameFees, key = { it.id }) { p ->
                            StudentPaymentCard(
                                payment = p,
                                onPay = { paymentTarget = p },
                                onDownloadReceipt = { onOpenReceiptPdf(p) }
                            )
                        }
                    }

                    if (certFees.isNotEmpty()) {
                        item { FeeSectionHeader("Taxa de Certificado", Icons.Filled.WorkspacePremium) }
                        items(certFees, key = { it.id }) { p ->
                            StudentPaymentCard(
                                payment = p,
                                onPay = { paymentTarget = p },
                                onDownloadReceipt = { onOpenReceiptPdf(p) }
                            )
                        }
                    }

                    if (enrollFees.isNotEmpty()) {
                        item { FeeSectionHeader("Inscrição", Icons.Filled.AppRegistration) }
                        items(enrollFees, key = { it.id }) { p ->
                            StudentPaymentCard(
                                payment = p,
                                onPay = { paymentTarget = p },
                                onDownloadReceipt = { onOpenReceiptPdf(p) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (paymentTarget != null) {
        val target = paymentTarget!!
        PaymentSubmissionDialog(
            referenceLabel = target.referenceLabel,
            baseAmountMzn = target.baseAmountMzn,
            lateFeeApplied = target.lateFeeApplied,
            lateFeeAmountMzn = target.lateFeeAmountMzn,
            totalAmountMzn = target.totalAmountMzn,
            onDismiss = { paymentTarget = null },
            onSubmit = { method, txId, sms, uri ->
                paymentTarget = null
                onSubmitPayment(target.id, method, txId, sms, uri)
            }
        )
    }
}

@Composable
private fun GradeMetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FeeSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
        Icon(icon, contentDescription = null, tint = AjafNavyPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StudentPaymentCard(
    payment: PaymentRecordEntity,
    onPay: () -> Unit,
    onDownloadReceipt: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(payment.referenceLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                StatusBadge(
                    label = payment.status.labelPt,
                    containerColor = when (payment.status) {
                        PaymentStatus.APROVADO -> AjafEmeraldContainer
                        PaymentStatus.PENDENTE -> AjafAmberContainer
                        PaymentStatus.REJEITADO -> AjafErrorContainer
                        PaymentStatus.POR_PAGAR -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = when (payment.status) {
                        PaymentStatus.APROVADO -> AjafOnEmeraldContainer
                        PaymentStatus.PENDENTE -> AjafOnAmberContainer
                        PaymentStatus.REJEITADO -> AjafOnErrorContainer
                        PaymentStatus.POR_PAGAR -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                "Vencimento: ${AjafFormatters.formatDate(payment.dueDateMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Valor Base: ${AjafFormatters.formatMzn(payment.baseAmountMzn)}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Total: ${AjafFormatters.formatMzn(payment.totalAmountMzn)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (payment.lateFeeApplied) AjafAmber else AjafNavyPrimary
                )
            }

            if (payment.lateFeeApplied) {
                StatusBadge(
                    label = "Multa de 16% aplicada por atraso (+${AjafFormatters.formatMzn(payment.lateFeeAmountMzn)})",
                    containerColor = AjafAmberContainer,
                    contentColor = AjafOnAmberContainer,
                    icon = Icons.Filled.Warning
                )
            }

            if (payment.status == PaymentStatus.APROVADO) {
                Button(
                    onClick = onDownloadReceipt,
                    colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("student_download_receipt_${payment.id}")
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Baixar Recibo (PDF)")
                }
            } else if (payment.status != PaymentStatus.PENDENTE && !payment.lockedByAdmin) {
                Button(
                    onClick = onPay,
                    colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("student_pay_${payment.id}")
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (payment.status == PaymentStatus.REJEITADO) "Reenviar Comprovativo" else "Pagar / Enviar Comprovativo")
                }
            } else if (payment.status == PaymentStatus.PENDENTE) {
                Text(
                    "Comprovativo (${payment.paymentMethod?.labelPt ?: "Numerário"}) aguarda validação da Secretaria.",
                    style = MaterialTheme.typography.labelMedium,
                    color = AjafAmber
                )
            }
        }
    }
}
