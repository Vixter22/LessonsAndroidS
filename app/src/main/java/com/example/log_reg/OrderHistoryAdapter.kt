package com.example.log_reg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrderHistoryAdapter(private val orders: List<OrderHistoryDisplayItem>) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_history_item, parent, false)
        return OrderHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val item = orders[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = orders.size

    class OrderHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        private val rvOrderItems: RecyclerView = itemView.findViewById(R.id.rvOrderItems)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)

        fun bind(item: OrderHistoryDisplayItem) {
            // Відображаємо статус (перетворюємо коди, якщо потрібно)
            tvOrderStatus.text = when(item.order.status) {
                "pending" -> "Оформлено"
                "confirmed" -> "Виконано"
                "canceled" -> "Скасовано"
                else -> item.order.status
            }
            // Відображаємо дату/час замовлення
            tvOrderDate.text = item.order.orderDate

            // Налаштовуємо горизонтальний RecyclerView для позицій замовлення
            rvOrderItems.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            rvOrderItems.adapter = OrderItemHistoryAdapter(item.orderItems)

            // Відображаємо суму замовлення
            tvOrderTotal.text = "Загальна сума замовлення: ${item.order.totalCost} грн"
        }
    }
}
