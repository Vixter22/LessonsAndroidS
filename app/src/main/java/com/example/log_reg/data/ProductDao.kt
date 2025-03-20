package com.example.log_reg.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(products: List<Product>): List<Long>

    // LiveData для спостереження
    @Query("SELECT * FROM products")
    fun getAllProducts(): LiveData<List<Product>>

    // Синхронний виклик, щоб перевірити чи є дані
    @Query("SELECT * FROM products")
    fun getAllProductsSync(): List<Product>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    fun getProductByIdSync(productId: Int): Product?
}
