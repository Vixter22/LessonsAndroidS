package com.example.log_reg.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WishlistItemDao {

    // Вставка нового елемента в вішліст (якщо товар вже є, новий не вставиться)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(wishlistItem: WishlistItem): Long

    // Видалення елемента з вішлісту
    @Delete
    fun delete(wishlistItem: WishlistItem)

    // Отримати вішліст для конкретного користувача (як LiveData)
    @Query("SELECT * FROM wishlist WHERE userId = :userId")
    fun getWishlistForUser(userId: Int): LiveData<List<WishlistItem>>

    // Синхронна версія отримання вішлісту для користувача
    @Query("SELECT * FROM wishlist WHERE userId = :userId")
    fun getWishlistForUserSync(userId: Int): List<WishlistItem>

    // Перевірка, чи є конкретний товар у вішлісті користувача
    @Query("SELECT * FROM wishlist WHERE userId = :userId AND productId = :productId LIMIT 1")
    fun getWishlistItem(userId: Int, productId: Int): WishlistItem?

    // Отримання продуктів, що знаходяться у вішлісті користувача через JOIN
    @Query("SELECT p.* FROM products p INNER JOIN wishlist w ON p.id = w.productId WHERE w.userId = :userId")
    fun getWishlistProducts(userId: Int): LiveData<List<Product>>
}
