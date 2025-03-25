package com.example.log_reg

import com.example.log_reg.data.Order
import com.example.log_reg.data.OrderItem
import com.example.log_reg.data.Product

data class OrderItemDisplay(
    val orderItem: OrderItem,
    val product: Product
)

data class OrderHistoryDisplayItem(
    val order: Order,
    val orderItems: List<OrderItemDisplay>
)
