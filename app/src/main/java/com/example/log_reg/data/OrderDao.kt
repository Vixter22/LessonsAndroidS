package com.example.log_reg.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(order: Order): Long

    @Query("SELECT * FROM orders")
    fun getAllOrders(): List<Order>

    @Query("SELECT * FROM orders WHERE userId = :userId")
    fun getOrdersForUser(userId: Int): List<Order>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderById(orderId: Int): Order?

    @Update
    fun updateOrder(order: Order)
}
