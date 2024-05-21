package com.arsad.zakappsnfc.data.di

import com.arsad.zakappsnfc.data.impl.NfcRepositoryImpl
import com.arsad.zakappsnfc.domain.NfcRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NfcModule {
    @Binds
    @Singleton
    abstract fun bindNfcRepository(
        nfcRepositoryImpl: NfcRepositoryImpl
    ): NfcRepository
}