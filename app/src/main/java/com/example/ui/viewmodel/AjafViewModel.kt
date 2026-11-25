package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.AjafRepository
import com.example.util.NetworkMonitor
import com.example.util.PdfGenerator
import com.example.util.PdfViewerHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class AjafUiState(
    val session: ActiveSession = ActiveSession.Guest,
    val superadminCount: Int = 0,
    val staffUsers: List<StaffUserEntity> = emptyList(),
    val courses: List<CourseEntity> = emptyList(),
    val vacancies: List<VacancyAnnouncementEntity> = emptyList(),
    val notices: List<StudentNoticeEntity> = emptyList(),
    val libraryDocs: List<LibraryDocumentEntity> = emptyList(),
    val students: List<StudentEnrollmentEntity> = emptyList(),
    val payments: List<PaymentRecordEntity> = emptyList(),
    val grades: List<GradeRecordEntity> = emptyList(),
    val certificates: List<CertificateRecordEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val isOnline: Boolean = true,
    val preselectedCourseId: String? = null,
    val activePdfPreview: Pair<String, File>? = null,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
)

class AjafViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AjafDatabase.getInstance(application)
    private val repo = AjafRepository(application, db.ajafDao())

    private val _session = MutableStateFlow<ActiveSession>(ActiveSession.Guest)
    private val _preselectedCourseId = MutableStateFlow<String?>(null)
    private val _activePdfPreview = MutableStateFlow<Pair<String, File>?>(null)
    private val _feedback = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    private val dataGroupA = combine(
        repo.superadminCount,
        repo.staffUsers,
        repo.courses,
        repo.vacancies,
        repo.notices
    ) { saCount, staff, courses, vacancies, notices ->
        DataGroupA(saCount, staff, courses, vacancies, notices)
    }

    private val dataGroupB = combine(
        repo.libraryDocuments,
        repo.students,
        repo.payments,
        repo.grades,
        repo.certificates
    ) { docs, students, payments, grades, certs ->
        DataGroupB(docs, students, payments, grades, certs)
    }

    private val uiFlags = combine(
        _session,
        _preselectedCourseId,
        _activePdfPreview,
        _feedback,
        _error
    ) { session, preCourse, pdf, msg, err ->
        UiFlags(session, preCourse, pdf, msg, err)
    }

    val uiState: StateFlow<AjafUiState> = combine(
        dataGroupA,
        dataGroupB,
        repo.auditLogs,
        NetworkMonitor.observeIsOnline(application),
        uiFlags
    ) { a, b, logs, online, flags ->
        AjafUiState(
            session = flags.session,
            superadminCount = a.superadminCount,
            staffUsers = a.staffUsers,
            courses = a.courses,
            vacancies = a.vacancies,
            notices = a.notices,
            libraryDocs = b.libraryDocs,
            students = b.students,
            payments = b.payments,
            grades = b.grades,
            certificates = b.certificates,
            auditLogs = logs,
            isOnline = online,
            preselectedCourseId = flags.preselectedCourseId,
            activePdfPreview = flags.activePdfPreview,
            feedbackMessage = flags.feedbackMessage,
            errorMessage = flags.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AjafUiState()
    )

    init {
        viewModelScope.launch {
            repo.ensureSeedData()
        }
    }

    fun clearMessages() {
        _feedback.value = null
        _error.value = null
    }

    fun setPreselectedCourse(courseId: String?) {
        _preselectedCourseId.value = courseId
    }

    fun closePdfPreview() {
        _activePdfPreview.value = null
    }

    fun logout() {
        _session.value = ActiveSession.Guest
        _feedback.value = "Sessão terminada com segurança."
    }

    // --- Autenticação ---

    fun registerFirstSuperadmin(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            repo.registerFirstSuperadmin(fullName, email, password)
                .onSuccess { user ->
                    _session.value = ActiveSession.StaffSession(user.id, user.fullName, user.email, user.role)
                    _feedback.value = "Conta de Superadministrador configurada! O auto-registo privilegiado foi bloqueado."
                }
                .onFailure { e -> _error.value = e.message }
        }
    }

    fun loginStaff(email: String, password: String, expectedRole: UserRole) {
        viewModelScope.launch {
            repo.loginStaff(email, password, expectedRole)
                .onSuccess { user ->
                    _session.value = ActiveSession.StaffSession(user.id, user.fullName, user.email, user.role)
                    _feedback.value = "Bem-vindo(a), ${user.fullName} (${user.role.labelPt})."
                }
                .onFailure { e -> _error.value = e.message }
        }
    }

    fun loginStudent(identifier: String, phone: String) {
        viewModelScope.launch {
            repo.loginStudent(identifier, phone)
                .onSuccess { stu ->
                    _session.value = ActiveSession.StudentSession(stu.id, stu.studentNumber, stu.fullName, stu.phone)
                    _feedback.value = "Bem-vindo(a), ${stu.fullName} (${stu.studentNumber})."
                }
                .onFailure { e -> _error.value = e.message }
        }
    }

    // --- Inscrição Pública ---

    fun submitEnrollment(
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
    ) {
        viewModelScope.launch {
            repo.submitOnlineEnrollment(
                fullName = fullName,
                birthDate = birthDate,
                nationality = nationality,
                province = province,
                courseId = courseId,
                modality = modality,
                phone = phone,
                paymentMethod = paymentMethod,
                transactionId = transactionId,
                smsText = smsText,
                screenshotSourceUri = screenshotUri
            ).onSuccess { student ->
                onSuccessProtocol(student)
                _feedback.value = "Inscrição submetida! Protocolo ${student.studentNumber} em estado Pendente de Aprovação."
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    // --- Acções do Aluno ---

    fun submitStudentPayment(
        paymentId: String,
        method: PaymentMethod,
        transactionId: String?,
        smsText: String?,
        screenshotUri: Uri?
    ) {
        val stu = _session.value as? ActiveSession.StudentSession ?: return
        viewModelScope.launch {
            repo.submitStudentPaymentProof(
                paymentId = paymentId,
                studentName = stu.fullName,
                method = method,
                transactionId = transactionId,
                smsText = smsText,
                screenshotSourceUri = screenshotUri
            ).onSuccess {
                _feedback.value = "Comprovativo submetido para validação na Secretaria!"
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    // --- Acções do Formador ---

    fun saveGrade(grade: GradeRecordEntity, eval1: Double?, eval2: Double?, examGrade: Double?) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.saveStudentGrades(staff.fullName, staff.role, grade, eval1, eval2, examGrade)
                .onSuccess {
                    _feedback.value = "Pauta gravada para ${grade.studentName}."
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    // --- Acções da Secretaria & Superadministrador ---

    fun createInternalStaff(fullName: String, email: String, password: String, targetRole: UserRole) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.createInternalStaff(staff.fullName, staff.role, fullName, email, password, targetRole)
                .onSuccess {
                    _feedback.value = "Conta de ${targetRole.labelPt} criada para $fullName."
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    fun assignTrainerToCourse(courseId: String, trainerId: String?) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.assignTrainerToCourse(staff.fullName, staff.role, courseId, trainerId)
            _feedback.value = "Formador associado ao curso com sucesso."
        }
    }

    fun reviewPayment(paymentId: String, approved: Boolean) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.reviewPayment(staff.fullName, staff.role, paymentId, approved)
                .onSuccess {
                    _feedback.value = if (approved) "Pagamento aprovado e recibo disponibilizado." else "Pagamento rejeitado."
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    // --- Acções do Director ---

    fun approveCertificateByDirector(cert: CertificateRecordEntity) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.approveCertificateByDirector(staff.fullName, cert)
            _feedback.value = "Assinatura e carimbo aprovados para ${cert.studentName}."
        }
    }

    // --- Acções do Superadministrador ---

    fun toggleStaffActive(target: StaffUserEntity) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.toggleStaffActive(staff.fullName, target)
            _feedback.value = "Estado de permissão actualizado."
        }
    }

    fun createCourse(
        name: String,
        category: String,
        description: String,
        durationHours: Int,
        durationMonths: Int,
        enrollmentFee: Double,
        monthlyFee: Double,
        examFee: Double,
        certificateFee: Double,
        modalities: String
    ) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.createOrUpdateCourse(
                staff.fullName, name, category, description,
                durationHours, durationMonths, enrollmentFee, monthlyFee, examFee, certificateFee, modalities
            )
            _feedback.value = "Curso '$name' publicado no catálogo."
        }
    }

    fun publishVacancy(
        courseId: String,
        title: String,
        summary: String,
        startDate: String,
        endDate: String,
        modalities: String,
        vacanciesCount: Int
    ) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.publishVacancy(staff.fullName, courseId, title, summary, startDate, endDate, modalities, vacanciesCount)
            _feedback.value = "Anúncio de vaga publicado na Home."
        }
    }

    fun deleteVacancy(id: String) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.deleteVacancy(staff.fullName, id)
            _feedback.value = "Anúncio removido."
        }
    }

    fun publishStudentNotice(title: String, message: String, category: String) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.publishStudentNotice(staff.fullName, title, message, category)
            _feedback.value = "Comunicado publicado no painel dos alunos."
        }
    }

    fun publishLibraryDocument(
        title: String,
        category: String,
        courseName: String,
        description: String,
        contentSummary: String,
        pdfUri: Uri?
    ) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.publishLibraryDocument(staff.fullName, title, category, courseName, description, contentSummary, pdfUri)
            _feedback.value = "Manual publicado na Biblioteca Técnica Digital."
        }
    }

    fun deleteLibraryDocument(doc: LibraryDocumentEntity) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.deleteLibraryDocument(staff.fullName, doc)
            _feedback.value = "Documento removido da biblioteca."
        }
    }

    fun toggleCourseGradesLock(course: CourseEntity) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.toggleCourseGradesLock(staff.fullName, course)
            _feedback.value = "Controlo de edição da pauta actualizado."
        }
    }

    fun togglePaymentLock(payment: PaymentRecordEntity) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.togglePaymentLock(staff.fullName, payment)
            _feedback.value = "Controlo de edição financeira actualizado."
        }
    }

    fun toggleAuditLock(log: AuditLogEntity) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        viewModelScope.launch {
            repo.toggleAuditLock(staff.fullName, log)
            _feedback.value = "Estado do registo de auditoria actualizado."
        }
    }

    // --- Exportação e Pré-visualização de PDF ---

    fun openReceiptPdf(payment: PaymentRecordEntity) {
        val ctx = getApplication<Application>()
        val stu = uiState.value.students.find { it.id == payment.studentId }
        val file = PdfGenerator.generatePaymentReceiptPdf(ctx, payment, stu)
        _activePdfPreview.value = "Recibo — ${payment.studentNumber}" to file
    }

    fun openLibraryManualPdf(doc: LibraryDocumentEntity) {
        val ctx = getApplication<Application>()
        val file = PdfGenerator.generateLibraryManualPdf(ctx, doc)
        _activePdfPreview.value = doc.title to file
    }

    fun openPautaPdf(courseName: String, trainerName: String, grades: List<GradeRecordEntity>) {
        val ctx = getApplication<Application>()
        val file = PdfGenerator.generatePautaPdf(ctx, courseName, trainerName, grades)
        _activePdfPreview.value = "Pauta — $courseName" to file
    }

    fun openPeriodicReportPdf(period: ReportPeriod) {
        val ctx = getApplication<Application>()
        val now = System.currentTimeMillis()
        val cutoff = now - (period.days * 86_400_000L)
        val state = uiState.value
        val filtered = state.payments.filter { (it.reviewedAt ?: it.submittedAt ?: it.dueDateMillis) >= cutoff }
        val file = PdfGenerator.generatePeriodicReportPdf(
            context = ctx,
            title = "Relatório de Actividades — ${period.labelPt}",
            periodLabel = period.labelPt,
            payments = filtered,
            studentsCount = state.students.size,
            coursesCount = state.courses.size
        )
        _activePdfPreview.value = "Relatório ${period.labelPt}" to file
    }

    fun openAcademicPerformancePdf(courseFilterName: String, grades: List<GradeRecordEntity>) {
        val ctx = getApplication<Application>()
        val file = PdfGenerator.generatePautaPdf(ctx, courseFilterName, "Direcção Pedagógica AJAF", grades)
        _activePdfPreview.value = "Desempenho — $courseFilterName" to file
    }

    fun openConsolidatedAdminReportPdf() {
        val ctx = getApplication<Application>()
        val state = uiState.value
        val file = PdfGenerator.generatePeriodicReportPdf(
            context = ctx,
            title = "Relatório Consolidado Geral AJAF",
            periodLabel = "Histórico Completo (${AjafFormatters.formatDate(System.currentTimeMillis())})",
            payments = state.payments,
            studentsCount = state.students.size,
            coursesCount = state.courses.size,
            logs = state.auditLogs
        )
        _activePdfPreview.value = "Relatório Consolidado Geral" to file
    }

    fun issueAndOpenOfficialCertificatePdf(cert: CertificateRecordEntity) {
        val staff = _session.value as? ActiveSession.StaffSession ?: return
        if (!cert.directorApproved) {
            _error.value = "Este certificado aguarda aprovação de Assinatura e Carimbo pelo Director."
            return
        }
        viewModelScope.launch {
            repo.markCertificateIssuedBySuperadmin(staff.fullName, cert)
            val updated = cert.copy(issuedBySuperadmin = true, issuedAt = System.currentTimeMillis())
            val file = PdfGenerator.generateOfficialCertificatePdf(getApplication(), updated)
            _activePdfPreview.value = "Certificado Oficial — ${cert.studentName}" to file
        }
    }

    fun savePreviewedPdfToDownloads(file: File) {
        viewModelScope.launch {
            PdfViewerHelper.savePdfToDownloads(getApplication(), file)
                .onSuccess { msg -> _feedback.value = msg }
                .onFailure { e -> _error.value = "Erro ao guardar PDF: ${e.message}" }
        }
    }
}

private data class DataGroupA(
    val superadminCount: Int,
    val staffUsers: List<StaffUserEntity>,
    val courses: List<CourseEntity>,
    val vacancies: List<VacancyAnnouncementEntity>,
    val notices: List<StudentNoticeEntity>
)

private data class DataGroupB(
    val libraryDocs: List<LibraryDocumentEntity>,
    val students: List<StudentEnrollmentEntity>,
    val payments: List<PaymentRecordEntity>,
    val grades: List<GradeRecordEntity>,
    val certificates: List<CertificateRecordEntity>
)

private data class UiFlags(
    val session: ActiveSession,
    val preselectedCourseId: String?,
    val activePdfPreview: Pair<String, File>?,
    val feedbackMessage: String?,
    val errorMessage: String?
)
