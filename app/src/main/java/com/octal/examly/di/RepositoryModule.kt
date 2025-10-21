package com.octal.examly.di

import com.octal.examly.data.repository.*
import com.octal.examly.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindSubjectRepository(
        subjectRepositoryImpl: SubjectRepositoryImpl
    ): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        questionRepositoryImpl: QuestionRepositoryImpl
    ): QuestionRepository

    @Binds
    @Singleton
    abstract fun bindTestRepository(
        testRepositoryImpl: TestRepositoryImpl
    ): TestRepository

    @Binds
    @Singleton
    abstract fun bindTestAssignmentRepository(
        testAssignmentRepositoryImpl: TestAssignmentRepositoryImpl
    ): TestAssignmentRepository

    @Binds
    @Singleton
    abstract fun bindTestAttemptRepository(
        testAttemptRepositoryImpl: TestAttemptRepositoryImpl
    ): TestAttemptRepository

    @Binds
    @Singleton
    abstract fun bindResultRepository(
        resultRepositoryImpl: ResultRepositoryImpl
    ): ResultRepository
}