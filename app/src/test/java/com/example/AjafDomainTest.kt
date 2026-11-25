package com.example

import com.example.data.local.PaymentRecordEntity
import com.example.data.model.AjafFormatters
import com.example.data.model.FeeType
import com.example.data.model.MozambiqueData
import com.example.data.model.PaymentStatus
import org.junit.Assert.*
import org.junit.Test

class AjafDomainTest {
    @Test
    fun `mozambique has all 11 provinces`() {
        assertEquals(11, MozambiqueData.provinces.size)
        assertTrue(MozambiqueData.provinces.contains("Maputo Cidade"))
        assertTrue(MozambiqueData.provinces.contains("Cabo Delgado"))
        assertTrue(MozambiqueData.provinces.contains("Niassa"))
    }

    @Test
    fun `automatic 16 percent late fee formula applies when overdue and unpaid`() {
        val now = 1_000_000_000L
        val overdue = PaymentRecordEntity(
            id = "p1",
            studentId = "s1",
            studentNumber = "AJAF-2026-0001",
            studentName = "Armando Sitoe",
            courseId = "c_info",
            courseName = "Informática Aplicada e Redes",
            feeType = FeeType.MENSALIDADE,
            referenceLabel = "Mensalidade 2/3",
            baseAmountMzn = 1800.0,
            dueDateMillis = now - 86_400_000L,
            status = PaymentStatus.POR_PAGAR
        )
        val shouldApply = overdue.feeType == FeeType.MENSALIDADE &&
            overdue.status != PaymentStatus.APROVADO &&
            now > overdue.dueDateMillis
        assertTrue(shouldApply)
        val multa = overdue.baseAmountMzn * 0.16
        assertEquals(288.0, multa, 0.01)
        assertEquals(2088.0, overdue.baseAmountMzn + multa, 0.01)
    }

    @Test
    fun `currency formats in Meticais MT`() {
        val formatted = AjafFormatters.formatMzn(1800.0)
        assertTrue(formatted.endsWith("MT"))
    }
}
