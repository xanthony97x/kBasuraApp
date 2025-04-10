package com.marcoescalera.kbasura

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class WasteAdapter(private val items: List<WasteItem>) :
    RecyclerView.Adapter<WasteAdapter.WasteViewHolder>() {

    class WasteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.card_view)
        val imageView: ImageView = view.findViewById(R.id.image_view)
        val textView: TextView = view.findViewById(R.id.text_view)
        val textExamples: TextView = view.findViewById(R.id.text_examples)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WasteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_waste_card, parent, false)
        return WasteViewHolder(view)
    }

    override fun onBindViewHolder(holder: WasteViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.setImageResource(item.imageResId)
        holder.textView.text = item.name
        holder.textExamples.text = "Ej: ${item.examples}"
    }

    override fun getItemCount(): Int = items.size
}