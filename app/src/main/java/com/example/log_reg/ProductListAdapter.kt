package com.example.log_reg.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.R
import com.example.log_reg.data.Product

class ProductListAdapter(private var products: List<Product>) :
    RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    // Callback для обробки кліку на іконку видалення
    var onDeleteClick: ((Product) -> Unit)? = null

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductId: TextView = itemView.findViewById(R.id.tvProductId)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_list, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.tvProductId.text = product.id?.toString() ?: ""
        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = product.price.toString()

        holder.ivDelete.setOnClickListener {
            onDeleteClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun setProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
