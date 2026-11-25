package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.CertificateRecordEntity
import com.example.data.local.GradeRecordEntity
import com.example.data.model.*
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.KpiCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@Composable
fun DirectorPortalScreen(
    session: ActiveSession.StaffSession,
    uiState: AjafUiState,
    onApproveCertificate: (CertificateRecordEntity) -> Unit,
    onOpenAcademicPerformancePdf: (courseFilterName: String, grades: List<GradeRecordEntity>) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

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
                    Text("Portal da Direcção", style = MaterialTheme.typography.labelMedium, color = AjafEmeraldBright)
                    Text(session.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Desempenho Académico & Validação de Certificados", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                }
                FilledTonalButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.14f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("director_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sair")
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Desempenho Académico") },
                icon = { Icon(Icons.Filled.BarChart, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Aprovar Certificados") },
                icon = { Icon(Icons.Filled.Verified, contentDescription = null) }
            )
        }

        when (selectedTab) {
            0 -> AcademicPerformanceTab(
                uiState = uiState,
                onExportPdf = onOpenAcademicPerformancePdf
            )
            1 -> CertificateApprovalTab(
                uiState = uiState,
                onApproveCertificate = onApproveCertificate
            )
        }
    }
}

@Composable
private fun AcademicPerformanceTab(
    uiState: AjafUiState,
    onExportPdf: (String, List<GradeRecordEntity>) -> Unit
) {
    var selectedCourseId by remember { mutableStateOf("TODOS") }
    val filteredGrades = remember(uiState.grades, selectedCourseId) {
        if (selectedCourseId == "TODOS") uiState.grades
        else uiState.grades.filter { it.courseId == selectedCourseId }
    }
    val evaluated = filteredGrades.filter { it.finalAverage != null }
    val approvedCount = evaluated.count { it.academicStatus == AcademicStatus.APROVADO }
    val approvalRate = if (evaluated.isNotEmpty()) (approvedCount * 100.0 / evaluated.size) else 0.0
    val averageScore = if (evaluated.isNotEmpty()) evaluated.mapNotNull { it.finalAverage }.average() else 0.0
    val selectedCourseName = if (selectedCourseId == "TODOS") "Todos os Cursos" else {
        uiState.courses.find { it.id == selectedCourseId }?.name ?: "Curso"
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCourseId == "TODOS",
                        onClick = { selectedCourseId = "TODOS" },
                        label = { Text("Todos os Cursos") }
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

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Taxa de Aprovação",
                    value = String.format("%.0f%%", approvalRate),
                    icon = Icons.Filled.TrendingUp,
                    accentColor = AjafEmerald,
                    subtitle = "$approvedCount de ${evaluated.size} avaliados",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Média Geral",
                    value = if (evaluated.isEmpty()) "--" else String.format("%.1f", averageScore),
                    icon = Icons.Filled.School,
                    accentColor = AjafNavyPrimary,
                    subtitle = "Escala 0 - 20 val.",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Gráfico de Barras por Curso (Canvas)
        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Aproveitamento por Curso (Média / 20)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val courseBars = uiState.courses.map { course ->
                        val g = uiState.grades.filter { it.courseId == course.id && it.finalAverage != null }
                        val avg = if (g.isNotEmpty()) g.mapNotNull { it.finalAverage }.average().toFloat() else 0f
                        course.name to avg
                    }

                    if (courseBars.all { it.second == 0f }) {
                        Text(
                            "Ainda não há notas finais lançadas suficientes para o gráfico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(top = 8.dp)
                        ) {
                            val count = courseBars.size.coerceAtLeast(1)
                            val slotWidth = size.width / count
                            val barWidth = (slotWidth * 0.52f).coerceAtMost(64f)
                            courseBars.forEachIndexed { idx, (_, avg) ->
                                val ratio = (avg / 20f).coerceIn(0f, 1f)
                                val barHeight = size.height * ratio
                                val x = idx * slotWidth + (slotWidth - barWidth) / 2f
                                val y = size.height - barHeight
                                drawRoundRect(
                                    color = if (avg >= 10f) AjafEmerald else AjafAmber,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight.coerceAtLeast(6f)),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            courseBars.forEach { (name, avg) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(
                                        String.format("%.1f", avg),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        name.split(" ").firstOrNull() ?: name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { onExportPdf(selectedCourseName, filteredGrades) },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Exportar Pauta de Desempenho (PDF)")
                    }
                }
            }
        }

        item {
            Text("Tabela de Desempenho dos Formandos (${filteredGrades.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(filteredGrades, key = { it.id }) { g ->
            ElevatedCard(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(g.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${g.studentNumber} • ${g.courseName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "Av1: ${AjafFormatters.formatGrade(g.eval1)} | Av2: ${AjafFormatters.formatGrade(g.eval2)} | Exame: ${AjafFormatters.formatGrade(g.examGrade)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            AjafFormatters.formatGrade(g.finalAverage),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AjafNavyPrimary
                        )
                        StatusBadge(
                            label = g.academicStatus.labelPt,
                            containerColor = when (g.academicStatus) {
                                AcademicStatus.APROVADO -> AjafEmeraldContainer
                                AcademicStatus.REPROVADO -> AjafErrorContainer
                                AcademicStatus.EM_CURSO -> AjafAmberContainer
                            },
                            contentColor = when (g.academicStatus) {
                                AcademicStatus.APROVADO -> AjafOnEmeraldContainer
                                AcademicStatus.REPROVADO -> AjafOnErrorContainer
                                AcademicStatus.EM_CURSO -> AjafOnAmberContainer
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateApprovalTab(
    uiState: AjafUiState,
    onApproveCertificate: (CertificateRecordEntity) -> Unit
) {
    // Apenas certificados cuja Taxa de Certificado já foi aprovada pela Secretaria
    val eligibleCerts = remember(uiState.certificates, uiState.payments) {
        uiState.certificates.filter { cert ->
            uiState.payments.any { p ->
                p.studentId == cert.studentId &&
                    p.feeType == FeeType.CERTIFICADO &&
                    p.status == PaymentStatus.APROVADO
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (eligibleCerts.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.WorkspacePremium,
                    title = "Sem certificados pendentes de assinatura",
                    description = "Os alunos concluídos com Taxa de Certificado aprovada pela Secretaria aparecerão aqui para assinatura e carimbo."
                )
            }
        } else {
            items(eligibleCerts, key = { it.id }) { cert ->
                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cert.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${cert.studentNumber} • ${cert.courseName}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusBadge(
                                label = if (cert.directorApproved) "Assinado & Carimbado" else "Aguarda Assinatura",
                                containerColor = if (cert.directorApproved) AjafEmeraldContainer else AjafAmberContainer,
                                contentColor = if (cert.directorApproved) AjafOnEmeraldContainer else AjafOnAmberContainer
                            )
                        }
                        Text(
                            "Carga Horária: ${cert.durationHours}h  •  Nota Final: ${AjafFormatters.formatGrade(cert.finalGrade)} val.  •  Código: ${cert.certificateCode}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        StatusBadge(
                            label = "Taxa de Certificado: Aprovada pela Secretaria",
                            containerColor = AjafEmeraldContainer.copy(alpha = 0.65f),
                            contentColor = AjafOnEmeraldContainer,
                            icon = Icons.Filled.CheckCircle
                        )

                        if (!cert.directorApproved) {
                            Button(
                                onClick = { onApproveCertificate(cert) },
                                colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Filled.Draw, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Aprovar Assinatura e Carimbo")
                            }
                        } else {
                            Text(
                                "Validado por ${cert.directorName ?: "Director"} em ${cert.directorApprovedAt?.let { AjafFormatters.formatDateTime(it) } ?: "—"}. Disponível para emissão oficial pelo Superadministrador.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AjafEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}
