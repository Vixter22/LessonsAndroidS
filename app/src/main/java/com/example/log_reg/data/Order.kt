package com.example.log_reg.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId")]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val orderDate: String,
    val status: String, // наприклад, "pending", "confirmed", "canceled" тощо
    val recipientName: String,
    val recipientEmail: String,
    val deliveryInfo: String, // формат "Місто, відділення"
    val paymentMethod: String, // "при отриманні" або "карта"
    val totalCost: Double
)
