package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.LibraryDocumentEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@Composable
fun LibraryScreen(
    uiState: AjafUiState,
    onDownloadPdf: (LibraryDocumentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var previewDoc by remember { mutableStateOf<LibraryDocumentEntity?>(null) }

    val categories = remember(uiState.libraryDocs) {
        listOf("Todas") + uiState.libraryDocs.map { it.category }.distinct()
    }

    val filteredDocs = remember(uiState.libraryDocs, searchQuery, selectedCategory) {
        uiState.libraryDocs.filter { doc ->
            val matchesCat = selectedCategory == "Todas" || doc.category == selectedCategory
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                doc.title.lowercase().contains(q) ||
                doc.description.lowercase().contains(q) ||
                doc.courseName.lowercase().contains(q) ||
                doc.category.lowercase().contains(q)
            matchesCat && matchesSearch
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_library_ajaf),
                        contentDescription = "Biblioteca Técnica Digital AJAF",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(AjafNavyDeep.copy(alpha = 0.55f), AjafNavyDeep.copy(alpha = 0.92f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        StatusBadge(
                            label = "Acesso Público Gratuito",
                            containerColor = AjafEmerald,
                            contentColor = Color.White,
                            icon = Icons.Filled.Public
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Biblioteca Técnica Digital",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Fichas, livros e manuais profissionais prontos para download em PDF.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // Pesquisa
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Pesquisar por título, tema ou curso") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpar pesquisa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input")
            )
        }

        // Filtro por área/categoria
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        leadingIcon = if (selectedCategory == cat) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        if (filteredDocs.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.MenuBook,
                    title = "Nenhum manual encontrado",
                    description = "Tente outro termo de pesquisa ou seleccione 'Todas' as categorias."
                )
            }
        } else {
            items(filteredDocs, key = { it.id }) { doc ->
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { previewDoc = doc }
                        .testTag("library_doc_card_${doc.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatusBadge(
                                label = doc.category,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${doc.pageCount} págs • ${doc.publishedDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Curso: ${doc.courseName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AjafEmerald
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = doc.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { previewDoc = doc },
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Pré-visualizar")
                            }
                            Button(
                                onClick = { onDownloadPdf(doc) },
                                colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("library_download_pdf_${doc.id}")
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Baixar PDF")
                            }
                        }
                    }
                }
            }
        }
    }

    if (previewDoc != null) {
        val doc = previewDoc!!
        AlertDialog(
            onDismissRequest = { previewDoc = null },
            icon = { Icon(Icons.Filled.MenuBook, contentDescription = null, tint = AjafNavyPrimary) },
            title = { Text(doc.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(
                        label = "${doc.category} • ${doc.courseName}",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text("Autor/Fonte: ${doc.authorOrSource}", style = MaterialTheme.typography.labelMedium)
                    Text("Publicado em: ${doc.publishedDate} (${doc.pageCount} páginas)", style = MaterialTheme.typography.labelSmall)
                    HorizontalDivider()
                    Text(doc.description, style = MaterialTheme.typography.bodySmall)
                    Text("Índice / Conteúdo:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(doc.contentSummary, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        previewDoc = null
                        onDownloadPdf(doc)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Baixar em PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewDoc = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}
