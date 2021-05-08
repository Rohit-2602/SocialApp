package com.example.socailapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.socailapp.data.User

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao

}