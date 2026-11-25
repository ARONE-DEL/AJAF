package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AcademicStatus
import com.example.data.model.EnrollmentStatus
import com.example.data.model.FeeType
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.UserRole

@Entity(tableName = "staff_users")
data class StaffUserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val description: String,
    val durationHours: Int,
    val durationMonths: Int,
    val enrollmentFeeMzn: Double,
    val monthlyFeeMzn: Double,
    val examFeeMzn: Double,
    val certificateFeeMzn: Double,
    val modalities: String,
    val trainerId: String? = null,
    val active: Boolean = true,
    val gradesLocked: Boolean = false
)

@Entity(tableName = "vacancy_announcements")
data class VacancyAnnouncementEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val summary: String,
    val startDate: String,
    val endDate: String,
    val modalities: String,
    val vacanciesCount: Int,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_notices")
data class StudentNoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis(),
    val authorName: String
)

@Entity(tableName = "library_documents")
data class LibraryDocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val courseName: String,
    val description: String,
    val authorOrSource: String,
    val pageCount: Int,
    val publishedDate: String,
    val contentSummary: String,
    val localPdfPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_enrollments")
data class StudentEnrollmentEntity(
    @PrimaryKey val id: String,
    val studentNumber: String,
    val fullName: String,
    val birthDate: String,
    val nationality: String,
    val province: String,
    val courseId: String,
    val modality: String,
    val phone: String,
    val status: EnrollmentStatus,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_records")
data class PaymentRecordEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentNumber: String,
    val studentName: String,
    val courseId: String,
    val courseName: String,
    val feeType: FeeType,
    val referenceLabel: String,
    val baseAmountMzn: Double,
    val dueDateMillis: Long,
    val lateFeeApplied: Boolean = false,
    val lateFeeAmountMzn: Double = 0.0,
    val totalAmountMzn: Double = baseAmountMzn,
    val paymentMethod: PaymentMethod? = null,
    val transactionId: String? = null,
    val smsText: String? = null,
    val screenshotPath: String? = null,
    val status: PaymentStatus = PaymentStatus.POR_PAGAR,
    val submittedAt: Long? = null,
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null,
    val authCode: String? = null,
    val lockedByAdmin: Boolean = false
)

@Entity(tableName = "grade_records")
data class GradeRecordEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentNumber: String,
    val studentName: String,
    val courseId: String,
    val courseName: String,
    val eval1: Double? = null,
    val eval2: Double? = null,
    val examGrade: Double? = null,
    val finalAverage: Double? = null,
    val academicStatus: AcademicStatus = AcademicStatus.EM_CURSO,
    val updatedBy: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val lockedByAdmin: Boolean = false
)

@Entity(tableName = "certificate_records")
data class CertificateRecordEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentNumber: String,
    val studentName: String,
    val courseId: String,
    val courseName: String,
    val durationHours: Int,
    val finalGrade: Double,
    val directorApproved: Boolean = false,
    val directorName: String? = null,
    val directorApprovedAt: Long? = null,
    val issuedBySuperadmin: Boolean = false,
    val issuedAt: Long? = null,
    val certificateCode: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val locked: Boolean = false
)
