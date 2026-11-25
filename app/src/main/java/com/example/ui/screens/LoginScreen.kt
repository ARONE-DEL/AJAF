package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@Composable
fun LoginScreen(
    uiState: AjafUiState,
    onLoginStudent: (identifier: String, phone: String) -> Unit,
    onLoginStaff: (email: String, password: String, role: UserRole) -> Unit,
    onRegisterFirstSuperadmin: (fullName: String, email: String, password: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val needsInitialSuperadmin = uiState.superadminCount == 0
    var selectedRole by remember { mutableStateOf(UserRole.ALUNO) }

    // Campos de Aluno (Sem e-mail)
    var studentIdentifier by remember { mutableStateOf("") }
    var studentPhone by remember { mutableStateOf("") }

    // Campos de Equipa / Admin (Com e-mail e palavra-passe)
    var staffEmail by remember { mutableStateOf("") }
    var staffPassword by remember { mutableStateOf("") }
    var showStaffPassword by remember { mutableStateOf(false) }

    // Registo inicial do primeiro Superadministrador
    var showSuperadminSetup by remember(needsInitialSuperadmin) { mutableStateOf(false) }
    var setupName by remember { mutableStateOf("") }
    var setupEmail by remember { mutableStateOf("") }
    var setupPassword by remember { mutableStateOf("") }
    var setupConfirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho Institucional
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.img_ajaf_emblem),
                contentDescription = "Logotipo AJAF",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Portal Institucional AJAF", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Seleccione o seu perfil de acesso",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Regra do Primeiro Utilizador: Superadministrador Inicial
        if (needsInitialSuperadmin) {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = AjafAmberContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = AjafOnAmberContainer)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Configuração Inicial do Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AjafOnAmberContainer
                        )
                    }
                    Text(
                        "Ainda não existe nenhum administrador registado. A primeira conta administrativa criada torna-se automaticamente o Superadministrador e bloqueia novos auto-registos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AjafOnAmberContainer
                    )
                    Button(
                        onClick = { showSuperadminSetup = !showSuperadminSetup },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafNavyDeep),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("toggle_first_superadmin_setup")
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (showSuperadminSetup) "Ocultar Formulário de Configuração" else "Criar Conta de Superadministrador")
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = AjafEmerald, modifier = Modifier.size(18.dp))
                    Text(
                        "Superadministrador já configurado. Contas internas (Secretaria, Director, Formador) são criadas apenas no painel administrativo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Formulário de Criação do Primeiro Superadministrador (se aberto e ainda permitido)
        if (needsInitialSuperadmin && showSuperadminSetup) {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Registar Primeiro Superadministrador",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = setupName,
                        onValueChange = { setupName = it },
                        label = { Text("Nome Completo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("setup_sa_name")
                    )
                    OutlinedTextField(
                        value = setupEmail,
                        onValueChange = { setupEmail = it },
                        label = { Text("E-mail Institucional *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("setup_sa_email")
                    )
                    OutlinedTextField(
                        value = setupPassword,
                        onValueChange = { setupPassword = it },
                        label = { Text("Palavra-passe (mín. 6 caracteres) *") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("setup_sa_password")
                    )
                    OutlinedTextField(
                        value = setupConfirmPassword,
                        onValueChange = { setupConfirmPassword = it },
                        label = { Text("Confirmar Palavra-passe *") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("setup_sa_confirm_password")
                    )
                    if (localError != null) {
                        Text(localError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = {
                            localError = null
                            when {
                                setupName.isBlank() -> localError = "Informe o nome completo."
                                !setupEmail.contains("@") -> localError = "Informe um e-mail válido."
                                setupPassword.length < 6 -> localError = "A palavra-passe deve ter pelo menos 6 caracteres."
                                setupPassword != setupConfirmPassword -> localError = "As palavras-passe não coincidem."
                                else -> onRegisterFirstSuperadmin(setupName, setupEmail, setupPassword)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("setup_sa_submit")
                    ) {
                        Text("Activar Superadministrador")
                    }
                }
            }
        }

        // Selector dos 5 Perfis
        Text("Perfil de Acesso", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(UserRole.entries) { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = {
                        selectedRole = role
                        localError = null
                    },
                    label = { Text(role.labelPt) },
                    leadingIcon = {
                        Icon(
                            when (role) {
                                UserRole.ALUNO -> Icons.Filled.Person
                                UserRole.FORMADOR -> Icons.Filled.CastForEducation
                                UserRole.SECRETARIA -> Icons.Filled.ReceiptLong
                                UserRole.DIRECTOR -> Icons.Filled.WorkspacePremium
                                UserRole.SUPERADMIN -> Icons.Filled.AdminPanelSettings
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("role_chip_${role.name}")
                )
            }
        }

        // Cartão de Login
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Entrar como ${selectedRole.labelPt}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(
                        label = if (selectedRole == UserRole.ALUNO) "Sem e-mail" else "E-mail + Senha",
                        containerColor = if (selectedRole == UserRole.ALUNO) AjafEmeraldContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (selectedRole == UserRole.ALUNO) AjafOnEmeraldContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (selectedRole == UserRole.ALUNO) {
                    OutlinedTextField(
                        value = studentIdentifier,
                        onValueChange = { studentIdentifier = it },
                        label = { Text("Nome Completo ou Nº de Estudante *") },
                        placeholder = { Text("Ex: Armando Sitoe ou AJAF-2026-0001") },
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_student_identifier")
                    )
                    OutlinedTextField(
                        value = studentPhone,
                        onValueChange = { studentPhone = it },
                        label = { Text("Contacto Telefónico *") },
                        placeholder = { Text("Ex: 841234567") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_student_phone")
                    )

                    // Atalho útil para testar os alunos de demonstração
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Contas de demonstração disponíveis para teste:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(
                                    onClick = {
                                        studentIdentifier = "AJAF-2026-0001"
                                        studentPhone = "841234567"
                                    },
                                    label = { Text("Armando (Multa 16% + Pauta)") }
                                )
                                AssistChip(
                                    onClick = {
                                        studentIdentifier = "AJAF-2026-0002"
                                        studentPhone = "829876543"
                                    },
                                    label = { Text("Celeste (Concluída)") }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            localError = null
                            if (studentIdentifier.isBlank() || studentPhone.isBlank()) {
                                localError = "Preencha o Nome Completo/Nº de Estudante e o Contacto Telefónico."
                            } else {
                                onLoginStudent(studentIdentifier, studentPhone)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_student_submit")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Aceder ao Portal do Aluno")
                    }
                } else {
                    OutlinedTextField(
                        value = staffEmail,
                        onValueChange = { staffEmail = it },
                        label = { Text("E-mail Institucional *") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_staff_email")
                    )
                    OutlinedTextField(
                        value = staffPassword,
                        onValueChange = { staffPassword = it },
                        label = { Text("Palavra-passe *") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showStaffPassword = !showStaffPassword }) {
                                Icon(
                                    if (showStaffPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Mostrar ou ocultar senha"
                                )
                            }
                        },
                        visualTransformation = if (showStaffPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_staff_password")
                    )

                    if (localError != null) {
                        Text(localError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            localError = null
                            if (staffEmail.isBlank() || staffPassword.isBlank()) {
                                localError = "Informe o e-mail e a palavra-passe."
                            } else {
                                onLoginStaff(staffEmail, staffPassword, selectedRole)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_staff_submit")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Entrar como ${selectedRole.labelPt}")
                    }
                }
            }
        }
    }
}
