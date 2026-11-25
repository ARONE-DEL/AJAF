package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CourseEntity
import com.example.data.local.VacancyAnnouncementEntity
import com.example.data.model.AjafFormatters
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@Composable
fun PublicHomeScreen(
    uiState: AjafUiState,
    onNavigateToLibrary: () -> Unit,
    onNavigateToEnroll: (courseId: String?) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCourses = uiState.courses.filter { it.active }
    val activeVacancies = uiState.vacancies.filter { it.active }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        // Hero Institutional Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_ajaf),
                        contentDescription = "Centro de Formação Profissional AJAF",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        AjafNavyDeep.copy(alpha = 0.72f),
                                        AjafNavyDeep.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_ajaf_emblem),
                                contentDescription = "Logotipo AJAF",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AJAF — Moçambique",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AjafEmeraldBright,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Associação para Jovens e Adolescentes para o Futuro",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Formação Profissional de Curta Duração & Biblioteca Técnica Digital",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Capacitação prática para o emprego, empreendedorismo e acesso gratuito a manuais técnicos em Português.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onNavigateToEnroll(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("home_enroll_cta")
                            ) {
                                Icon(Icons.Filled.AppRegistration, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Inscrever-se", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = onNavigateToLogin,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("home_login_cta")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Portal", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Indicadores Rápidos
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickStatPill(
                    label = "Cursos Activos",
                    value = "${activeCourses.size}",
                    icon = Icons.Filled.School,
                    color = AjafNavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                QuickStatPill(
                    label = "Vagas Abertas",
                    value = "${activeVacancies.sumOf { it.vacanciesCount }}",
                    icon = Icons.Filled.Campaign,
                    color = AjafEmerald,
                    modifier = Modifier.weight(1f)
                )
                QuickStatPill(
                    label = "Manuais PDF",
                    value = "${uiState.libraryDocs.size}",
                    icon = Icons.Filled.MenuBook,
                    color = AjafAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Acesso rápido à Biblioteca Técnica Digital Pública
        item {
            ElevatedCard(
                onClick = onNavigateToLibrary,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_library_banner_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AjafNavyDeep)
                    ) {
                        Icon(Icons.Filled.AutoStories, contentDescription = null, tint = AjafEmeraldBright)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Biblioteca Técnica Digital Pública",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Consulte e descarregue gratuitamente fichas, livros e manuais em PDF sem precisar de login.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Abrir biblioteca")
                }
            }
        }

        // Secção: Anúncios de Vagas de Inscrição
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        "Anúncios de Vagas de Inscrição",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Cursos com inscrições abertas em Moçambique",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(
                    label = "${activeVacancies.size} abertos",
                    containerColor = AjafEmeraldContainer,
                    contentColor = AjafOnEmeraldContainer
                )
            }
        }

        if (activeVacancies.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.Campaign,
                    title = "Sem anúncios publicados",
                    description = "O Superadministrador ainda não publicou novas vagas para este ciclo."
                )
            }
        } else {
            items(activeVacancies, key = { it.id }) { vacancy ->
                val course = uiState.courses.find { it.id == vacancy.courseId }
                VacancyCard(
                    vacancy = vacancy,
                    course = course,
                    onEnrollNow = { onNavigateToEnroll(vacancy.courseId) }
                )
            }
        }

        // Sobre a Instituição
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = AjafEmerald)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Sobre a AJAF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A Associação para Jovens e Adolescentes para o Futuro (AJAF) promove a inclusão produtiva da juventude através de cursos técnicos de curta duração (modalidade Online e Presencial), acompanhamento pedagógico transparente e certificação oficial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Pagamentos aceites: M-Pesa, E-Mola e Numerário • Moeda: Meticais (MT / MZN)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun VacancyCard(
    vacancy: VacancyAnnouncementEntity,
    course: CourseEntity?,
    onEnrollNow: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatusBadge(
                    label = course?.category ?: "Formação Profissional",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatusBadge(
                    label = "${vacancy.vacanciesCount} vagas",
                    containerColor = AjafEmeraldContainer,
                    contentColor = AjafOnEmeraldContainer,
                    icon = Icons.Filled.Group
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = vacancy.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = vacancy.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = AjafNavyPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${vacancy.startDate} – ${vacancy.endDate}", style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LaptopMac, contentDescription = null, tint = AjafEmerald, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(vacancy.modalities, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (course != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Inscrição: ${AjafFormatters.formatMzn(course.enrollmentFeeMzn)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text("Mensalidade: ${AjafFormatters.formatMzn(course.monthlyFeeMzn)}", style = MaterialTheme.typography.labelMedium, color = AjafNavyPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onEnrollNow,
                colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("vacancy_enroll_${vacancy.id}")
            ) {
                Text("Inscrever-se Agora")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
