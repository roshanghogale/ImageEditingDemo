package com.learnteachsalefromads.imageeditingdemo.models

import android.widget.ImageView

data class LayerItem(
    val name: String,
    val imageView: ImageView,
    var isVisible: Boolean = true
)
