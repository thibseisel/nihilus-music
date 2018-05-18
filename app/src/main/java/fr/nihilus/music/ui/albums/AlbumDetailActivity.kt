/*
 * Copyright 2018 Thibault Seisel
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

package fr.nihilus.music.ui.albums

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import dagger.android.AndroidInjection
import fr.nihilus.music.R
import fr.nihilus.music.client.BrowserViewModel
import fr.nihilus.music.client.ViewModelFactory
import fr.nihilus.music.glide.GlideApp
import fr.nihilus.music.ui.BaseAdapter
import fr.nihilus.music.utils.luminance
import fr.nihilus.music.utils.setLightStatusBar
import fr.nihilus.music.view.CurrentlyPlayingDecoration
import kotlinx.android.synthetic.main.activity_album_detail.*
import javax.inject.Inject

class AlbumDetailActivity : AppCompatActivity(),
    View.OnClickListener,
    BaseAdapter.OnItemSelectedListener {

    @Inject lateinit var factory: ViewModelFactory
    @Inject lateinit var defaultAlbumPalette: AlbumPalette

    private lateinit var adapter: TrackAdapter
    private lateinit var pickedAlbum: MediaItem
    private lateinit var decoration: CurrentlyPlayingDecoration
    private lateinit var viewModel: BrowserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)

        pickedAlbum = checkNotNull(intent.getParcelableExtra(ARG_PICKED_ALBUM)) {
            "Calling activity must specify the album to display."
        }

        with(pickedAlbum.description) {
            titleView.text = title
            subtitleView.text = subtitle
        }

        playFab.setOnClickListener(this)

        setupToolbar()
        setupAlbumArt()
        setupTrackList()

        val palette: AlbumPalette? = intent.getParcelableExtra(ARG_PALETTE)
        applyPaletteTheme(palette ?: defaultAlbumPalette)

        viewModel = ViewModelProviders.of(this, factory).get(BrowserViewModel::class.java)
        viewModel.connect()

        // Change the decorated item when metadata changes
        viewModel.currentMetadata.observe(this, Observer(this::decoratePlayingTrack))

        // Subscribe to children of this album
        viewModel.subscribeTo(pickedAlbum.mediaId!!).observe(this, Observer { children ->
            adapter.submitList(children.orEmpty())
            recycler.swapAdapter(adapter, false)

            val currentMetadata = viewModel.currentMetadata.value
            decoratePlayingTrack(currentMetadata)
        })
    }

    private fun setupAlbumArt() {
        val albumArtView: ImageView = findViewById(R.id.albumArtView)
        albumArtView.transitionName = ALBUM_ART_TRANSITION_NAME

        GlideApp.with(this).asBitmap()
            .load(pickedAlbum.description.iconUri)
            .fallback(R.drawable.ic_album_24dp)
            .centerCrop()
            .into(albumArtView)
    }

    private fun setupTrackList() {
        recycler.let {
            it.layoutManager = LinearLayoutManager(this)
            adapter = TrackAdapter(this)
            it.adapter = adapter
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)

        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            title = null
        }
    }

    /**
     * Apply colors picked from the album art to the user interface.
     */
    private fun applyPaletteTheme(palette: AlbumPalette) {
        findViewById<View>(R.id.albumInfoLayout).setBackgroundColor(palette.primary)
        titleView.setTextColor(palette.titleText)
        subtitleView.setTextColor(palette.bodyText)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setLightStatusBar(window, palette.bodyText.luminance < 0.5f)
        }

        collapsingToolbar.setStatusBarScrimColor(palette.primaryDark)
        collapsingToolbar.setContentScrimColor(palette.primary)

        playFab.backgroundTintList = ColorStateList.valueOf(palette.accent)
        decoration = CurrentlyPlayingDecoration(this, palette.accent)
        recycler.addItemDecoration(decoration)
    }

    override fun onClick(view: View) {
        if (R.id.playFab == view.id) {
            playMediaItem(pickedAlbum)
        }
    }

    override fun onItemSelected(position: Int, actionId: Int) {
        val selectedTrack = adapter.getItem(position)
        playMediaItem(selectedTrack)
    }

    private fun playMediaItem(item: MediaItem) {
        viewModel.post {
            it.transportControls.playFromMediaId(item.mediaId, null)
        }
    }

    /**
     * Adds an [CurrentlyPlayingDecoration] to a track of this album if it is currently playing.
     *
     * @param playingTrack the currently playing track.
     */
    private fun decoratePlayingTrack(playingTrack: MediaMetadataCompat?) {
        if (playingTrack != null) {
            val position = adapter.indexOf(playingTrack)

            if (position != -1) {
                decoration.decoratedPosition = position
                recycler.invalidateItemDecorations()
            }
        }
    }

    companion object {
        const val ALBUM_ART_TRANSITION_NAME = "albumArt"
        const val ARG_PALETTE = "palette"
        const val ARG_PICKED_ALBUM = "pickedAlbum"
    }
}
