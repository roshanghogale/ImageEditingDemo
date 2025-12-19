package com.learnteachsalefromads.imageeditingdemo.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.learnteachsalefromads.imageeditingdemo.utils.LayerManager

class LayerDragCallback(
    private val manager: LayerManager,
    private val adapter: RecyclerView.Adapter<*>
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        val fromPos = viewHolder.bindingAdapterPosition
        val toPos = target.bindingAdapterPosition

        // Convert adapter position to layer index
        val fromIndex = manager.layers.lastIndex - fromPos
        val toIndex = manager.layers.lastIndex - toPos

        manager.move(fromIndex, toIndex)
        adapter.notifyItemMoved(fromPos, toPos)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // No swipe actions
    }

    override fun isLongPressDragEnabled(): Boolean = true
}
