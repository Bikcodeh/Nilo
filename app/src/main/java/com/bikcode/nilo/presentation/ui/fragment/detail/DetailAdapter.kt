package com.bikcode.nilo.presentation.ui.fragment.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bikcode.nilo.R
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.StorageReference

class DetailAdapter(private val imgList: MutableList<StorageReference>, private val context: Context): PagerAdapter() {

    override fun getCount(): Int = imgList.count()

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imgProduct = ImageView(context)

        GlideApp.with(context)
            .load(imgList[position])
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(R.drawable.ic_access_time)
            .error(R.drawable.ic_broken_image)
            .into(imgProduct)

        container.addView(imgProduct)
        return imgProduct
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ImageView)
    }
}