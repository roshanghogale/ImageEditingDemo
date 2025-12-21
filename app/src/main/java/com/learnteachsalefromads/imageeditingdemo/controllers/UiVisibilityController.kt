package com.learnteachsalefromads.imageeditingdemo.controllers

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class UiVisibilityController(
    private val toolRecycler: RecyclerView
) {

    fun toggleTools() {
        toolRecycler.visibility =
            if (toolRecycler.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    fun hideTools() {
        toolRecycler.visibility = View.GONE
    }
}
