package fr.nihilus.music.ui.playlist

import android.graphics.Bitmap
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import fr.nihilus.music.R
import fr.nihilus.music.glide.GlideApp
import fr.nihilus.music.glide.GlideRequest
import fr.nihilus.music.inflate
import fr.nihilus.music.utils.MediaItemDiffCallback

class MembersAdapter(fragment: Fragment) : RecyclerView.Adapter<MembersHolder>() {
    private val mItems: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()
    private val glideRequest = GlideApp.with(fragment).asBitmap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersHolder =
            MembersHolder(parent, glideRequest)

    override fun onBindViewHolder(holder: MembersHolder, position: Int) {
        holder.bind(mItems[position])
    }

    override fun getItemCount() = mItems.size

    fun update(newItems: List<MediaBrowserCompat.MediaItem>) {
        val diffCallback = MediaItemDiffCallback(mItems, newItems)
        val result = DiffUtil.calculateDiff(diffCallback)
        mItems.clear()
        mItems.addAll(newItems)
        result.dispatchUpdatesTo(this)
    }

    fun moveTo(from: Int, to: Int) {

    }
}

class MembersHolder(parent: ViewGroup, private val artLoader: GlideRequest<Bitmap>)
    : RecyclerView.ViewHolder(parent.inflate(R.layout.song_list_item)) {

    private val albumArt: ImageView = itemView.findViewById(R.id.albumArt)
    private val title: TextView = itemView.findViewById(R.id.title)
    private val subtitle: TextView = itemView.findViewById(R.id.subtitle)

    fun bind(item: MediaBrowserCompat.MediaItem) {
        val descr = item.description
        title.text = descr.title
        subtitle.text = descr.subtitle
        artLoader.load(descr.mediaUri).into(albumArt)
    }
}