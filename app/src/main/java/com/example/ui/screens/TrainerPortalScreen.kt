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
import androidx.compose.ui.unit.dp
import com.example.data.local.CourseEntity
import com.example.data.local.GradeRecordEntity
import com.example.data.model.AcademicStatus
import com.example.data.model.ActiveSession
import com.example.data.model.AjafFormatters
import com.example.data.model.EnrollmentStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@Composable
fun TrainerPortalScreen(
    session: ActiveSession.StaffSession,
    uiState: AjafUiState,
    onSaveGrade: (GradeRecordEntity, eval1: Double?, eval2: Double?, exam: Double?) -> Unit,
    onOpenPautaPdf: (courseName: String, trainerName: String, grades: List<GradeRecordEntity>) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myCourses = remember(uiState.courses, session.staffId) {
        uiState.courses.filter { it.trainerId == session.staffId }
    }
    var selectedCourseId by remember(myCourses) { mutableStateOf(myCourses.firstOrNull()?.id) }
    val selectedCourse = myCourses.find { it.id == selectedCourseId }

    val activeStudentIds = remember(uiState.students) {
        uiState.students.filter { it.status == EnrollmentStatus.ACTIVO || it.status == EnrollmentStatus.CONCLUIDO }
            .map { it.id }.toSet()
    }
    val courseGrades = remember(uiState.grades, selectedCourseId, activeStudentIds) {
        uiState.grades.filter { it.courseId == selectedCourseId && it.studentId in activeStudentIds }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        Surface(color = AjafNavyDeep, contentColor = Color.White, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Portal do Formador", style = MaterialTheme.typography.labelMedium, color = AjafEmeraldBright)
                    Text(session.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${myCourses.size} curso(s) atribuído(s)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                }
                FilledTonalButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.14f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("trainer_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sair")
                }
            }
        }

        if (myCourses.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    icon = Icons.Filled.CastForEducation,
                    title = "Nenhum curso atribuído",
                    description = "A Secretaria ou o Superadministrador ainda não o associou a nenhum curso."
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text("Meus Cursos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(myCourses, key = { it.id }) { course ->
                            FilterChip(
                                selected = selectedCourseId == course.id,
                                onClick = { selectedCourseId = course.id },
                                label = { Text(course.name) },
                                leadingIcon = if (course.gradesLocked) {
                                    { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                if (selectedCourse != null) {
                    if (selectedCourse.gradesLocked) {
                        item {
                            Surface(
                                color = AjafAmberContainer,
                                contentColor = AjafOnAmberContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null)
                                    Text(
                                        "Pauta marcada como \"Não Editável\" pelo Superadministrador. Apenas leitura.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Lançamento de Pauta (${courseGrades.size} alunos)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { onOpenPautaPdf(selectedCourse.name, session.fullName, courseGrades) },
                                enabled = courseGrades.isNotEmpty()
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Pauta PDF")
                            }
                        }
                    }

                    if (courseGrades.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.GroupOff,
                                title = "Sem formandos activos",
                                description = "Não existem alunos com inscrição aprovada neste curso até ao momento."
                            )
                        }
                    } else {
                        items(courseGrades, key = { it.id }) { grade ->
                            TrainerGradeEntryCard(
                                grade = grade,
                                courseLocked = selectedCourse.gradesLocked || grade.lockedByAdmin,
                                onSave = { e1, e2, ex -> onSaveGrade(grade, e1, e2, ex) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainerGradeEntryCard(
    grade: GradeRecordEntity,
    courseLocked: Boolean,
    onSave: (Double?, Double?, Double?) -> Unit
) {
    var eval1Text by remember(grade.id, grade.eval1) { mutableStateOf(grade.eval1?.toString() ?: "") }
    var eval2Text by remember(grade.id, grade.eval2) { mutableStateOf(grade.eval2?.toString() ?: "") }
    var examText by remember(grade.id, grade.examGrade) { mutableStateOf(grade.examGrade?.toString() ?: "") }

    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(grade.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(grade.studentNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(
                    label = "${grade.academicStatus.labelPt} (${AjafFormatters.formatGrade(grade.finalAverage)})",
                    containerColor = when (grade.academicStatus) {
                        AcademicStatus.APROVADO -> AjafEmeraldContainer
                        AcademicStatus.REPROVADO -> AjafErrorContainer
                        AcademicStatus.EM_CURSO -> AjafAmberContainer
                    },
                    contentColor = when (grade.academicStatus) {
                        AcademicStatus.APROVADO -> AjafOnEmeraldContainer
                        AcademicStatus.REPROVADO -> AjafOnErrorContainer
                        AcademicStatus.EM_CURSO -> AjafOnAmberContainer
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = eval1Text,
                    onValueChange = { eval1Text = it.replace(",", ".") },
                    label = { Text("Av. 1") },
                    enabled = !courseLocked,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = eval2Text,
                    onValueChange = { eval2Text = it.replace(",", ".") },
                    label = { Text("Av. 2") },
                    enabled = !courseLocked,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = examText,
                    onValueChange = { examText = it.replace(",", ".") },
                    label = { Text("Exame") },
                    enabled = !courseLocked,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    onSave(
                        eval1Text.toDoubleOrNull(),
                        eval2Text.toDoubleOrNull(),
                        examText.toDoubleOrNull()
                    )
                },
                enabled = !courseLocked,
                colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(if (courseLocked) Icons.Filled.Lock else Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (courseLocked) "Pauta Bloqueada" else "Gravar Notas")
            }
        }
    }
}
