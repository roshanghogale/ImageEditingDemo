package com.learnteachsalefromads.imageeditingdemo.models

import android.widget.FrameLayout
import android.widget.ImageView

data class LayerItem(
    val name: String,
    val container: FrameLayout, // FULL canvas size
    val imageView: ImageView,   // actual image (moves, scales, rotates)
    var isVisible: Boolean = true,
    var isLocked: Boolean = false
)
