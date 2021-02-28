package com.apps.kunalfarmah.echo.di
import android.content.Context
import androidx.room.Room
import com.apps.kunalfarmah.echo.database.EchoSongsDatabase
import com.apps.kunalfarmah.echo.database.dao.EchoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object RoomModule {

    @Singleton
    @Provides
    fun provideEchoSongsDb(@ApplicationContext context: Context): EchoSongsDatabase {
        return Room
            .databaseBuilder(
                context,
                EchoSongsDatabase::class.java,
                EchoSongsDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideSongsDAO(echoSongsDatabase: EchoSongsDatabase): EchoDao {
        return echoSongsDatabase.SongsDao()
    }
}