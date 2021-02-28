package com.apps.kunalfarmah.echo.di


import com.apps.kunalfarmah.echo.repository.SongsRepository
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object ViewModelModule {

    @Singleton
    @Provides
    fun provideSongsViewModel(
            songsRepository: SongsRepository
    ): SongsViewModel {
        return SongsViewModel(songsRepository)
    }
}














