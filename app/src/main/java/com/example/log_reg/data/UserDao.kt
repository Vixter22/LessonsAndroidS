package com.example.log_reg.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // Повертаємо ідентифікатор вставленого рядка. Якщо не потрібен, можна використовувати Unit.
    @Insert
    fun registerUser(user: User): Long

    // Повертаємо Flow для спостереження за даними користувача
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUser(username: String): Flow<User?>

    // Для перевірки логіну – повертаємо Flow, який можна перетворити на LiveData або використати .first()
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    fun checkUser(username: String, password: String): Flow<User?>

    // Повертаємо кількість оновлених рядків
    @Update
    fun updateUserProfile(user: User): Int

    // Повертаємо кількість видалених рядків
    @Query("DELETE FROM users WHERE username = :username")
    fun deleteUser(username: String): Int

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserProfile(username: String): LiveData<User>

}
