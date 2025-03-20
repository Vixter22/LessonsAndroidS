package com.example.log_reg.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    // Синхронна вставка нового користувача
    @Insert
    fun registerUser(user: User): Long

    // Синхронне отримання користувача за username
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserSync(username: String): User?

    // Синхронна перевірка логіну – повертає користувача або null, якщо дані не співпадають
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    fun checkUserSync(username: String, password: String): User?

    // Синхронне оновлення профілю користувача
    @Update
    fun updateUserProfile(user: User): Int

    // Синхронне видалення користувача за username
    @Query("DELETE FROM users WHERE username = :username")
    fun deleteUser(username: String): Int

    // Отримання профілю користувача у вигляді LiveData
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserProfile(username: String): LiveData<User>
}
