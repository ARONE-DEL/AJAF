package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.security.MessageDigest
import java.util.UUID

class AjafRepository(
    private val context: Context,
    private val dao: AjafDao
) {
    val staffUsers: Flow<List<StaffUserEntity>> = dao.observeStaffUsers()
    val superadminCount: Flow<Int> = dao.observeSuperadminCount()
    val courses: Flow<List<CourseEntity>> = dao.observeCourses()
    val vacancies: Flow<List<VacancyAnnouncementEntity>> = dao.observeVacancies()
    val notices: Flow<List<StudentNoticeEntity>> = dao.observeNotices()
    val libraryDocuments: Flow<List<LibraryDocumentEntity>> = dao.observeLibraryDocuments()
    val students: Flow<List<StudentEnrollmentEntity>> = dao.observeStudents()
    val payments: Flow<List<PaymentRecordEntity>> = dao.observePayments().map { list ->
        val now = System.currentTimeMillis()
        list.map { p -> computeEffectivePayment(p, now) }
    }
    val grades: Flow<List<GradeRecordEntity>> = dao.observeGrades()
    val certificates: Flow<List<CertificateRecordEntity>> = dao.observeCertificates()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.observeAuditLogs()

    /**
     * Regra Automática de Multa de 16%:
     * Se uma mensalidade ultrapassar a data de vencimento sem pagamento aprovado,
     * aplica automaticamente multa de 16% sobre o valor base.
     */
    fun computeEffectivePayment(payment: PaymentRecordEntity, nowMillis: Long = System.currentTimeMillis()): PaymentRecordEntity {
        if (payment.feeType == FeeType.MENSALIDADE &&
            payment.status != PaymentStatus.APROVADO &&
            nowMillis > payment.dueDateMillis
        ) {
            val multa = payment.baseAmountMzn * 0.16
            return payment.copy(
                lateFeeApplied = true,
                lateFeeAmountMzn = multa,
                totalAmountMzn = payment.baseAmountMzn + multa
            )
        }
        return payment
    }

    suspend fun ensureSeedData() {
        if (dao.getStudentCount() > 0) return

        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val initialCourses = listOf(
            CourseEntity(
                id = "c_info",
                name = "Informática Aplicada e Redes",
                category = "Tecnologia & TI",
                description = "Formação prática em sistemas operativos, pacote Office profissional, redes locais (LAN/Wi-Fi) e manutenção de computadores.",
                durationHours = 140,
                durationMonths = 3,
                enrollmentFeeMzn = 500.0,
                monthlyFeeMzn = 1800.0,
                examFeeMzn = 650.0,
                certificateFeeMzn = 850.0,
                modalities = "Online, Presencial"
            ),
            CourseEntity(
                id = "c_elec",
                name = "Electricidade Instaladora e Energia Solar",
                category = "Energia & Engenharia",
                description = "Dimensionamento e montagem de quadros eléctricos residenciais, sistemas fotovoltaicos autónomos e normas de segurança.",
                durationHours = 160,
                durationMonths = 3,
                enrollmentFeeMzn = 600.0,
                monthlyFeeMzn = 2200.0,
                examFeeMzn = 750.0,
                certificateFeeMzn = 900.0,
                modalities = "Presencial"
            ),
            CourseEntity(
                id = "c_cont",
                name = "Contabilidade e Gestão de PME",
                category = "Gestão & Finanças",
                description = "Escrituração contabilística, gestão de tesouraria, obrigações fiscais em Moçambique (INSS, IRPS, IVA) e plano de negócios.",
                durationHours = 120,
                durationMonths = 2,
                enrollmentFeeMzn = 450.0,
                monthlyFeeMzn = 1600.0,
                examFeeMzn = 600.0,
                certificateFeeMzn = 800.0,
                modalities = "Online, Presencial"
            ),
            CourseEntity(
                id = "c_cost",
                name = "Corte, Costura e Empreendedorismo",
                category = "Moda & Ofícios",
                description = "Modelagem industrial e artesanal, acabamento têxtil, cálculo de custos e gestão de atelier sustentável.",
                durationHours = 120,
                durationMonths = 3,
                enrollmentFeeMzn = 400.0,
                monthlyFeeMzn = 1500.0,
                examFeeMzn = 550.0,
                certificateFeeMzn = 750.0,
                modalities = "Presencial"
            )
        )
        dao.insertCourses(initialCourses)

        val initialVacancies = listOf(
            VacancyAnnouncementEntity(
                id = "v1",
                courseId = "c_info",
                title = "Turma de Informática Aplicada & Redes — 1º Ciclo",
                summary = "Aulas práticas em laboratório com certificado reconhecido. Vagas limitadas para turnos da manhã e pós-laboral.",
                startDate = "01/12/2026",
                endDate = "28/02/2027",
                modalities = "Online e Presencial",
                vacanciesCount = 25,
                createdAt = now - 2 * dayMs
            ),
            VacancyAnnouncementEntity(
                id = "v2",
                courseId = "c_elec",
                title = "Electricidade Instaladora & Painéis Solares",
                summary = "Capacitação intensiva com kits reais de montagem eléctrica e sistemas solares off-grid para habitações.",
                startDate = "05/12/2026",
                endDate = "05/03/2027",
                modalities = "Presencial",
                vacanciesCount = 18,
                createdAt = now - dayMs
            ),
            VacancyAnnouncementEntity(
                id = "v3",
                courseId = "c_cont",
                title = "Contabilidade Prática e Fiscalidade Moçambicana",
                summary = "Domine lançamentos, balancetes e declarações fiscais para pequenas e médias empresas.",
                startDate = "10/12/2026",
                endDate = "10/02/2027",
                modalities = "Online e Presencial",
                vacanciesCount = 30,
                createdAt = now
            )
        )
        dao.insertVacancies(initialVacancies)

        val initialDocs = listOf(
            LibraryDocumentEntity(
                id = "doc1",
                title = "Manual de Redes Locais e Manutenção de PCs",
                category = "Tecnologia & TI",
                courseName = "Informática Aplicada e Redes",
                description = "Guia técnico completo sobre arquitectura de computadores, montagem, diagnóstico de hardware e configuração de redes TCP/IP e Wi-Fi.",
                authorOrSource = "Coordenação Técnica AJAF",
                pageCount = 48,
                publishedDate = "10/10/2026",
                contentSummary = "Módulo 1: Componentes internos e fonte de alimentação.\nMódulo 2: Instalação de sistemas operativos e particionamento.\nMódulo 3: Cabeamento estruturado RJ-45 (Normas T568A/B), sub-redes IPv4 e routers.\nMódulo 4: Segurança básica, backups e resolução de avarias comuns.",
                createdAt = now - 10 * dayMs
            ),
            LibraryDocumentEntity(
                id = "doc2",
                title = "Instalações Eléctricas de Baixa Tensão e Sistemas Solares",
                category = "Energia & Engenharia",
                courseName = "Electricidade Instaladora e Energia Solar",
                description = "Apostila prática cobrindo lei de Ohm, quadros de distribuição, disjuntores diferenciais, aterramento e dimensionamento fotovoltaico.",
                authorOrSource = "Departamento de Ofícios AJAF",
                pageCount = 64,
                publishedDate = "18/10/2026",
                contentSummary = "Capítulo 1: Segurança eléctrica e EPIs obrigatórios.\nCapítulo 2: Leitura de esquemas unifilares e multifilares.\nCapítulo 3: Cálculo de secção de condutores e protecções térmicas.\nCapítulo 4: Painéis solares, controladores MPPT, baterias estacionárias e inversores.",
                createdAt = now - 8 * dayMs
            ),
            LibraryDocumentEntity(
                id = "doc3",
                title = "Fundamentos de Contabilidade e Fiscalidade em Moçambique",
                category = "Gestão & Finanças",
                courseName = "Contabilidade e Gestão de PME",
                description = "Manual de apoio com planos de contas PGC-PE, fluxo de caixa, cálculo de salários, INSS, IRPS e preparação do balanço.",
                authorOrSource = "Núcleo de Gestão AJAF",
                pageCount = 52,
                publishedDate = "02/11/2026",
                contentSummary = "Unidade 1: Património, contas do activo, passivo e capital próprio.\nUnidade 2: Diário geral, razão e balancete de verificação.\nUnidade 3: Processamento salarial, INSS (3% trabalhador / 4% entidade) e retenção na fonte.\nUnidade 4: Gestão de tesouraria e microcrédito produtivo.",
                createdAt = now - 5 * dayMs
            ),
            LibraryDocumentEntity(
                id = "doc4",
                title = "Guia de Modelagem, Corte e Empreendedorismo Têxtil",
                category = "Moda & Ofícios",
                courseName = "Corte, Costura e Empreendedorismo",
                description = "Caderno técnico de medidas anatómicas, traçado de moldes base, operação de máquina industrial e formação de preço de venda.",
                authorOrSource = "Oficina Criativa AJAF",
                pageCount = 38,
                publishedDate = "12/11/2026",
                contentSummary = "Parte 1: Tomada de medidas e tabela padrão.\nParte 2: Molde base de camisa, saia e calça.\nParte 3: Costura recta, overlock e acabamentos profissionais.\nParte 4: Cálculo de metragem, margem de lucro e atendimento ao cliente.",
                createdAt = now - 2 * dayMs
            )
        )
        dao.insertLibraryDocs(initialDocs)

        val initialNotices = listOf(
            StudentNoticeEntity(
                id = "n1",
                title = "Prazo das Mensalidades de Novembro",
                message = "Caros formandos, lembrem-se que pagamentos efectuados após a data de vencimento sofrem acréscimo automático da multa regulamentar de 16%. Submetam o comprovativo M-Pesa ou E-Mola no portal.",
                category = "Propinas",
                createdAt = now - 3 * dayMs,
                authorName = "Direcção AJAF"
            ),
            StudentNoticeEntity(
                id = "n2",
                title = "Calendário de Exames Finais — Ciclo 2026",
                message = "Os exames práticos e teóricos decorrerão entre 14 e 19 de Dezembro. Apenas alunos com a Taxa de Exame aprovada pela Secretaria terão acesso à sala de exame.",
                category = "Exames",
                createdAt = now - dayMs,
                authorName = "Coordenação Pedagógica"
            ),
            StudentNoticeEntity(
                id = "n3",
                title = "Levantamento e Emissão de Certificados",
                message = "Após aprovação académica e liquidação da Taxa de Certificado, o documento passa pela assinatura do Director e fica disponível em PDF oficial.",
                category = "Certificados",
                createdAt = now,
                authorName = "Secretaria Geral AJAF"
            )
        )
        dao.insertNotices(initialNotices)

        // Dois alunos de demonstração para teste imediato (incluindo multa de 16% e recibo aprovado)
        val demoStudents = listOf(
            StudentEnrollmentEntity(
                id = "stu_armando",
                studentNumber = "AJAF-2026-0001",
                fullName = "Armando Sitoe",
                birthDate = "14/05/2002",
                nationality = "Moçambicana",
                province = "Maputo Cidade",
                courseId = "c_info",
                modality = "Presencial",
                phone = "841234567",
                status = EnrollmentStatus.ACTIVO,
                createdAt = now - 40 * dayMs
            ),
            StudentEnrollmentEntity(
                id = "stu_celeste",
                studentNumber = "AJAF-2026-0002",
                fullName = "Celeste Mondlane",
                birthDate = "22/08/2001",
                nationality = "Moçambicana",
                province = "Sofala",
                courseId = "c_cont",
                modality = "Online",
                phone = "829876543",
                status = EnrollmentStatus.CONCLUIDO,
                createdAt = now - 70 * dayMs
            )
        )
        demoStudents.forEach { dao.insertStudent(it) }

        val demoPayments = listOf(
            PaymentRecordEntity(
                id = "pay_1",
                studentId = "stu_armando",
                studentNumber = "AJAF-2026-0001",
                studentName = "Armando Sitoe",
                courseId = "c_info",
                courseName = "Informática Aplicada e Redes",
                feeType = FeeType.INSCRICAO,
                referenceLabel = "Inscrição Inicial",
                baseAmountMzn = 500.0,
                dueDateMillis = now - 40 * dayMs,
                totalAmountMzn = 500.0,
                paymentMethod = PaymentMethod.M_PESA,
                transactionId = "MP2610128841",
                smsText = "Confirmado. Transferiste 500.00 MT para AJAF Formacao. ID: MP2610128841.",
                status = PaymentStatus.APROVADO,
                submittedAt = now - 40 * dayMs,
                reviewedAt = now - 39 * dayMs,
                reviewedBy = "Secretaria AJAF",
                authCode = "AJAF-REC-8841"
            ),
            PaymentRecordEntity(
                id = "pay_2",
                studentId = "stu_armando",
                studentNumber = "AJAF-2026-0001",
                studentName = "Armando Sitoe",
                courseId = "c_info",
                courseName = "Informática Aplicada e Redes",
                feeType = FeeType.MENSALIDADE,
                referenceLabel = "Mensalidade 1/3 (Outubro)",
                baseAmountMzn = 1800.0,
                dueDateMillis = now - 30 * dayMs,
                totalAmountMzn = 1800.0,
                paymentMethod = PaymentMethod.E_MOLA,
                transactionId = "EM2610259102",
                smsText = "E-Mola: Pagamento de 1.800,00 MT efectuado com sucesso para AJAF. Ref EM2610259102.",
                status = PaymentStatus.APROVADO,
                submittedAt = now - 31 * dayMs,
                reviewedAt = now - 30 * dayMs,
                reviewedBy = "Secretaria AJAF",
                authCode = "AJAF-REC-9102"
            ),
            // Mensalidade vencida para demonstrar multa automática de 16%!
            PaymentRecordEntity(
                id = "pay_3",
                studentId = "stu_armando",
                studentNumber = "AJAF-2026-0001",
                studentName = "Armando Sitoe",
                courseId = "c_info",
                courseName = "Informática Aplicada e Redes",
                feeType = FeeType.MENSALIDADE,
                referenceLabel = "Mensalidade 2/3 (Novembro)",
                baseAmountMzn = 1800.0,
                dueDateMillis = now - 6 * dayMs,
                totalAmountMzn = 1800.0,
                status = PaymentStatus.POR_PAGAR
            ),
            PaymentRecordEntity(
                id = "pay_4",
                studentId = "stu_armando",
                studentNumber = "AJAF-2026-0001",
                studentName = "Armando Sitoe",
                courseId = "c_info",
                courseName = "Informática Aplicada e Redes",
                feeType = FeeType.EXAME,
                referenceLabel = "Taxa de Exame Final",
                baseAmountMzn = 650.0,
                dueDateMillis = now + 15 * dayMs,
                totalAmountMzn = 650.0,
                status = PaymentStatus.POR_PAGAR
            ),
            PaymentRecordEntity(
                id = "pay_5",
                studentId = "stu_armando",
                studentNumber = "AJAF-2026-0001",
                studentName = "Armando Sitoe",
                courseId = "c_info",
                courseName = "Informática Aplicada e Redes",
                feeType = FeeType.CERTIFICADO,
                referenceLabel = "Taxa de Certificado Oficial",
                baseAmountMzn = 850.0,
                dueDateMillis = now + 25 * dayMs,
                totalAmountMzn = 850.0,
                status = PaymentStatus.POR_PAGAR
            ),
            // Celeste concluiu o curso e pagou o certificado (pronto para aprovação do Director)
            PaymentRecordEntity(
                id = "pay_6",
                studentId = "stu_celeste",
                studentNumber = "AJAF-2026-0002",
                studentName = "Celeste Mondlane",
                courseId = "c_cont",
                courseName = "Contabilidade e Gestão de PME",
                feeType = FeeType.CERTIFICADO,
                referenceLabel = "Taxa de Certificado Oficial",
                baseAmountMzn = 800.0,
                dueDateMillis = now - 10 * dayMs,
                totalAmountMzn = 800.0,
                paymentMethod = PaymentMethod.M_PESA,
                transactionId = "MP2611103319",
                smsText = "Confirmado. Transferiste 800.00 MT para AJAF Formacao. ID: MP2611103319.",
                status = PaymentStatus.APROVADO,
                submittedAt = now - 10 * dayMs,
                reviewedAt = now - 9 * dayMs,
                reviewedBy = "Secretaria AJAF",
                authCode = "AJAF-REC-3319"
            )
        )
        dao.insertPayments(demoPayments)

        dao.insertGrades(
            listOf(
                GradeRecordEntity(
                    id = "gr_armando",
                    studentId = "stu_armando",
                    studentNumber = "AJAF-2026-0001",
                    studentName = "Armando Sitoe",
                    courseId = "c_info",
                    courseName = "Informática Aplicada e Redes",
                    eval1 = 15.5,
                    eval2 = 16.0,
                    examGrade = null,
                    finalAverage = null,
                    academicStatus = AcademicStatus.EM_CURSO,
                    updatedBy = "Sistema AJAF",
                    updatedAt = now - 5 * dayMs
                ),
                GradeRecordEntity(
                    id = "gr_celeste",
                    studentId = "stu_celeste",
                    studentNumber = "AJAF-2026-0002",
                    studentName = "Celeste Mondlane",
                    courseId = "c_cont",
                    courseName = "Contabilidade e Gestão de PME",
                    eval1 = 17.0,
                    eval2 = 16.5,
                    examGrade = 18.0,
                    finalAverage = 17.4,
                    academicStatus = AcademicStatus.APROVADO,
                    updatedBy = "Sistema AJAF",
                    updatedAt = now - 12 * dayMs
                )
            )
        )

        dao.insertCertificate(
            CertificateRecordEntity(
                id = "cert_celeste",
                studentId = "stu_celeste",
                studentNumber = "AJAF-2026-0002",
                studentName = "Celeste Mondlane",
                courseId = "c_cont",
                courseName = "Contabilidade e Gestão de PME",
                durationHours = 120,
                finalGrade = 17.4,
                directorApproved = false,
                issuedBySuperadmin = false,
                certificateCode = "AJAF-CERT-2026-0002"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_init",
                actorName = "Sistema AJAF",
                actorRole = "Sistema",
                action = "Inicialização da Plataforma",
                details = "Catálogo inicial de cursos, biblioteca pública e registos de demonstração carregados. Aguarda configuração da primeira conta de Superadministrador.",
                timestamp = now - 40 * dayMs
            )
        )
    }

    // --- Segurança e Autenticação ---

    private fun hashPassword(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("AJAF_MZ_SALT::$raw".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun registerFirstSuperadmin(
        fullName: String,
        email: String,
        password: String
    ): Result<StaffUserEntity> {
        if (dao.getSuperadminCount() > 0) {
            return Result.failure(IllegalStateException("Registo privilegiado bloqueado: já existe um Superadministrador registado."))
        }
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Introduza um e-mail institucional válido."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("A palavra-passe deve ter pelo menos 6 caracteres."))
        }
        val user = StaffUserEntity(
            id = "staff_${UUID.randomUUID().toString().take(8)}",
            fullName = fullName.trim(),
            email = cleanEmail,
            passwordHash = hashPassword(password),
            role = UserRole.SUPERADMIN,
            active = true
        )
        dao.insertStaff(user)
        logAction(user.fullName, user.role.labelPt, "Criação do Superadministrador Inicial", "Primeira conta administrativa configurada (${user.email}). Auto-registo privilegiado bloqueado.")
        return Result.success(user)
    }

    suspend fun loginStaff(email: String, password: String, expectedRole: UserRole): Result<StaffUserEntity> {
        val user = dao.findStaffByEmail(email.trim())
            ?: return Result.failure(IllegalArgumentException("Conta não encontrada para este e-mail."))
        if (!user.active) {
            return Result.failure(IllegalStateException("Esta conta encontra-se desactivada pelo Superadministrador."))
        }
        if (user.role != expectedRole) {
            return Result.failure(IllegalArgumentException("Esta conta pertence ao perfil ${user.role.labelPt} e não a ${expectedRole.labelPt}."))
        }
        if (user.passwordHash != hashPassword(password)) {
            return Result.failure(IllegalArgumentException("Palavra-passe incorrecta."))
        }
        logAction(user.fullName, user.role.labelPt, "Inicio de Sessão", "Acesso autenticado ao portal ${user.role.labelPt}.")
        return Result.success(user)
    }

    suspend fun loginStudent(identifier: String, phone: String): Result<StudentEnrollmentEntity> {
        val student = dao.findStudentForLogin(identifier, phone)
            ?: return Result.failure(IllegalArgumentException("Aluno não encontrado. Verifique o Nome Completo ou Nº de Estudante e o Contacto Telefónico."))
        if (student.status == EnrollmentStatus.REJEITADO) {
            return Result.failure(IllegalStateException("A sua inscrição foi rejeitada pela Secretaria. Contacte a administração da AJAF."))
        }
        return Result.success(student)
    }

    suspend fun createInternalStaff(
        actorName: String,
        actorRole: UserRole,
        fullName: String,
        email: String,
        password: String,
        targetRole: UserRole
    ): Result<StaffUserEntity> {
        if (targetRole == UserRole.ALUNO || targetRole == UserRole.SUPERADMIN) {
            return Result.failure(IllegalArgumentException("Perfil inválido para criação interna."))
        }
        if (actorRole == UserRole.SECRETARIA && targetRole != UserRole.FORMADOR) {
            return Result.failure(IllegalStateException("A Secretaria só pode cadastrar Formadores."))
        }
        val cleanEmail = email.trim().lowercase()
        if (dao.findStaffByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("Já existe um utilizador registado com este e-mail."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("A palavra-passe deve ter no mínimo 6 caracteres."))
        }
        val user = StaffUserEntity(
            id = "staff_${UUID.randomUUID().toString().take(8)}",
            fullName = fullName.trim(),
            email = cleanEmail,
            passwordHash = hashPassword(password),
            role = targetRole,
            active = true
        )
        dao.insertStaff(user)
        logAction(actorName, actorRole.labelPt, "Cadastro de Utilizador Interno", "Criada conta de ${targetRole.labelPt} para ${user.fullName} (${user.email}).")
        return Result.success(user)
    }

    suspend fun toggleStaffActive(actorName: String, staff: StaffUserEntity) {
        if (staff.role == UserRole.SUPERADMIN) return
        val updated = staff.copy(active = !staff.active)
        dao.updateStaff(updated)
        logAction(
            actorName,
            UserRole.SUPERADMIN.labelPt,
            "Alteração de Permissão",
            "${updated.fullName} (${updated.role.labelPt}) marcado como ${if (updated.active) "Activo" else "Inactivo"}."
        )
    }

    // --- Inscrição Pública e Pagamentos ---

    suspend fun submitOnlineEnrollment(
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
        screenshotSourceUri: Uri?
    ): Result<StudentEnrollmentEntity> {
        val course = dao.getCourseById(courseId)
            ?: return Result.failure(IllegalArgumentException("Curso seleccionado não encontrado."))

        if (paymentMethod.requiresDigitalProof) {
            if (transactionId.isNullOrBlank() || smsText.isNullOrBlank() || screenshotSourceUri == null) {
                return Result.failure(IllegalArgumentException("Para ${paymentMethod.labelPt}, o ID da Transacção, texto SMS e captura de ecrã são obrigatórios."))
            }
        }

        val count = dao.getStudentCount() + 1
        val studentNumber = "AJAF-2026-%04d".format(count)
        val studentId = "stu_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val savedScreenshot = screenshotSourceUri?.let { copyUriToInternal(it, "proof_${studentId}.jpg") }

        val student = StudentEnrollmentEntity(
            id = studentId,
            studentNumber = studentNumber,
            fullName = fullName.trim(),
            birthDate = birthDate.trim(),
            nationality = nationality.trim(),
            province = province,
            courseId = course.id,
            modality = modality,
            phone = phone.trim(),
            status = EnrollmentStatus.PENDENTE,
            createdAt = now
        )
        dao.insertStudent(student)

        // Cria o registo de pagamento da inscrição + plano de taxas do curso
        val payments = mutableListOf<PaymentRecordEntity>()
        payments.add(
            PaymentRecordEntity(
                id = "pay_${UUID.randomUUID().toString().take(8)}",
                studentId = studentId,
                studentNumber = studentNumber,
                studentName = student.fullName,
                courseId = course.id,
                courseName = course.name,
                feeType = FeeType.INSCRICAO,
                referenceLabel = "Inscrição Inicial",
                baseAmountMzn = course.enrollmentFeeMzn,
                dueDateMillis = now,
                totalAmountMzn = course.enrollmentFeeMzn,
                paymentMethod = paymentMethod,
                transactionId = transactionId?.trim(),
                smsText = smsText?.trim(),
                screenshotPath = savedScreenshot,
                status = PaymentStatus.PENDENTE,
                submittedAt = now
            )
        )
        for (m in 1..course.durationMonths) {
            payments.add(
                PaymentRecordEntity(
                    id = "pay_${UUID.randomUUID().toString().take(8)}",
                    studentId = studentId,
                    studentNumber = studentNumber,
                    studentName = student.fullName,
                    courseId = course.id,
                    courseName = course.name,
                    feeType = FeeType.MENSALIDADE,
                    referenceLabel = "Mensalidade $m/${course.durationMonths}",
                    baseAmountMzn = course.monthlyFeeMzn,
                    dueDateMillis = now + (m * 30L * dayMs),
                    totalAmountMzn = course.monthlyFeeMzn,
                    status = PaymentStatus.POR_PAGAR
                )
            )
        }
        payments.add(
            PaymentRecordEntity(
                id = "pay_${UUID.randomUUID().toString().take(8)}",
                studentId = studentId,
                studentNumber = studentNumber,
                studentName = student.fullName,
                courseId = course.id,
                courseName = course.name,
                feeType = FeeType.EXAME,
                referenceLabel = "Taxa de Exame Final",
                baseAmountMzn = course.examFeeMzn,
                dueDateMillis = now + (course.durationMonths * 30L * dayMs),
                totalAmountMzn = course.examFeeMzn,
                status = PaymentStatus.POR_PAGAR
            )
        )
        payments.add(
            PaymentRecordEntity(
                id = "pay_${UUID.randomUUID().toString().take(8)}",
                studentId = studentId,
                studentNumber = studentNumber,
                studentName = student.fullName,
                courseId = course.id,
                courseName = course.name,
                feeType = FeeType.CERTIFICADO,
                referenceLabel = "Taxa de Certificado Oficial",
                baseAmountMzn = course.certificateFeeMzn,
                dueDateMillis = now + ((course.durationMonths + 1) * 30L * dayMs),
                totalAmountMzn = course.certificateFeeMzn,
                status = PaymentStatus.POR_PAGAR
            )
        )
        dao.insertPayments(payments)

        // Cria registo inicial na pauta
        dao.insertGrade(
            GradeRecordEntity(
                id = "gr_${UUID.randomUUID().toString().take(8)}",
                studentId = studentId,
                studentNumber = studentNumber,
                studentName = student.fullName,
                courseId = course.id,
                courseName = course.name,
                academicStatus = AcademicStatus.EM_CURSO,
                updatedBy = "Inscrição Online",
                updatedAt = now
            )
        )

        logAction(
            student.fullName,
            "Candidato",
            "Nova Inscrição Online",
            "Protocolo $studentNumber submetido para ${course.name} (${modality}) via ${paymentMethod.labelPt}."
        )
        return Result.success(student)
    }

    suspend fun submitStudentPaymentProof(
        paymentId: String,
        studentName: String,
        method: PaymentMethod,
        transactionId: String?,
        smsText: String?,
        screenshotSourceUri: Uri?
    ): Result<Unit> {
        val raw = dao.getPaymentById(paymentId)
            ?: return Result.failure(IllegalArgumentException("Registo financeiro não encontrado."))
        if (raw.lockedByAdmin) {
            return Result.failure(IllegalStateException("Este registo foi marcado como Não Editável pelo Superadministrador."))
        }
        if (method.requiresDigitalProof) {
            if (transactionId.isNullOrBlank() || smsText.isNullOrBlank() || screenshotSourceUri == null) {
                return Result.failure(IllegalArgumentException("Para ${method.labelPt}, anexe ID da Transacção, mensagem SMS e captura de ecrã."))
            }
        }
        val effective = computeEffectivePayment(raw)
        val savedScreenshot = screenshotSourceUri?.let { copyUriToInternal(it, "proof_${paymentId}.jpg") }
        val updated = effective.copy(
            paymentMethod = method,
            transactionId = transactionId?.trim(),
            smsText = smsText?.trim(),
            screenshotPath = savedScreenshot ?: effective.screenshotPath,
            status = PaymentStatus.PENDENTE,
            submittedAt = System.currentTimeMillis()
        )
        dao.updatePayment(updated)
        logAction(
            studentName,
            UserRole.ALUNO.labelPt,
            "Submissão de Comprovativo",
            "${updated.referenceLabel} (${AjafFormatters.formatMzn(updated.totalAmountMzn)}) enviada via ${method.labelPt}."
        )
        return Result.success(Unit)
    }

    suspend fun reviewPayment(
        actorName: String,
        actorRole: UserRole,
        paymentId: String,
        approved: Boolean
    ): Result<Unit> {
        val raw = dao.getPaymentById(paymentId)
            ?: return Result.failure(IllegalArgumentException("Pagamento não encontrado."))
        if (raw.lockedByAdmin) {
            return Result.failure(IllegalStateException("Registo trancado pelo Superadministrador (Não Editável)."))
        }
        val effective = computeEffectivePayment(raw)
        val now = System.currentTimeMillis()
        val updated = effective.copy(
            status = if (approved) PaymentStatus.APROVADO else PaymentStatus.REJEITADO,
            reviewedAt = now,
            reviewedBy = actorName,
            authCode = if (approved) "AJAF-REC-${UUID.randomUUID().toString().take(6).uppercase()}" else null
        )
        dao.updatePayment(updated)

        // Se for inscrição, actualiza o estado da matrícula do aluno
        if (updated.feeType == FeeType.INSCRICAO) {
            dao.getStudentById(updated.studentId)?.let { stu ->
                val newStatus = if (approved) EnrollmentStatus.ACTIVO else EnrollmentStatus.REJEITADO
                dao.updateStudent(stu.copy(status = newStatus))
            }
        }

        // Se for certificado aprovado e aluno já tem média aprovada, cria registo de certificado se não existir
        if (approved && updated.feeType == FeeType.CERTIFICADO) {
            ensureCertificateRecordIfEligible(updated.studentId)
        }

        val verb = if (approved) "Aprovação" else "Rejeição"
        logAction(
            actorName,
            actorRole.labelPt,
            "$verb de Pagamento",
            "${updated.referenceLabel} de ${updated.studentName} (${AjafFormatters.formatMzn(updated.totalAmountMzn)}) — ${updated.status.labelPt}."
        )
        return Result.success(Unit)
    }

    // --- Formador & Pautas ---

    suspend fun assignTrainerToCourse(
        actorName: String,
        actorRole: UserRole,
        courseId: String,
        trainerId: String?
    ) {
        val course = dao.getCourseById(courseId) ?: return
        dao.updateCourse(course.copy(trainerId = trainerId))
        logAction(
            actorName,
            actorRole.labelPt,
            "Associação de Formador",
            "Curso '${course.name}' actualizado com formador ${trainerId ?: "Sem formador"}."
        )
    }

    suspend fun saveStudentGrades(
        actorName: String,
        actorRole: UserRole,
        grade: GradeRecordEntity,
        eval1: Double?,
        eval2: Double?,
        examGrade: Double?
    ): Result<Unit> {
        val course = dao.getCourseById(grade.courseId)
        if (course?.gradesLocked == true || grade.lockedByAdmin) {
            return Result.failure(IllegalStateException("Pauta bloqueada como Não Editável pelo Superadministrador."))
        }
        val validRange = listOfNotNull(eval1, eval2, examGrade).all { it in 0.0..20.0 }
        if (!validRange) {
            return Result.failure(IllegalArgumentException("As notas devem estar entre 0,0 e 20,0 valores."))
        }
        val avg = if (eval1 != null && eval2 != null && examGrade != null) {
            ((eval1 * 0.25) + (eval2 * 0.25) + (examGrade * 0.50))
        } else null
        val status = when {
            avg == null -> AcademicStatus.EM_CURSO
            avg >= 10.0 -> AcademicStatus.APROVADO
            else -> AcademicStatus.REPROVADO
        }
        val updated = grade.copy(
            eval1 = eval1,
            eval2 = eval2,
            examGrade = examGrade,
            finalAverage = avg,
            academicStatus = status,
            updatedBy = actorName,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateGrade(updated)

        if (status == AcademicStatus.APROVADO) {
            dao.getStudentById(grade.studentId)?.let { stu ->
                if (stu.status == EnrollmentStatus.ACTIVO) {
                    dao.updateStudent(stu.copy(status = EnrollmentStatus.CONCLUIDO))
                }
            }
            ensureCertificateRecordIfEligible(grade.studentId)
        }

        logAction(
            actorName,
            actorRole.labelPt,
            "Lançamento de Pauta",
            "Notas actualizadas para ${grade.studentName} em ${grade.courseName} (Média: ${AjafFormatters.formatGrade(avg)})."
        )
        return Result.success(Unit)
    }

    private suspend fun ensureCertificateRecordIfEligible(studentId: String) {
        val grade = dao.getGradeByStudent(studentId) ?: return
        if (grade.academicStatus != AcademicStatus.APROVADO || grade.finalAverage == null) return
        val certPaid = dao.getPaymentsByStudent(studentId)
            .any { it.feeType == FeeType.CERTIFICADO && it.status == PaymentStatus.APROVADO }
        if (!certPaid) return
        val existing = dao.getCertificateByStudent(studentId)
        if (existing != null) return
        val course = dao.getCourseById(grade.courseId) ?: return
        dao.insertCertificate(
            CertificateRecordEntity(
                id = "cert_${UUID.randomUUID().toString().take(8)}",
                studentId = studentId,
                studentNumber = grade.studentNumber,
                studentName = grade.studentName,
                courseId = grade.courseId,
                courseName = grade.courseName,
                durationHours = course.durationHours,
                finalGrade = grade.finalAverage,
                certificateCode = "AJAF-CERT-2026-${grade.studentNumber.takeLast(4)}"
            )
        )
    }

    // --- Director & Superadministrador: Certificados, Cursos, Biblioteca, Controlo de Edição ---

    suspend fun approveCertificateByDirector(directorName: String, cert: CertificateRecordEntity) {
        val updated = cert.copy(
            directorApproved = true,
            directorName = directorName,
            directorApprovedAt = System.currentTimeMillis()
        )
        dao.updateCertificate(updated)
        logAction(
            directorName,
            UserRole.DIRECTOR.labelPt,
            "Aprovação de Assinatura e Carimbo",
            "Certificado ${cert.certificateCode} de ${cert.studentName} aprovado oficialmente pelo Director."
        )
    }

    suspend fun markCertificateIssuedBySuperadmin(adminName: String, cert: CertificateRecordEntity) {
        val updated = cert.copy(
            issuedBySuperadmin = true,
            issuedAt = System.currentTimeMillis()
        )
        dao.updateCertificate(updated)
        logAction(
            adminName,
            UserRole.SUPERADMIN.labelPt,
            "Emissão Oficial de Certificado",
            "Certificado oficial PDF ${cert.certificateCode} emitido para ${cert.studentName}."
        )
    }

    suspend fun createOrUpdateCourse(
        adminName: String,
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
        val course = CourseEntity(
            id = "c_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            category = category.trim(),
            description = description.trim(),
            durationHours = durationHours,
            durationMonths = durationMonths,
            enrollmentFeeMzn = enrollmentFee,
            monthlyFeeMzn = monthlyFee,
            examFeeMzn = examFee,
            certificateFeeMzn = certificateFee,
            modalities = modalities
        )
        dao.insertCourse(course)
        logAction(adminName, UserRole.SUPERADMIN.labelPt, "Publicação de Curso", "Novo curso '${course.name}' publicado.")
    }

    suspend fun toggleCourseGradesLock(adminName: String, course: CourseEntity) {
        val updated = course.copy(gradesLocked = !course.gradesLocked)
        dao.updateCourse(updated)
        val courseGrades = dao.getGradesByCourse(course.id)
        courseGrades.forEach { g ->
            dao.updateGrade(g.copy(lockedByAdmin = updated.gradesLocked))
        }
        logAction(
            adminName,
            UserRole.SUPERADMIN.labelPt,
            "Controlo de Edição de Pauta",
            "Pauta de '${course.name}' definida como ${if (updated.gradesLocked) "Não Editável (Bloqueada)" else "Editável"}."
        )
    }

    suspend fun togglePaymentLock(adminName: String, payment: PaymentRecordEntity) {
        val updated = payment.copy(lockedByAdmin = !payment.lockedByAdmin)
        dao.updatePayment(updated)
        logAction(
            adminName,
            UserRole.SUPERADMIN.labelPt,
            "Controlo de Registo Financeiro",
            "Registo '${payment.referenceLabel}' de ${payment.studentName} definido como ${if (updated.lockedByAdmin) "Não Editável" else "Editável"}."
        )
    }

    suspend fun toggleAuditLock(adminName: String, log: AuditLogEntity) {
        val updated = log.copy(locked = !log.locked)
        dao.updateAuditLog(updated)
        logAction(
            adminName,
            UserRole.SUPERADMIN.labelPt,
            "Controlo de Registo Histórico",
            "Registo '${log.action}' marcado como ${if (updated.locked) "Não Editável (Selado)" else "Editável"}."
        )
    }

    suspend fun publishVacancy(
        adminName: String,
        courseId: String,
        title: String,
        summary: String,
        startDate: String,
        endDate: String,
        modalities: String,
        vacanciesCount: Int
    ) {
        val v = VacancyAnnouncementEntity(
            id = "vac_${UUID.randomUUID().toString().take(8)}",
            courseId = courseId,
            title = title.trim(),
            summary = summary.trim(),
            startDate = startDate.trim(),
            endDate = endDate.trim(),
            modalities = modalities,
            vacanciesCount = vacanciesCount
        )
        dao.insertVacancy(v)
        logAction(adminName, UserRole.SUPERADMIN.labelPt, "Anúncio de Vagas", "Publicado anúncio '${v.title}' ($vacanciesCount vagas).")
    }

    suspend fun deleteVacancy(adminName: String, id: String) {
        dao.deleteVacancy(id)
        logAction(adminName, UserRole.SUPERADMIN.labelPt, "Remoção de Anúncio", "Anúncio de vaga $id removido da página inicial.")
    }

    suspend fun publishStudentNotice(
        adminName: String,
        title: String,
        message: String,
        category: String
    ) {
        val notice = StudentNoticeEntity(
            id = "not_${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            message = message.trim(),
            category = category,
            authorName = adminName
        )
        dao.insertNotice(notice)
        logAction(adminName, UserRole.SUPERADMIN.labelPt, "Comunicado aos Alunos", "Publicado aviso '${notice.title}' ($category).")
    }

    suspend fun publishLibraryDocument(
        adminName: String,
        title: String,
        category: String,
        courseName: String,
        description: String,
        contentSummary: String,
        pdfUri: Uri?
    ) {
        val id = "doc_${UUID.randomUUID().toString().take(8)}"
        val copiedPdf = pdfUri?.let { copyUriToInternal(it, "$id.pdf") }
        val doc = LibraryDocumentEntity(
            id = id,
            title = title.trim(),
            category = category.trim(),
            courseName = courseName.trim(),
            description = description.trim(),
            authorOrSource = adminName,
            pageCount = if (copiedPdf != null) 12 else 24,
            publishedDate = AjafFormatters.formatDate(System.currentTimeMillis()),
            contentSummary = contentSummary.trim().ifBlank { description.trim() },
            localPdfPath = copiedPdf
        )
        dao.insertLibraryDoc(doc)
        logAction(adminName, UserRole.SUPERADMIN.labelPt, "Publicação na Biblioteca", "Documento '${doc.title}' publicado na Biblioteca Técnica Digital.")
    }

    suspend fun deleteLibraryDocument(adminName: String, doc: LibraryDocumentEntity) {
        dao.deleteLibraryDoc(doc.id)
        logAction(adminName, UserRole.SUPERADMIN.labelPt, "Remoção da Biblioteca", "Documento '${doc.title}' removido da biblioteca pública.")
    }

    private suspend fun logAction(actor: String, role: String, action: String, details: String) {
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_${UUID.randomUUID().toString().take(10)}",
                actorName = actor,
                actorRole = role,
                action = action,
                details = details
            )
        )
    }

    private fun copyUriToInternal(uri: Uri, targetName: String): String? {
        return try {
            val dir = File(context.filesDir, "uploads").apply { mkdirs() }
            val outFile = File(dir, targetName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
