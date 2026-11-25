package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.StudentEnrollmentEntity
import com.example.data.model.AjafFormatters
import com.example.data.model.MozambiqueData
import com.example.data.model.PaymentMethod
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AjafUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentScreen(
    uiState: AjafUiState,
    onSubmitEnrollment: (
        fullName: String,
        birthDate: String,
        nationality: String,
        province: String,
        courseId: String,
        modality: String,
        phone: String,
        paymentMethod: PaymentMethod,
        transactionId: String?,
        smsText: String?,
        screenshotUri: Uri?,
        onSuccessProtocol: (StudentEnrollmentEntity) -> Unit
    ) -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCourses = remember(uiState.courses) { uiState.courses.filter { it.active } }

    var fullName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("Moçambicana") }
    var province by remember { mutableStateOf(MozambiqueData.provinces.first()) }
    var provinceExpanded by remember { mutableStateOf(false) }

    var selectedCourseId by remember(activeCourses, uiState.preselectedCourseId) {
        mutableStateOf(
            uiState.preselectedCourseId?.takeIf { id -> activeCourses.any { it.id == id } }
                ?: activeCourses.firstOrNull()?.id.orEmpty()
        )
    }
    var courseExpanded by remember { mutableStateOf(false) }
    val selectedCourse = activeCourses.find { it.id == selectedCourseId }

    var modality by remember { mutableStateOf("Presencial") }
    var phone by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.M_PESA) }
    var transactionId by remember { mutableStateOf("") }
    var smsText by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }

    var localError by remember { mutableStateOf<String?>(null) }
    var createdProtocol by remember { mutableStateOf<StudentEnrollmentEntity?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) screenshotUri = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 760.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabeçalho
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AjafNavyDeep)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                StatusBadge(
                    label = "Sem necessidade de e-mail",
                    containerColor = AjafEmerald,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    icon = Icons.Filled.CheckCircle
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Inscrição Online de Novos Alunos",
                    style = MaterialTheme.typography.headlineSmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Preencha os seus dados pessoais, escolha o curso e anexe o comprovativo de inscrição. A Secretaria valida o seu pedido.",
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color(0xFFCBD5E1)
                )
            }
        }

        // Dados Pessoais
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text("1. Dados Pessoais do Candidato", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nome Completo *") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("enroll_fullname_input")
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Data de Nascimento (DD/MM/AAAA) *") },
                    placeholder = { Text("Ex: 15/06/2003") },
                    leadingIcon = { Icon(Icons.Filled.Cake, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Escolher data")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("enroll_birthdate_input")
                )

                OutlinedTextField(
                    value = nationality,
                    onValueChange = { nationality = it },
                    label = { Text("Nacionalidade *") },
                    leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("enroll_nationality_input")
                )

                // Províncias de Moçambique
                ExposedDropdownMenuBox(
                    expanded = provinceExpanded,
                    onExpandedChange = { provinceExpanded = !provinceExpanded }
                ) {
                    OutlinedTextField(
                        value = province,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Província (Moçambique) *") },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("enroll_province_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = provinceExpanded,
                        onDismissRequest = { provinceExpanded = false }
                    ) {
                        MozambiqueData.provinces.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    province = p
                                    provinceExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contacto Telefónico (Para Login e Contactos) *") },
                    placeholder = { Text("Ex: 841234567") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("enroll_phone_input")
                )
            }
        }

        // Curso e Modalidade
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text("2. Curso e Modalidade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                ExposedDropdownMenuBox(
                    expanded = courseExpanded,
                    onExpandedChange = { courseExpanded = !courseExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCourse?.name ?: "Seleccione um curso",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Curso a se inscrever *") },
                        leadingIcon = { Icon(Icons.Filled.School, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("enroll_course_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = courseExpanded,
                        onDismissRequest = { courseExpanded = false }
                    ) {
                        activeCourses.forEach { c ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(c.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Inscrição: ${AjafFormatters.formatMzn(c.enrollmentFeeMzn)} • Mensalidade: ${AjafFormatters.formatMzn(c.monthlyFeeMzn)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCourseId = c.id
                                    courseExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedCourse != null) {
                    Surface(
                        color = AjafEmeraldContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(
                                "Taxa de Inscrição: ${AjafFormatters.formatMzn(selectedCourse.enrollmentFeeMzn)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AjafOnEmeraldContainer
                            )
                            Text(
                                "Duração: ${selectedCourse.durationHours}h (${selectedCourse.durationMonths} meses) • Mensalidade: ${AjafFormatters.formatMzn(selectedCourse.monthlyFeeMzn)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AjafOnEmeraldContainer
                            )
                        }
                    }
                }

                Text("Modalidade *", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Presencial", "Online").forEach { mod ->
                        FilterChip(
                            selected = modality == mod,
                            onClick = { modality = mod },
                            label = { Text(mod) },
                            leadingIcon = {
                                Icon(
                                    if (mod == "Online") Icons.Filled.Wifi else Icons.Filled.Apartment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                    }
                }
            }
        }

        // Pagamento da Inscrição
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text("3. Pagamento da Inscrição", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                PaymentMethod.entries.forEach { method ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (paymentMethod == method) AjafNavyPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        color = if (paymentMethod == method) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { paymentMethod = method }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method }
                            )
                            Text(method.labelPt, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Regra Condicional para E-Mola e M-Pesa
                if (paymentMethod.requiresDigitalProof) {
                    Surface(
                        color = AjafAmberContainer.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Obrigatório para ${paymentMethod.labelPt}: Informe o ID da transacção, cole a SMS de confirmação e anexe a captura de ecrã.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AjafOnAmberContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("ID da Transacção *") },
                        placeholder = { Text("Ex: MP2611220019 ou EM998123") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("enroll_tx_id_input")
                    )

                    OutlinedTextField(
                        value = smsText,
                        onValueChange = { smsText = it },
                        label = { Text("Texto da Mensagem SMS de Confirmação *") },
                        placeholder = { Text("Cole aqui o texto completo da SMS da M-Pesa ou E-Mola...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("enroll_sms_input")
                    )

                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("enroll_pick_screenshot_button")
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (screenshotUri == null) "Anexar Captura de Ecrã (Screenshot) *" else "Alterar Captura de Ecrã")
                    }

                    if (screenshotUri != null) {
                        AsyncImage(
                            model = screenshotUri,
                            contentDescription = "Screenshot do comprovativo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Pagamento em Numerário: Após submeter a inscrição, efectue o pagamento na Secretaria da AJAF apresentando o seu número de protocolo.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        if (localError != null) {
            Surface(
                color = AjafErrorContainer,
                contentColor = AjafOnErrorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(localError!!, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp).fillMaxWidth())
            }
        }

        Button(
            onClick = {
                localError = null
                when {
                    fullName.isBlank() -> localError = "Preencha o Nome Completo."
                    birthDate.isBlank() -> localError = "Preencha a Data de Nascimento."
                    nationality.isBlank() -> localError = "Preencha a Nacionalidade."
                    phone.filter { it.isDigit() }.length < 8 -> localError = "Introduza um Contacto Telefónico válido (mínimo 8 dígitos)."
                    selectedCourseId.isBlank() -> localError = "Seleccione um Curso."
                    paymentMethod.requiresDigitalProof && transactionId.isBlank() ->
                        localError = "Informe o ID da Transacção para ${paymentMethod.labelPt}."
                    paymentMethod.requiresDigitalProof && smsText.isBlank() ->
                        localError = "Cole o texto da mensagem SMS de confirmação."
                    paymentMethod.requiresDigitalProof && screenshotUri == null ->
                        localError = "Anexe a captura de ecrã (screenshot) do comprovativo."
                    else -> {
                        onSubmitEnrollment(
                            fullName, birthDate, nationality, province,
                            selectedCourseId, modality, phone, paymentMethod,
                            transactionId, smsText, screenshotUri
                        ) { stu ->
                            createdProtocol = stu
                            fullName = ""
                            birthDate = ""
                            phone = ""
                            transactionId = ""
                            smsText = ""
                            screenshotUri = null
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AjafEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("enroll_submit_button")
        ) {
            Icon(Icons.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Submeter Inscrição Online", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        birthDate = AjafFormatters.formatDate(millis)
                    }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (createdProtocol != null) {
        val stu = createdProtocol!!
        AlertDialog(
            onDismissRequest = { createdProtocol = null },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AjafEmerald, modifier = Modifier.size(40.dp)) },
            title = { Text("Inscrição Registada!", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("A sua inscrição foi submetida com sucesso e encontra-se com estado \"Pendente de Aprovação\" na Secretaria.")
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text("Nº de Estudante / Protocolo:", style = MaterialTheme.typography.labelSmall)
                            Text(stu.studentNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Contacto registado: ${stu.phone}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(
                        "Para acompanhar a aprovação e pagar propinas, aceda ao Portal do Aluno usando o seu Nome Completo ou Nº de Estudante + Contacto Telefónico (sem e-mail).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        createdProtocol = null
                        onGoToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AjafNavyPrimary)
                ) {
                    Text("Ir para o Portal")
                }
            },
            dismissButton = {
                TextButton(onClick = { createdProtocol = null }) { Text("Fechar") }
            }
        )
    }
}
