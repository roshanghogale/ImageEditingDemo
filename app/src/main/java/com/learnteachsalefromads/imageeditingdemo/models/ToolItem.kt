package com.learnteachsalefromads.imageeditingdemo.models

import androidx.annotation.DrawableRes

/**
 * Generic tool definition.
 * Can be reused for Image, Text, Shape layers later.
 */
data class ToolItem(
    val id: ToolAction,
    @DrawableRes val icon: Int,
    val label: String
)

