package com.example.log_reg.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String,
    val role: String, // "ADMIN" або "CLIENT"
    val name: String,
    val email: String,
    @ColumnInfo(name = "birth_date")
    val birthDate: String,
    val about: String,
    val avatar: String? = null
)
