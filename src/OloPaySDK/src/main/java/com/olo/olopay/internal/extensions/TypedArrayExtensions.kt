// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.extensions

import android.content.Context
import android.content.res.TypedArray
import androidx.core.content.withStyledAttributes
import kotlin.math.abs

private const val EPSILON = 0.00000001f
private const val FIRST_INDEX = 0
private const val UNDEFINED_FLOAT = Float.MIN_VALUE
private const val UNDEFINED_INT = Integer.MIN_VALUE

private fun Float.withinTolerance(other: Float) = abs(this - other) < EPSILON

internal fun TypedArray.getColorOrNull(index: Int): Int? {
    val color = getColor(index, UNDEFINED_INT)
    return if (color == UNDEFINED_INT) null else color
}

internal fun TypedArray.getDimensionOrNull(index: Int): Float? {
    val dimension = getDimension(index, UNDEFINED_FLOAT)
    return if (dimension.withinTolerance(UNDEFINED_FLOAT)) null else dimension
}

internal fun TypedArray.getIntOrNull(index: Int): Int? {
    val int = getInt(index, UNDEFINED_INT)
    return if (int == UNDEFINED_INT) null else int
}

internal fun TypedArray.getResourceOrNull(index: Int): Int? {
    val resource = getResourceId(index, UNDEFINED_INT)
    return if (resource == UNDEFINED_INT) null else resource
}

internal fun getColorAttributeFromResource(context: Context, resourceId: Int, attribute: Int): Int? {
    var color: Int? = null
    val resourceArray = intArrayOf(attribute)
    context.withStyledAttributes(resourceId, resourceArray) {
        color = this.getColorOrNull(FIRST_INDEX)
    }
    return color
}
