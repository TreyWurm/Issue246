package com.appmea.issue246

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import javax.inject.Inject


class ImageLoader @Inject constructor(
    private val context: Context,
) {

    val defaultFallback: Drawable

    init {
        val fallbackImage = AppCompatResources.getDrawable(context, R.drawable.ic_launcher_background)?.mutate()
        fallbackImage?.colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.DST)
        defaultFallback = LayerDrawable(arrayOf(ColorDrawable(Color.GRAY), fallbackImage))
    }


    // ====================================================================================================================================================================================
    // <editor-fold desc="Options">

    class Options {
        companion object {
            const val DEAFULT_CORNER_RADIUS_DP = 0
            const val DEAFULT_WITH_LOADING_ANIMATION = false
            const val DEAFULT_BORDER_WIDTH_DP = 0f
            const val DEAFULT_BORDER_COLOR = 0xababab
            const val DEAFULT_CIRCLE = false
        }

        var cornerRadiusDp: Int = DEAFULT_CORNER_RADIUS_DP
        var withLoadingAnimation: Boolean = DEAFULT_WITH_LOADING_ANIMATION
        var progressDrawable: CircularProgressDrawable? = null

        /**
         * If null the default fallback will be used based on aspect ratio
         */
        var fallbackRes: Int? = null
        var listener: RequestListener<Drawable>? = null
        var bitmapTransformation: BitmapTransformation? = CenterCrop()
        var borderWidthDp: Float = DEAFULT_BORDER_WIDTH_DP
        var borderColor: Int = DEAFULT_BORDER_COLOR
        var circle: Boolean = DEAFULT_CIRCLE

        fun cornerRadiusDp(cornerRadius: Int) = apply { this.cornerRadiusDp = cornerRadius }
        fun withLoadingAnimation(withLoadingAnimation: Boolean) = apply { this.withLoadingAnimation = withLoadingAnimation }
        fun progressDrawable(progressDrawable: CircularProgressDrawable) = apply { this.progressDrawable = progressDrawable }
        fun fallbackRes(fallbackRes: Int) = apply { this.fallbackRes = fallbackRes }
        fun listener(listener: RequestListener<Drawable>) = apply { this.listener = listener }
        fun bitmapTransformation(bitmapTransformation: BitmapTransformation) = apply { this.bitmapTransformation = bitmapTransformation }
        fun borderWidthDp(borderWidthDp: Float) = apply { this.borderWidthDp = borderWidthDp }
        fun borderColor(borderColor: Int) = apply { this.borderColor = borderColor }
        fun circle(circle: Boolean) = apply { this.circle = circle }
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Whole image">

    /**
     * Loads the image at given url into target ImageView
     *
     * @param ivImage Target ImageView
     * @param url     Image url
     */
    fun loadUrl(ivImage: ImageView, url: String, options: Options = Options()) {
        if (!TextUtils.isEmpty(url)) {
            loadWholeInternal(ivImage, url, options)
        } else {
            loadFallbackInternal(ivImage, options)
        }
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Utility">

    private fun appendErrorFallback(options: Options, requestOptions: RequestOptions) {
        options.fallbackRes?.let {
            requestOptions.error(it)
        } ?: run {
            requestOptions.error(defaultFallback)
        }
    }

    fun createProgressPlaceholder(displayLoadingAnimation: Boolean): CircularProgressDrawable? {
        if (!displayLoadingAnimation) {
            return null
        }
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 1.dpAsPxFloat
        circularProgressDrawable.centerRadius = 1.dpAsPxFloat
        circularProgressDrawable.colorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    fun getWidth(view: View): Int {
        if (view.width != 0) {
            return view.width
        }
        if (view.measuredWidth != 0) {
            return view.measuredWidth
        }
        val layoutParams = view.layoutParams
        return if (layoutParams.width >= 0) {
            layoutParams.width
        } else 0
    }

    fun getHeight(view: View): Int {
        if (view.height != 0) {
            return view.height
        }
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        if (view.measuredHeight != 0) {
            return view.measuredHeight
        }
        val layoutParams = view.layoutParams
        return if (layoutParams.height >= 0) {
            layoutParams.height
        } else 0
    }

    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Internal">

    private fun loadWholeInternal(
        ivImage: ImageView,
        imageUrl: String,
        options: Options,
    ) {
        var requestOptions = RequestOptions().placeholder(getProgressDrawableFromOptions(options))
        appendErrorFallback(options, requestOptions)

        val transformations = mutableListOf<Transformation<Bitmap>>()
        transformations.add(RoundedBorderTransformation(options, ivImage.context))
        transformations.add(CenterInside())
        requestOptions = requestOptions.transform(MultiTransformation(transformations))

        Glide.with(ivImage)
            .load(imageUrl)
            .listener(options.listener)
            .apply(requestOptions)
            .into(ivImage)
    }


    private fun loadFallbackInternal(
        ivImage: ImageView,
        options: Options,
    ) {
        var requestOptions = RequestOptions().placeholder(getProgressDrawableFromOptions(options))
        val transformations = mutableListOf<Transformation<Bitmap>>()
        transformations.add(CenterInside())
        transformations.add(RoundedBorderTransformation(options, ivImage.context))
        requestOptions = requestOptions.transform(MultiTransformation(transformations))

        Glide.with(ivImage)
            .load(defaultFallback)
            .listener(options.listener)
            .apply(requestOptions)
            .into(ivImage)
    }

    private fun getProgressDrawableFromOptions(options: Options): CircularProgressDrawable? {
        return when {
            options.progressDrawable != null -> options.progressDrawable
            options.withLoadingAnimation -> createProgressPlaceholder(displayLoadingAnimation = true)
            else -> null
        }
    }
    // </editor-fold>
}