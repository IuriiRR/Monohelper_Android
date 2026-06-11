package com.monohelper.di

import com.monohelper.core.config.ApiConfig
import com.monohelper.data.config.PrefsApiConfig
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigModule {

    @Binds
    @Singleton
    abstract fun bindApiConfig(impl: PrefsApiConfig): ApiConfig
}
