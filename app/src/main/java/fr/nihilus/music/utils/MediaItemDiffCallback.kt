package fr.nihilus.music.utils

import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v7.util.DiffUtil
import android.text.TextUtils

class MediaItemDiffCallback(
        private val mOld: List<MediaItem>, 
        private val mNew: List<MediaItem>
) : DiffUtil.Callback() {

    override fun getOldListSize() = mOld.size

    override fun getNewListSize() = mNew.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldId = mOld[oldItemPosition].mediaId!!
        val newId = mNew[newItemPosition].mediaId!!
        return oldId == newId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldDesc = mOld[oldItemPosition].description
        val newDesc = mNew[newItemPosition].description
        val sameTitle = TextUtils.equals(oldDesc.title, newDesc.title)
        val sameSubtitle = TextUtils.equals(oldDesc.subtitle, newDesc.subtitle)
        return sameTitle && sameSubtitle
    }
}
