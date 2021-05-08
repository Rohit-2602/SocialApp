package com.example.socailapp

import androidx.room.*
import com.example.socailapp.data.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: User)

    @Query("DELETE FROM user_table")
    fun deleteAll()

    @Query("SELECT * FROM user_table")
    fun getAllUser() : Flow<List<User>>

    @Query("UPDATE user_table SET backgroundImage =:backgroundImage WHERE id = :userId")
    fun updateUserBackgroundImage(userId: String, backgroundImage: String)

    @Query("SELECT * FROM user_table WHERE id LIKE '%' || :userId || '%'")
    fun getCurrentUser(userId: String): Flow<List<User>>

}