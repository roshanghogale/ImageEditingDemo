package com.learnteachsalefromads.imageeditingdemo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.learnteachsalefromads.imageeditingdemo.R
import com.learnteachsalefromads.imageeditingdemo.databinding.ItemLayerHorizontalBinding
import com.learnteachsalefromads.imageeditingdemo.utils.LayerManager

/**
 * Horizontal layer strip adapter
 *
 * Responsibilities:
 * - Show layer preview
 * - Highlight selected layer
 * - Show menu overlay only for selected layer
 * - Support drag & drop ordering
 */
class LayerAdapter(
    private val manager: LayerManager,
    private val onLayerClick: (Int) -> Unit
) : RecyclerView.Adapter<LayerAdapter.VH>() {

    /* ================= VIEW HOLDER ================= */

    inner class VH(val binding: ItemLayerHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root)

    /* ================= REQUIRED OVERRIDE ================= */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLayerHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    /* ================= BIND ================= */

    override fun onBindViewHolder(holder: VH, position: Int) {

        val index = manager.layers.lastIndex - position
        val layer = manager.layers[index]

        holder.binding.txtLayerName.text = layer.name
        holder.binding.imgPreview.setImageDrawable(layer.imageView.drawable)

        val context = holder.itemView.context

        // ---------- Selection UI ----------
        if (index == manager.selectedIndex) {
            holder.binding.rootItem.background =
                ContextCompat.getDrawable(context, R.drawable.bg_layer_item_selected)
            holder.binding.btnMenu.visibility = View.VISIBLE
        } else {
            holder.binding.rootItem.background =
                ContextCompat.getDrawable(context, R.drawable.bg_layer_item)
            holder.binding.btnMenu.visibility = View.GONE
        }

        // ---------- Visibility UI (FADE EFFECT) ----------
        holder.binding.rootItem.alpha =
            if (layer.isVisible) 1.0f else 0.4f   // 👈 faded when hidden

        // ---------- Click ----------
        holder.binding.rootItem.setOnClickListener {
            onLayerClick(index)
        }
    }


    /* ================= COUNT ================= */

    override fun getItemCount(): Int = manager.layers.size

    /* ================= DRAG HELPERS ================= */

    fun moveItem(fromPosition: Int, toPosition: Int) {
        manager.move(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun selectAfterDrop(position: Int) {
        val index = manager.layers.lastIndex - position
        manager.select(index)
        notifyDataSetChanged()
    }
}
