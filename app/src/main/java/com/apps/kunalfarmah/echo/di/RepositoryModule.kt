package com.apps.kunalfarmah.echo.di

import android.content.Context
import com.apps.kunalfarmah.echo.database.CacheMapper
import com.apps.kunalfarmah.echo.database.dao.EchoDao
import com.apps.kunalfarmah.echo.repository.SongsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideSongsRepository(
            @ApplicationContext context: Context,
            echoDao: EchoDao,
            cacheMapper: CacheMapper
    ): SongsRepository {
        return SongsRepository(context, echoDao, cacheMapper)
    }
}














