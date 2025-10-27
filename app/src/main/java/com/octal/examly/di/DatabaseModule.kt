package com.octal.examly.di

import android.content.Context
import androidx.room.Room
import com.octal.examly.data.local.PreferencesManager
import com.octal.examly.data.local.dao.*
import com.octal.examly.data.local.database.DatabaseSeeder
import com.octal.examly.data.local.database.ExamDatabase
import com.octal.examly.util.PasswordHasher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideExamDatabase(@ApplicationContext context: Context): ExamDatabase {
        return Room.databaseBuilder(
            context,
            ExamDatabase::class.java,
            "exam_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: ExamDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideSubjectDao(database: ExamDatabase): SubjectDao {
        return database.subjectDao()
    }

    @Provides
    @Singleton
    fun provideQuestionDao(database: ExamDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideAnswerDao(database: ExamDatabase): AnswerDao {
        return database.answerDao()
    }

    @Provides
    @Singleton
    fun provideTestDao(database: ExamDatabase): TestDao {
        return database.testDao()
    }

    @Provides
    @Singleton
    fun provideTestQuestionDao(database: ExamDatabase): TestQuestionDao {
        return database.testQuestionDao()
    }

    @Provides
    @Singleton
    fun provideTestAssignmentDao(database: ExamDatabase): TestAssignmentDao {
        return database.testAssignmentDao()
    }

    @Provides
    @Singleton
    fun provideTestAttemptDao(database: ExamDatabase): TestAttemptDao {
        return database.testAttemptDao()
    }

    @Provides
    @Singleton
    fun provideUserAnswerDao(database: ExamDatabase): UserAnswerDao {
        return database.userAnswerDao()
    }

    @Provides
    @Singleton
    fun provideTestResultDao(database: ExamDatabase): TestResultDao {
        return database.testResultDao()
    }

    @Provides
    @Singleton
    fun provideDatabaseSeeder(
        userDao: UserDao,
        passwordHasher: PasswordHasher
    ): DatabaseSeeder {
        return DatabaseSeeder(userDao, passwordHasher)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}