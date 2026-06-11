package com.monohelper.di

import com.monohelper.data.repo.AccountRepository
import com.monohelper.data.repo.AccountRepositoryImpl
import com.monohelper.data.repo.ReportRepository
import com.monohelper.data.repo.ReportRepositoryImpl
import com.monohelper.data.repo.SyncRepository
import com.monohelper.data.repo.SyncRepositoryImpl
import com.monohelper.data.repo.TransactionRepository
import com.monohelper.data.repo.TransactionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
