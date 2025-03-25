package com.example.log_reg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class OrderItemHistoryAdapter(private val items: List<OrderItemDisplay>) :
    RecyclerView.Adapter<OrderItemHistoryAdapter.OrderItemHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_history_item_card, parent, false)
        return OrderItemHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemHistoryViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class OrderItemHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)

        fun bind(item: OrderItemDisplay) {
            // Завантаження зображення товару (приклад із Picasso)
            Picasso.get().load(item.product.image).into(ivProductImage)
            tvProductName.text = item.product.name
            tvProductQuantity.text = "Кількість: ${item.orderItem.quantity}"
            tvProductPrice.text = "Ціна: ${item.product.price} грн"
        }
    }
}
