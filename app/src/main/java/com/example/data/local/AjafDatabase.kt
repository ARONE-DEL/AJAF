package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.AcademicStatus
import com.example.data.model.EnrollmentStatus
import com.example.data.model.FeeType
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.UserRole

class AjafTypeConverters {
    @TypeConverter fun fromUserRole(v: UserRole): String = v.name
    @TypeConverter fun toUserRole(v: String): UserRole = UserRole.valueOf(v)

    @TypeConverter fun fromEnrollmentStatus(v: EnrollmentStatus): String = v.name
    @TypeConverter fun toEnrollmentStatus(v: String): EnrollmentStatus = EnrollmentStatus.valueOf(v)

    @TypeConverter fun fromFeeType(v: FeeType): String = v.name
    @TypeConverter fun toFeeType(v: String): FeeType = FeeType.valueOf(v)

    @TypeConverter fun fromPaymentMethod(v: PaymentMethod?): String? = v?.name
    @TypeConverter fun toPaymentMethod(v: String?): PaymentMethod? = v?.let { PaymentMethod.valueOf(it) }

    @TypeConverter fun fromPaymentStatus(v: PaymentStatus): String = v.name
    @TypeConverter fun toPaymentStatus(v: String): PaymentStatus = PaymentStatus.valueOf(v)

    @TypeConverter fun fromAcademicStatus(v: AcademicStatus): String = v.name
    @TypeConverter fun toAcademicStatus(v: String): AcademicStatus = AcademicStatus.valueOf(v)
}

@Database(
    entities = [
        StaffUserEntity::class,
        CourseEntity::class,
        VacancyAnnouncementEntity::class,
        StudentNoticeEntity::class,
        LibraryDocumentEntity::class,
        StudentEnrollmentEntity::class,
        PaymentRecordEntity::class,
        GradeRecordEntity::class,
        CertificateRecordEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AjafTypeConverters::class)
abstract class AjafDatabase : RoomDatabase() {
    abstract fun ajafDao(): AjafDao

    companion object {
        @Volatile
        private var INSTANCE: AjafDatabase? = null

        fun getInstance(context: Context): AjafDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AjafDatabase::class.java,
                    "ajaf_centro.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
