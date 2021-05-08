package com.example.socailapp.di

import android.app.Application
import androidx.room.Room
import com.example.socailapp.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application) = Room.databaseBuilder(app, UserDatabase::class.java, "user_database")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideUserDao(db: UserDatabase) = db.getUserDao()

}