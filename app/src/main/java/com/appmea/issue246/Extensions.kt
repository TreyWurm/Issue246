package com.appmea.issue246

import android.content.res.ColorStateList
import android.view.View
import kotlin.math.floor

val Int.dpAsPx: Int get() = if (this == 0) 0 else floor(AppConfig.density * this.toDouble()).toInt()

val Int.pxAsDp: Int get() = if (this == 0) 0 else floor(this.toDouble() / AppConfig.density).toInt()

val Int.dpAsPxFloat: Float get() = if (this == 0) 0f else floor(AppConfig.density * this.toDouble()).toFloat()

val Float.dpAsPx: Int get() = if (this == 0f) 0 else floor(AppConfig.density * this.toDouble()).toInt()

val Float.pxAsDp: Int get() = if (this == 0f) 0 else floor(this.toDouble() / AppConfig.density).toInt()

val Float.dpAsPxFloat: Float get() = if (this == 0f) 0f else floor(AppConfig.density * this.toDouble()).toFloat()

val Float.pxAsDpFloat: Float get() = if (this == 0f) 0f else floor(this.toDouble() / AppConfig.density).toFloat()

val Int.spAsPx: Int get() = if (this == 0) 0 else floor(AppConfig.fontDensity * this.toDouble()).toInt()

val Float.spAsPx: Float get() = if (this == 0f) 0f else floor(AppConfig.fontDensity * this.toDouble()).toFloat()

val Float.spAsPxInt: Int get() = this.spAsPx.toInt()

fun View.visible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
    if (visible && alpha == 0f) {
        alpha = 1f
    }
}

inline fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

fun Int.toColorStateList(): ColorStateList {
    return ColorStateList.valueOf(this)
}