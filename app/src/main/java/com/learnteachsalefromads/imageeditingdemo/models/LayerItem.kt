package com.learnteachsalefromads.imageeditingdemo.models

import android.widget.FrameLayout
import android.widget.ImageView

data class LayerItem(
    val name: String,
    val container: FrameLayout,
    val imageView: ImageView,
    var rotation: Float = 0f,        // ✅ STORED ROTATION
    var isVisible: Boolean = true,
    var isLocked: Boolean = false
)
