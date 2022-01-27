package com.appmea.issue246

import android.content.Context
import android.graphics.*
import android.os.Build
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.nio.charset.Charset
import java.security.MessageDigest


class RoundedBorderTransformation(
    private var radii: FloatArray = FloatArray(4) { 0F },
    private val strokeWidth: Float = 0F,
    private val strokeColor: Int = Color.parseColor("#ababab"),
    private val circle: Boolean = false,
) : BitmapTransformation() {

    private val paint = Paint().apply {
        color = this@RoundedBorderTransformation.strokeColor
        style = Paint.Style.STROKE
        strokeWidth = this@RoundedBorderTransformation.strokeWidth
        isAntiAlias = true
    }

    constructor(options: ImageLoader.Options, context: Context) : this(
        radii = FloatArray(4) { options.cornerRadiusDp.dpAsPxFloat },
        strokeWidth = options.borderWidthDp.dpAsPxFloat,
        strokeColor = options.borderColor,
        circle = options.circle,
    )

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        sanitizeRadii(toTransform.width.toFloat())
        scaleRadii(toTransform, outWidth, outHeight)

        if (circle) {
            radii = FloatArray(4) { toTransform.width / 2f }
        }

        return when {
            radii.any { it != 0f } && strokeWidth != 0f -> transformRoundedBorder(pool, toTransform)
            radii.any { it != 0f } -> transformRounded(pool, toTransform)
            strokeWidth != 0f -> transformBordered(toTransform)
            else -> toTransform
        }
    }

    private fun scaleRadii(toTransform: Bitmap, outWidth: Int, outHeight: Int) {
        val scale = toTransform.width / outWidth.toFloat()
        for (i in radii.indices) {
            radii[i] = radii[i] * scale
        }
    }

    /**
     * Rounded corners + border
     */
    private fun transformRoundedBorder(pool: BitmapPool, toTransform: Bitmap): Bitmap {
        val innerRadius = (radii[0] - strokeWidth).coerceAtLeast(0f)
        val roundedBitmap = TransformationUtils.roundedCorners(pool, toTransform, innerRadius, innerRadius, innerRadius, innerRadius);

        // Calculate the circular bitmap width with border
        val dstBitmapWidth: Int = (roundedBitmap.width + strokeWidth * 2).toInt()
        val dstBitmap = Bitmap.createBitmap(dstBitmapWidth, dstBitmapWidth, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(dstBitmap)
        canvas.drawBitmap(roundedBitmap, strokeWidth, strokeWidth, null)
        drawBorder(canvas)
        return dstBitmap
    }


    /**
     * Rounded corners only
     */
    private fun transformRounded(pool: BitmapPool, toTransform: Bitmap): Bitmap {
        return TransformationUtils.roundedCorners(pool, toTransform, radii[0], radii[1], radii[2], radii[3])
    }


    /**
     * Border only
     */
    private fun transformBordered(toTransform: Bitmap): Bitmap {
        val dstBitmapWidth: Int = (toTransform.width + strokeWidth * 2).toInt()
        val dstBitmap = Bitmap.createBitmap(dstBitmapWidth, dstBitmapWidth, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dstBitmap)
        canvas.drawBitmap(toTransform, strokeWidth, strokeWidth, null)
        drawBorder(canvas)
        return dstBitmap
    }


    /**
     * Sanitize radii
     */
    private fun sanitizeRadii(canvasWidth: Float) {
        radii.forEach { it.coerceAtLeast(0f).coerceAtMost(canvasWidth / 2f) }
    }


    private fun drawBorder(canvas: Canvas) {
        val innerRadius = (radii[0] - strokeWidth).coerceAtLeast(0f)

        // Draw the circular border around circular bitmap
        val path = Path().apply {
            addRoundRect(
                strokeWidth / 2f,
                strokeWidth / 2f,
                canvas.width.toFloat() - strokeWidth / 2f,
                canvas.height.toFloat() - strokeWidth / 2f,
                if (innerRadius == 0f) 0f else innerRadius + strokeWidth / 2f,
                if (innerRadius == 0f) 0f else innerRadius + strokeWidth / 2f,
                Path.Direction.CW
            )
        }

        canvas.drawPath(path, paint)
    }


    override fun equals(other: Any?): Boolean {
        return other is RoundedBorderTransformation
    }

    override fun hashCode(): Int {
        return (RoundedBorderTransformation::class.java.name + ".version." + BuildConfig.VERSION_CODE).hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private val ID_BYTES = Build.ID.toByteArray(Charset.forName("UTF-8"))
    }
}