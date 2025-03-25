package com.example.log_reg.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(order: Order): Long

    @Query("SELECT * FROM orders WHERE userId = :userId")
    fun getOrdersForUser(userId: Int): List<Order>
}
