package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AjafDao {
    // Staff & Auth
    @Query("SELECT * FROM staff_users ORDER BY createdAt DESC")
    fun observeStaffUsers(): Flow<List<StaffUserEntity>>

    @Query("SELECT COUNT(*) FROM staff_users WHERE role = 'SUPERADMIN'")
    fun observeSuperadminCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM staff_users WHERE role = 'SUPERADMIN'")
    suspend fun getSuperadminCount(): Int

    @Query("SELECT * FROM staff_users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun findStaffByEmail(email: String): StaffUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(user: StaffUserEntity)

    @Update
    suspend fun updateStaff(user: StaffUserEntity)

    // Courses
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    // Vacancies
    @Query("SELECT * FROM vacancy_announcements ORDER BY createdAt DESC")
    fun observeVacancies(): Flow<List<VacancyAnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacancy(vacancy: VacancyAnnouncementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacancies(vacancies: List<VacancyAnnouncementEntity>)

    @Query("DELETE FROM vacancy_announcements WHERE id = :id")
    suspend fun deleteVacancy(id: String)

    // Student Notices
    @Query("SELECT * FROM student_notices ORDER BY createdAt DESC")
    fun observeNotices(): Flow<List<StudentNoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: StudentNoticeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<StudentNoticeEntity>)

    @Query("DELETE FROM student_notices WHERE id = :id")
    suspend fun deleteNotice(id: String)

    // Library Documents
    @Query("SELECT * FROM library_documents ORDER BY createdAt DESC")
    fun observeLibraryDocuments(): Flow<List<LibraryDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryDoc(doc: LibraryDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryDocs(docs: List<LibraryDocumentEntity>)

    @Query("DELETE FROM library_documents WHERE id = :id")
    suspend fun deleteLibraryDoc(id: String)

    // Student Enrollments
    @Query("SELECT * FROM student_enrollments ORDER BY createdAt DESC")
    fun observeStudents(): Flow<List<StudentEnrollmentEntity>>

    @Query("SELECT * FROM student_enrollments WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: String): StudentEnrollmentEntity?

    @Query("SELECT COUNT(*) FROM student_enrollments")
    suspend fun getStudentCount(): Int

    @Query("""
        SELECT * FROM student_enrollments 
        WHERE (LOWER(TRIM(fullName)) = LOWER(TRIM(:identifier)) 
               OR UPPER(TRIM(studentNumber)) = UPPER(TRIM(:identifier)))
          AND REPLACE(REPLACE(TRIM(phone), ' ', ''), '+258', '') = REPLACE(REPLACE(TRIM(:phone), ' ', ''), '+258', '')
        LIMIT 1
    """)
    suspend fun findStudentForLogin(identifier: String, phone: String): StudentEnrollmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEnrollmentEntity)

    @Update
    suspend fun updateStudent(student: StudentEnrollmentEntity)

    // Payments
    @Query("SELECT * FROM payment_records ORDER BY dueDateMillis ASC")
    fun observePayments(): Flow<List<PaymentRecordEntity>>

    @Query("SELECT * FROM payment_records WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: String): PaymentRecordEntity?

    @Query("SELECT * FROM payment_records WHERE studentId = :studentId")
    suspend fun getPaymentsByStudent(studentId: String): List<PaymentRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentRecordEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentRecordEntity)

    // Grades
    @Query("SELECT * FROM grade_records ORDER BY studentName ASC")
    fun observeGrades(): Flow<List<GradeRecordEntity>>

    @Query("SELECT * FROM grade_records WHERE studentId = :studentId LIMIT 1")
    suspend fun getGradeByStudent(studentId: String): GradeRecordEntity?

    @Query("SELECT * FROM grade_records WHERE courseId = :courseId")
    suspend fun getGradesByCourse(courseId: String): List<GradeRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<GradeRecordEntity>)

    @Update
    suspend fun updateGrade(grade: GradeRecordEntity)

    // Certificates
    @Query("SELECT * FROM certificate_records ORDER BY studentName ASC")
    fun observeCertificates(): Flow<List<CertificateRecordEntity>>

    @Query("SELECT * FROM certificate_records WHERE studentId = :studentId LIMIT 1")
    suspend fun getCertificateByStudent(studentId: String): CertificateRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(cert: CertificateRecordEntity)

    @Update
    suspend fun updateCertificate(cert: CertificateRecordEntity)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun observeAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLogEntity>)

    @Update
    suspend fun updateAuditLog(log: AuditLogEntity)
}
