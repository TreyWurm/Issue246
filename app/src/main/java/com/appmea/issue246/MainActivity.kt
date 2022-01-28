package com.appmea.issue246

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appmea.issue246.databinding.ActivityMainBinding
import com.appmea.issue246.databinding.ItemEventDescriptionBinding
import com.appmea.roundedlayouts.layouts.RoundedConstraintLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader
    lateinit var binding: ActivityMainBinding

    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppConfig.onConfigChanged(this, null)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = Adapter(imageLoader)

        binding.rvItems.also {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            adapter.items.addAll(
                mutableListOf(
                    "https://cities-prod.s3.eu-central-1.amazonaws.com/fe61c0ba-ce65-4872-a8f8-6fea7960878a.png",
                    "https://cities-dev.s3.eu-central-1.amazonaws.com/2fc066d6-b939-4131-947a-98189ea806db.png",
                )
            )
        }, 3)
    }
}

class ContentCreator(val imageLoader: ImageLoader, val context: Context, val clContentContainer: RoundedConstraintLayout) {

    private var lastAddedId = 0

    private fun createImageContent(imageUrl: String): AppCompatImageView {
        val layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.constrainedHeight = true
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
//        if (content.getImageAttribute() != null) {
//            layoutParams.width = Utils.convertDpToPx(content.getImageAttribute().getWidth(), this)
//            layoutParams.height = Utils.convertDpToPx(content.getImageAttribute().getHeight(), this)
//            layoutParams.horizontalBias = 0f
//        }
//        layoutParams.topMargin = padding.get(0)
//        layoutParams.leftMargin = padding.get(1)
//        layoutParams.bottomMargin = padding.get(2)
//        layoutParams.rightMargin = padding.get(3)
        if (lastAddedId == 0) {
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        } else {
            layoutParams.topToBottom = lastAddedId
        }
        val newId = ViewCompat.generateViewId()
        val imageView = AppCompatImageView(context)
        imageView.layoutParams = layoutParams
        imageView.id = newId
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        imageLoader.loadUrl(imageView, imageUrl, ImageLoader.Options())
        return imageView
    }

    fun buildContent(s: String) {
        val view = createImageContent(s)
        clContentContainer.addView(view)
    }
}

class Adapter(val imageLoader: ImageLoader) : RecyclerView.Adapter<Adapter.VH>() {

    val items = mutableListOf<String>()

    class VH(val imageLoader: ImageLoader, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemEventDescriptionBinding = ItemEventDescriptionBinding.bind(itemView)

        fun update(s: String) {
            val contentCreator = ContentCreator(imageLoader, itemView.context, binding.clContentContainer)
            contentCreator.buildContent(s)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_event_description, parent, false)
        return VH(imageLoader, layout)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.update(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}