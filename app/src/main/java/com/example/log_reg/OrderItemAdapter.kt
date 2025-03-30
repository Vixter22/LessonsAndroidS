package com.example.log_reg.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.R
import com.squareup.picasso.Picasso

class OrderItemAdapter(private val orderItems: List<OrderItemDisplay>) :
    RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item_card, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(orderItems[position])
    }

    override fun getItemCount(): Int = orderItems.size

    class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)

        fun bind(item: OrderItemDisplay) {
            tvProductName.text = item.product.name
            tvProductQuantity.text = "Кількість: ${item.orderItem.quantity}"
            tvProductPrice.text = "Ціна: ${item.orderItem.price} грн"
            Picasso.get().load(item.product.image).into(ivProductImage)
        }
    }
}
