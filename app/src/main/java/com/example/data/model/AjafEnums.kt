package com.example.data.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class UserRole(val labelPt: String) {
    ALUNO("Aluno"),
    FORMADOR("Formador"),
    SECRETARIA("Secretaria"),
    DIRECTOR("Director"),
    SUPERADMIN("Superadministrador")
}

enum class EnrollmentStatus(val labelPt: String) {
    PENDENTE("Pendente de Aprovação"),
    ACTIVO("Activo"),
    REJEITADO("Rejeitado"),
    CONCLUIDO("Concluído")
}

enum class FeeType(val labelPt: String) {
    INSCRICAO("Taxa de Inscrição"),
    MENSALIDADE("Mensalidade"),
    EXAME("Taxa de Exame"),
    CERTIFICADO("Taxa de Certificado")
}

enum class PaymentMethod(val labelPt: String, val requiresDigitalProof: Boolean) {
    E_MOLA("Transferência (E-Mola)", true),
    M_PESA("Transferência (M-Pesa)", true),
    NUMERARIO("Numerário", false)
}

enum class PaymentStatus(val labelPt: String) {
    POR_PAGAR("Por Pagar"),
    PENDENTE("Pendente"),
    APROVADO("Aprovado"),
    REJEITADO("Rejeitado")
}

enum class AcademicStatus(val labelPt: String) {
    EM_CURSO("Em curso"),
    APROVADO("Aprovado"),
    REPROVADO("Reprovado")
}

enum class ReportPeriod(val labelPt: String, val days: Long) {
    SEMANAL("Semanal (Últimos 7 dias)", 7L),
    MENSAL("Mensal (Últimos 30 dias)", 30L),
    ANUAL("Anual (Últimos 365 dias)", 365L)
}

object MozambiqueData {
    val provinces = listOf(
        "Maputo Cidade",
        "Maputo Província",
        "Gaza",
        "Inhambane",
        "Sofala",
        "Manica",
        "Tete",
        "Zambézia",
        "Nampula",
        "Cabo Delgado",
        "Niassa"
    )
}

object AjafFormatters {
    private val ptLocale = Locale("pt", "MZ")

    fun formatMzn(amount: Double): String {
        val nf = NumberFormat.getNumberInstance(ptLocale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "${nf.format(amount)} MT"
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", ptLocale)
        return sdf.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", ptLocale)
        return sdf.format(Date(millis))
    }

    fun formatGrade(value: Double?): String {
        return if (value == null) "--" else String.format(ptLocale, "%.1f", value)
    }
}

sealed interface ActiveSession {
    data object Guest : ActiveSession
    data class StudentSession(
        val studentId: String,
        val studentNumber: String,
        val fullName: String,
        val phone: String
    ) : ActiveSession

    data class StaffSession(
        val staffId: String,
        val fullName: String,
        val email: String,
        val role: UserRole
    ) : ActiveSession
}
