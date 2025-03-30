package com.example.log_reg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.data.Order

class OrderListAdapter(
    private var orderList: List<Order>
) : RecyclerView.Adapter<OrderListAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val ivOrderAction: ImageView = itemView.findViewById(R.id.ivOrderAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_list, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orderList.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.tvOrderId.text = order.id.toString()
        holder.tvOrderStatus.text = order.status
        holder.tvOrderDate.text = order.orderDate
        // Виводимо тільки число, без "грн"
        holder.tvOrderTotal.text = order.totalCost.toString()
        holder.ivOrderAction.setOnClickListener {
            // Реалізуйте перехід до деталей замовлення, якщо потрібно
        }
    }

    fun updateList(newList: List<Order>) {
        orderList = newList
        notifyDataSetChanged()
    }
}
