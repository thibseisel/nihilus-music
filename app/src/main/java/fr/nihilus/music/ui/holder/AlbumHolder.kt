/*
 * Copyright 2017 Thibault Seisel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.nihilus.music.ui.holder

import android.support.annotation.ColorInt
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.CardView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.ImageViewTarget
import fr.nihilus.music.R
import fr.nihilus.music.glide.palette.PaletteBitmap
import fr.nihilus.music.ui.BaseAdapter

/**
 *
 */
internal class AlbumHolder(
    parent: ViewGroup,
    private val glide: RequestBuilder<PaletteBitmap>,
    private val defaultColors: IntArray
) : BaseAdapter.ViewHolder(parent, R.layout.album_grid_item) {

    private val card: CardView = itemView.findViewById(R.id.card)
    private val albumArt: ImageView = itemView.findViewById(R.id.albumArtView)
    private val title: TextView = itemView.findViewById(R.id.title)
    private val artist: TextView = itemView.findViewById(R.id.artist)

    private val albumViewTarget = object : ImageViewTarget<PaletteBitmap>(albumArt) {

        override fun setResource(resource: PaletteBitmap?) {
            if (resource != null) {
                super.view.setImageBitmap(resource.bitmap)
                val swatch = resource.palette.dominantSwatch
                val accentColor = resource.palette.getVibrantColor(defaultColors[1])
                if (swatch != null) {
                    setColors(
                        swatch.rgb, accentColor,
                        swatch.titleTextColor, swatch.bodyTextColor
                    )
                }
            }
        }
    }

    inline val transitionView get() = albumArt

    @ColorInt
    val colors = IntArray(4)

    private fun setColors(
        @ColorInt primary: Int, @ColorInt accent: Int, @ColorInt title: Int,
        @ColorInt body: Int
    ) {
        this.card.setCardBackgroundColor(primary)
        this.title.setTextColor(body)
        artist.setTextColor(body)
        colors[0] = primary
        colors[1] = accent
        colors[2] = title
        colors[3] = body
    }

    override fun onAttachListeners(client: BaseAdapter.OnItemSelectedListener) {
        itemView.setOnClickListener { _ ->
            client.onItemSelected(adapterPosition, R.id.action_browse_item)
        }
    }

    override fun onBind(item: MediaBrowserCompat.MediaItem) {
        val description = item.description
        title.text = description.title
        artist.text = description.subtitle

        setColors(defaultColors[0], defaultColors[1], defaultColors[2], defaultColors[3])

        glide.load(description.iconUri).into(albumViewTarget)
        ViewCompat.setTransitionName(albumArt, "image_" + description.mediaId)
    }
}