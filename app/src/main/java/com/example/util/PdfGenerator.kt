package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.R
import com.example.data.local.*
import com.example.data.model.AjafFormatters
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.ReportPeriod
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val MARGIN = 36f

    private val NAVY = Color.parseColor("#0B2945")
    private val EMERALD = Color.parseColor("#059669")
    private val AMBER = Color.parseColor("#D97706")
    private val SLATE = Color.parseColor("#475569")
    private val DARK = Color.parseColor("#0F172A")
    private val BORDER = Color.parseColor("#CBD5E1")
    private val LIGHT_BG = Color.parseColor("#F1F5F9")

    private fun getPdfOutputDir(context: Context): File {
        return File(context.filesDir, "pdfs").apply { mkdirs() }
    }

    private fun drawAjafHeader(
        context: Context,
        canvas: Canvas,
        pageWidth: Int,
        docTitle: String,
        subtitle: String
    ): Float {
        val headerPaint = Paint().apply { color = NAVY }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 98f, headerPaint)

        // Accent emerald strip
        val stripPaint = Paint().apply { color = EMERALD }
        canvas.drawRect(0f, 94f, pageWidth.toFloat(), 98f, stripPaint)

        // Emblem
        val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_ajaf_emblem)
        if (logoBitmap != null) {
            val dest = RectF(MARGIN, 18f, MARGIN + 58f, 76f)
            canvas.drawBitmap(logoBitmap, null, dest, null)
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("AJAF — Associação para Jovens e Adolescentes para o Futuro", MARGIN + 70f, 36f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        canvas.drawText("Centro de Formação Profissional & Biblioteca Técnica Digital • Moçambique", MARGIN + 70f, 52f, subPaint)

        val docTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6EE7B7")
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(docTitle, MARGIN + 70f, 72f, docTitlePaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(subtitle, pageWidth - MARGIN, 86f, datePaint)

        return 122f
    }

    private fun drawFooter(canvas: Canvas, pageWidth: Int, pageHeight: Int, footerText: String) {
        val linePaint = Paint().apply {
            color = BORDER
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, pageHeight - 44f, pageWidth - MARGIN, pageHeight - 44f, linePaint)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SLATE
            textSize = 8.5f
        }
        canvas.drawText(footerText, MARGIN, pageHeight - 26f, textPaint)
        val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SLATE
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Documento gerado pelo Sistema Oficial AJAF", pageWidth - MARGIN, pageHeight - 26f, rightPaint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float
    ): Float {
        var y = startY
        text.split("\n").forEach { paragraph ->
            val words = paragraph.split(" ")
            var line = ""
            for (word in words) {
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    line = candidate
                } else {
                    canvas.drawText(line, x, y, paint)
                    y += lineHeight
                    line = word
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, x, y, paint)
                y += lineHeight
            }
        }
        return y
    }

    /**
     * 1. Recibo Oficial de Pagamento (Inscrição, Mensalidade + Multa 16%, Exame, Certificado)
     */
    fun generatePaymentReceiptPdf(
        context: Context,
        payment: PaymentRecordEntity,
        student: StudentEnrollmentEntity?
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        var y = drawAjafHeader(
            context, canvas, A4_WIDTH,
            "RECIBO OFICIAL DE PAGAMENTO",
            "Emitido em: ${AjafFormatters.formatDateTime(System.currentTimeMillis())}"
        )

        // Box Código de Autenticação
        val authBox = Paint().apply { color = Color.parseColor("#ECFDF5") }
        val borderPaint = Paint().apply {
            color = EMERALD
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(MARGIN, y, A4_WIDTH - MARGIN, y + 44f, 6f, 6f, authBox)
        canvas.drawRoundRect(MARGIN, y, A4_WIDTH - MARGIN, y + 44f, 6f, 6f, borderPaint)

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SLATE
            textSize = 10f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 10.5f
        }

        val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = EMERALD
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText("Código de Autenticação: ${payment.authCode ?: "AJAF-VERIFICADO"}", MARGIN + 14f, y + 26f, codePaint)
        val statusRight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = EMERALD
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("ESTADO: ${payment.status.labelPt.uppercase()}", A4_WIDTH - MARGIN - 14f, y + 26f, statusRight)

        y += 66f
        canvas.drawText("DADOS DO FORMANDO", MARGIN, y, boldPaint)
        y += 10f

        val cardBg = Paint().apply { color = LIGHT_BG }
        canvas.drawRoundRect(MARGIN, y, A4_WIDTH - MARGIN, y + 92f, 6f, 6f, cardBg)
        y += 22f
        canvas.drawText("Nome Completo: ${payment.studentName}", MARGIN + 14f, y, valuePaint)
        canvas.drawText("Nº de Estudante: ${payment.studentNumber}", A4_WIDTH / 2f + 10f, y, valuePaint)
        y += 20f
        canvas.drawText("Curso: ${payment.courseName}", MARGIN + 14f, y, valuePaint)
        y += 20f
        canvas.drawText("Contacto: ${student?.phone ?: "—"}   •   Província: ${student?.province ?: "Moçambique"}   •   Modalidade: ${student?.modality ?: "—"}", MARGIN + 14f, y, labelPaint)

        y += 44f
        canvas.drawText("DETALHES DO PAGAMENTO", MARGIN, y, boldPaint)
        y += 12f

        // Table rows
        fun drawRow(label: String, value: String, highlight: Boolean = false) {
            val rowBg = Paint().apply { color = if (highlight) Color.parseColor("#EFF6FF") else Color.WHITE }
            val stroke = Paint().apply {
                color = BORDER
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
            canvas.drawRect(MARGIN, y, A4_WIDTH - MARGIN, y + 28f, rowBg)
            canvas.drawRect(MARGIN, y, A4_WIDTH - MARGIN, y + 28f, stroke)
            canvas.drawText(label, MARGIN + 12f, y + 18f, if (highlight) boldPaint else labelPaint)
            val rightVal = Paint(if (highlight) boldPaint else valuePaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText(value, A4_WIDTH - MARGIN - 12f, y + 18f, rightVal)
            y += 28f
        }

        drawRow("Tipo de Taxa / Descrição", "${payment.feeType.labelPt} — ${payment.referenceLabel}")
        drawRow("Valor Base", AjafFormatters.formatMzn(payment.baseAmountMzn))
        drawRow(
            "Multa por Atraso (16%)",
            if (payment.lateFeeApplied) AjafFormatters.formatMzn(payment.lateFeeAmountMzn) else "Não Aplicável (0,00 MT)"
        )
        drawRow("Método de Pagamento", payment.paymentMethod?.labelPt ?: "Numerário")
        drawRow("ID da Transacção / Ref.", payment.transactionId ?: "Liquidação Presencial")
        drawRow("Data de Vencimento", AjafFormatters.formatDate(payment.dueDateMillis))
        drawRow("Data de Aprovação", payment.reviewedAt?.let { AjafFormatters.formatDateTime(it) } ?: AjafFormatters.formatDateTime(System.currentTimeMillis()))
        drawRow("Validado por", payment.reviewedBy ?: "Secretaria AJAF")
        drawRow("TOTAL PAGO (MZN)", AjafFormatters.formatMzn(payment.totalAmountMzn), highlight = true)

        y += 38f
        val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SLATE
            textSize = 9.5f
        }
        drawWrappedText(
            canvas,
            "Nota Institucional: Este recibo comprova a liquidação da taxa acima discriminada junto da AJAF - Associação para Jovens e Adolescentes para o Futuro. Guarde o código de autenticação para efeitos de auditoria e emissão de certificado.",
            MARGIN, y, A4_WIDTH - 2 * MARGIN, notePaint, 14f
        )

        drawFooter(canvas, A4_WIDTH, A4_HEIGHT, "AJAF • Recibo ${payment.authCode ?: payment.id}")
        doc.finishPage(page)

        val file = File(getPdfOutputDir(context), "Recibo_${payment.studentNumber}_${payment.id}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /**
     * 2. Pauta Académica (do Aluno ou do Curso Completo)
     */
    fun generatePautaPdf(
        context: Context,
        courseName: String,
        trainerName: String,
        grades: List<GradeRecordEntity>
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        var y = drawAjafHeader(
            context, canvas, A4_WIDTH,
            "PAUTA DE AVALIAÇÃO ACADÉMICA",
            "Emitido: ${AjafFormatters.formatDateTime(System.currentTimeMillis())}"
        )

        val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 9.5f
        }

        canvas.drawText("Curso: $courseName", MARGIN, y, bold)
        y += 16f
        canvas.drawText("Formador Responsável: $trainerName   •   Total de Formandos: ${grades.size}", MARGIN, y, text)
        y += 20f

        // Table Header
        val hdrBg = Paint().apply { color = NAVY }
        canvas.drawRect(MARGIN, y, A4_WIDTH - MARGIN, y + 26f, hdrBg)
        val hdrTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("Nº Estudante", MARGIN + 6f, y + 17f, hdrTxt)
        canvas.drawText("Nome Completo", MARGIN + 92f, y + 17f, hdrTxt)
        canvas.drawText("Av.1", MARGIN + 275f, y + 17f, hdrTxt)
        canvas.drawText("Av.2", MARGIN + 318f, y + 17f, hdrTxt)
        canvas.drawText("Exame", MARGIN + 362f, y + 17f, hdrTxt)
        canvas.drawText("Média", MARGIN + 412f, y + 17f, hdrTxt)
        canvas.drawText("Estado", MARGIN + 462f, y + 17f, hdrTxt)
        y += 26f

        val border = Paint().apply {
            color = BORDER
            style = Paint.Style.STROKE
            strokeWidth = 0.7f
        }
        grades.take(22).forEachIndexed { idx, g ->
            val bg = Paint().apply { color = if (idx % 2 == 0) Color.WHITE else LIGHT_BG }
            canvas.drawRect(MARGIN, y, A4_WIDTH - MARGIN, y + 24f, bg)
            canvas.drawRect(MARGIN, y, A4_WIDTH - MARGIN, y + 24f, border)
            canvas.drawText(g.studentNumber, MARGIN + 6f, y + 16f, text)
            canvas.drawText(g.studentName.take(28), MARGIN + 92f, y + 16f, text)
            canvas.drawText(AjafFormatters.formatGrade(g.eval1), MARGIN + 275f, y + 16f, text)
            canvas.drawText(AjafFormatters.formatGrade(g.eval2), MARGIN + 318f, y + 16f, text)
            canvas.drawText(AjafFormatters.formatGrade(g.examGrade), MARGIN + 362f, y + 16f, text)
            canvas.drawText(AjafFormatters.formatGrade(g.finalAverage), MARGIN + 412f, y + 16f, bold.apply { textSize = 9.5f })
            val stPaint = Paint(text).apply {
                color = when (g.academicStatus.name) {
                    "APROVADO" -> EMERALD
                    "REPROVADO" -> Color.RED
                    else -> AMBER
                }
            }
            canvas.drawText(g.academicStatus.labelPt, MARGIN + 462f, y + 16f, stPaint)
            y += 24f
        }

        drawFooter(canvas, A4_WIDTH, A4_HEIGHT, "AJAF • Pauta Oficial ($courseName)")
        doc.finishPage(page)

        val file = File(getPdfOutputDir(context), "Pauta_${courseName.replace(" ", "_").take(20)}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /**
     * 3. Manual Técnico da Biblioteca Digital Pública (PDF)
     */
    fun generateLibraryManualPdf(context: Context, docItem: LibraryDocumentEntity): File {
        if (!docItem.localPdfPath.isNullOrBlank()) {
            val uploaded = File(docItem.localPdfPath)
            if (uploaded.exists()) return uploaded
        }

        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        var y = drawAjafHeader(
            context, canvas, A4_WIDTH,
            "BIBLIOTECA TÉCNICA DIGITAL AJAF",
            "Publicado: ${docItem.publishedDate}"
        )

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NAVY
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        y = drawWrappedText(canvas, docItem.title, MARGIN, y + 6f, A4_WIDTH - 2 * MARGIN, titlePaint, 24f)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = EMERALD
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("Área: ${docItem.category}  •  Curso: ${docItem.courseName}  •  Autor: ${docItem.authorOrSource}", MARGIN, y + 8f, metaPaint)
        y += 30f

        val linePaint = Paint().apply { color = BORDER; strokeWidth = 1f }
        canvas.drawLine(MARGIN, y, A4_WIDTH - MARGIN, y, linePaint)
        y += 24f

        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 10.5f
        }

        canvas.drawText("Descrição Geral", MARGIN, y, sectionPaint)
        y += 18f
        y = drawWrappedText(canvas, docItem.description, MARGIN, y, A4_WIDTH - 2 * MARGIN, bodyPaint, 16f)

        y += 20f
        canvas.drawText("Conteúdo Técnico Estruturado", MARGIN, y, sectionPaint)
        y += 20f
        y = drawWrappedText(canvas, docItem.contentSummary, MARGIN, y, A4_WIDTH - 2 * MARGIN, bodyPaint, 17f)

        drawFooter(canvas, A4_WIDTH, A4_HEIGHT, "AJAF Biblioteca Técnica Digital • Uso Educacional Gratuito")
        doc.finishPage(page)

        val file = File(getPdfOutputDir(context), "Manual_${docItem.id}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /**
     * 4. Relatórios Financeiros e Consolidados (Semanal, Mensal, Anual, Consolidado Geral)
     */
    fun generatePeriodicReportPdf(
        context: Context,
        title: String,
        periodLabel: String,
        payments: List<PaymentRecordEntity>,
        studentsCount: Int,
        coursesCount: Int,
        logs: List<AuditLogEntity> = emptyList()
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        var y = drawAjafHeader(
            context, canvas, A4_WIDTH,
            title.uppercase(),
            "Período: $periodLabel"
        )

        val approved = payments.filter { it.status == PaymentStatus.APROVADO }
        val pending = payments.filter { it.status == PaymentStatus.PENDENTE }
        val totalMzn = approved.sumOf { it.totalAmountMzn }
        val mpesaTotal = approved.filter { it.paymentMethod == PaymentMethod.M_PESA }.sumOf { it.totalAmountMzn }
        val emolaTotal = approved.filter { it.paymentMethod == PaymentMethod.E_MOLA }.sumOf { it.totalAmountMzn }
        val cashTotal = approved.filter { it.paymentMethod == PaymentMethod.NUMERARIO }.sumOf { it.totalAmountMzn }

        val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 10f
        }

        canvas.drawText("Resumo Executivo", MARGIN, y, bold)
        y += 16f
        val kpiBg = Paint().apply { color = LIGHT_BG }
        canvas.drawRoundRect(MARGIN, y, A4_WIDTH - MARGIN, y + 84f, 6f, 6f, kpiBg)
        canvas.drawText("Total Arrecadado (Aprovado): ${AjafFormatters.formatMzn(totalMzn)}", MARGIN + 14f, y + 22f, bold)
        canvas.drawText("Pagamentos Pendentes: ${pending.size}   •   Estudantes: $studentsCount   •   Cursos: $coursesCount", MARGIN + 14f, y + 42f, body)
        canvas.drawText("M-Pesa: ${AjafFormatters.formatMzn(mpesaTotal)}  |  E-Mola: ${AjafFormatters.formatMzn(emolaTotal)}  |  Numerário: ${AjafFormatters.formatMzn(cashTotal)}", MARGIN + 14f, y + 64f, body)
        y += 106f

        canvas.drawText("Discriminação por Tipo de Taxa", MARGIN, y, bold)
        y += 14f
        com.example.data.model.FeeType.entries.forEach { ft ->
            val sumFt = approved.filter { it.feeType == ft }.sumOf { it.totalAmountMzn }
            val countFt = approved.count { it.feeType == ft }
            canvas.drawText("• ${ft.labelPt}: $countFt recebimentos — ${AjafFormatters.formatMzn(sumFt)}", MARGIN + 10f, y, body)
            y += 18f
        }

        y += 14f
        canvas.drawText("Últimos Movimentos Aprovados", MARGIN, y, bold)
        y += 14f
        approved.take(10).forEach { p ->
            canvas.drawText(
                "${p.studentNumber} | ${p.studentName.take(20)} | ${p.referenceLabel.take(22)} | ${AjafFormatters.formatMzn(p.totalAmountMzn)}",
                MARGIN + 6f,
                y,
                body
            )
            y += 16f
        }

        if (logs.isNotEmpty() && y < A4_HEIGHT - 130f) {
            y += 16f
            canvas.drawText("Actividades Recentes de Auditoria", MARGIN, y, bold)
            y += 14f
            logs.take(5).forEach { l ->
                canvas.drawText("• [${l.actorRole}] ${l.action}: ${l.details.take(65)}", MARGIN + 6f, y, body)
                y += 15f
            }
        }

        drawFooter(canvas, A4_WIDTH, A4_HEIGHT, "AJAF • Relatório Institucional ($periodLabel)")
        doc.finishPage(page)

        val file = File(getPdfOutputDir(context), "Relatorio_${periodLabel.replace(" ", "_").take(20)}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /**
     * 5. Certificado Oficial da AJAF (Formato Paisagem A4 Formal com Assinatura e Carimbo)
     */
    fun generateOfficialCertificatePdf(context: Context, cert: CertificateRecordEntity): File {
        val width = 842
        val height = 595
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        // Fundo marfim suave
        canvas.drawColor(Color.parseColor("#FFFDF7"))

        // Moldura dupla institucional
        val outerBorder = Paint().apply {
            color = NAVY
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val innerBorder = Paint().apply {
            color = AMBER
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(24f, 24f, width - 24f, height - 24f, outerBorder)
        canvas.drawRect(34f, 34f, width - 34f, height - 34f, innerBorder)

        // Logotipo AJAF centrado no topo
        val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_ajaf_emblem)
        if (logoBitmap != null) {
            val dest = RectF(width / 2f - 36f, 48f, width / 2f + 36f, 120f)
            canvas.drawBitmap(logoBitmap, null, dest, null)
        }

        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NAVY
            textSize = 14f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("AJAF — ASSOCIAÇÃO PARA JOVENS E ADOLESCENTES PARA O FUTURO", width / 2f, 142f, orgPaint)

        val subOrgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SLATE
            textSize = 10.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Centro de Formação Profissional de Curta Duração • República de Moçambique", width / 2f, 158f, subOrgPaint)

        val certHeadline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = EMERALD
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("CERTIFICADO DE FORMAÇÃO PROFISSIONAL", width / 2f, 206f, certHeadline)

        val bodyCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DARK
            textSize = 13f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Certifica-se para os devidos efeitos que o(a) formando(a):", width / 2f, 244f, bodyCenter)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NAVY
            textSize = 25f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(cert.studentName.uppercase(), width / 2f, 286f, namePaint)

        val line = Paint().apply { color = AMBER; strokeWidth = 1.5f }
        canvas.drawLine(width / 2f - 190f, 294f, width / 2f + 190f, 294f, line)

        val detailsText = "Portador(a) do Nº de Estudante ${cert.studentNumber}, concluiu com aproveitamento o curso de\n" +
            "\"${cert.courseName}\", com carga horária total de ${cert.durationHours} horas,\n" +
            "obtendo a classificação final de ${AjafFormatters.formatGrade(cert.finalGrade)} valores."
        var y = 326f
        detailsText.split("\n").forEach {
            canvas.drawText(it, width / 2f, y, bodyCenter)
            y += 22f
        }

        val issueDate = AjafFormatters.formatDate(cert.issuedAt ?: System.currentTimeMillis())
        canvas.drawText("Emitido em Moçambique, aos $issueDate  •  Código Oficial: ${cert.certificateCode}", width / 2f, 412f, subOrgPaint)

        // Carimbo Institucional Circular à esquerda/centro
        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NAVY
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val stampFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6EFF8")
            style = Paint.Style.FILL
        }
        val cx = 210f
        val cy = 492f
        canvas.drawCircle(cx, cy, 44f, stampFill)
        canvas.drawCircle(cx, cy, 44f, stampPaint)
        canvas.drawCircle(cx, cy, 38f, Paint(stampPaint).apply { strokeWidth = 1f })

        val stampText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NAVY
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("AJAF MOÇAMBIQUE", cx, cy - 10f, stampText)
        canvas.drawText("CARIMBO OFICIAL", cx, cy + 4f, stampText)
        canvas.drawText("VALIDADO", cx, cy + 18f, Paint(stampText).apply { color = EMERALD })

        // Assinatura Digital do Director à direita
        val sigX = width - 230f
        val sigLine = Paint().apply { color = DARK; strokeWidth = 1.2f }
        canvas.drawLine(sigX - 110f, 498f, sigX + 110f, 498f, sigLine)
        val sigScript = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NAVY
            textSize = 15f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }
        canvas.drawText(cert.directorName ?: "Direcção Geral AJAF", sigX, 490f, sigScript)
        canvas.drawText("O Director Pedagógico e Institucional", sigX, 515f, subOrgPaint)
        val approvedTime = cert.directorApprovedAt?.let { AjafFormatters.formatDate(it) } ?: issueDate
        canvas.drawText("Assinado digitalmente em $approvedTime", sigX, 530f, Paint(subOrgPaint).apply { textSize = 8.5f })

        doc.finishPage(page)

        val file = File(getPdfOutputDir(context), "Certificado_${cert.certificateCode}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
