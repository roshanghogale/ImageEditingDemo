package com.learnteachsalefromads.imageeditingdemo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.learnteachsalefromads.imageeditingdemo.R
import com.learnteachsalefromads.imageeditingdemo.databinding.ItemLayerHorizontalBinding
import com.learnteachsalefromads.imageeditingdemo.layer.LayerManager

class LayerAdapter(
    private val manager: LayerManager,
    private val onLayerClick: (Int) -> Unit
) : RecyclerView.Adapter<LayerAdapter.VH>() {

    inner class VH(val b: ItemLayerHorizontalBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(ItemLayerHorizontalBinding.inflate(
            LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {

        val index = manager.layers.lastIndex - pos
        val layer = manager.layers[index]

        h.b.txtLayerName.text = layer.name
        h.b.imgPreview.setImageDrawable(layer.imageView.drawable)

        h.b.rootItem.alpha = if (layer.isVisible) 1f else 0.4f

        h.b.rootItem.background =
            ContextCompat.getDrawable(
                h.itemView.context,
                if (index == manager.selectedIndex)
                    R.drawable.bg_layer_item_selected
                else R.drawable.bg_layer_item
            )

        h.b.btnMenu.visibility =
            if (index == manager.selectedIndex) View.VISIBLE else View.GONE

        h.b.rootItem.setOnClickListener { onLayerClick(index) }
    }

    override fun getItemCount(): Int = manager.layers.size

    fun moveItem(from: Int, to: Int) {
        manager.move(
            manager.layers.lastIndex - from,
            manager.layers.lastIndex - to
        )
        notifyItemMoved(from, to)
    }

    fun selectAfterDrop(pos: Int) {
        manager.select(manager.layers.lastIndex - pos)
        notifyDataSetChanged()
    }
}
