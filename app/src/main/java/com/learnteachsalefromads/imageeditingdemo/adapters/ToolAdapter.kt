package com.learnteachsalefromads.imageeditingdemo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learnteachsalefromads.imageeditingdemo.R
import com.learnteachsalefromads.imageeditingdemo.databinding.ItemToolBinding
import com.learnteachsalefromads.imageeditingdemo.models.ToolAction
import com.learnteachsalefromads.imageeditingdemo.models.ToolItem

class ToolAdapter(
    private val tools: MutableList<ToolItem>,
    private val onToolClick: (ToolItem) -> Unit
) : RecyclerView.Adapter<ToolAdapter.VH>() {

    inner class VH(val binding: ItemToolBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemToolBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tool = tools[position]
        holder.binding.imgTool.setImageResource(tool.icon)
        holder.binding.txtTool.text = tool.label
        holder.binding.root.setOnClickListener { onToolClick(tool) }
    }

    override fun getItemCount(): Int = tools.size

    /**
     * 🔥 Instant icon update — no fade, no animation
     */
    fun updateVisibilityTool(isVisible: Boolean) {
        val index = tools.indexOfFirst { it.id == ToolAction.TOGGLE_VISIBILITY }
        if (index == -1) return

        tools[index] = tools[index].copy(
            icon = if (isVisible)
                R.drawable.ic_eye_closed
            else
                R.drawable.ic_eye_open
        )

        notifyItemChanged(index)
    }
}
