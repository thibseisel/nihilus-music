/*
 * Copyright 2019 Thibault Seisel
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

package fr.nihilus.music.library.playlists

import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import fr.nihilus.music.R
import fr.nihilus.music.base.BaseDialogFragment
import fr.nihilus.music.extensions.inflate
import fr.nihilus.music.extensions.observeK
import fr.nihilus.music.glide.GlideApp
import fr.nihilus.music.glide.GlideRequest
import fr.nihilus.music.ui.ListAdapter
import fr.nihilus.music.ui.LoadRequest

/**
 * A fragment displaying an Alert Dialog prompting the user to choose to which playlists
 * he wants to add a set of tracks.
 * He may also trigger another dialog allowing him to create a new playlist from the given tracks.
 */
class AddToPlaylistDialog : BaseDialogFragment() {

    private lateinit var playlistAdapter: TargetPlaylistsAdapter

    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, viewModelFactory).get(AddToPlaylistViewModel::class.java)
    }

    companion object Factory {
        private const val ARG_SELECTED_TRACKS = "selected_tracks_ids"

        /**
         * The tag associated with this dialog.
         * This may be used to identify the dialog in the fragment manager.
         */
        const val TAG = "AddToPlaylistDialog"

        /**
         * The number of tracks that have been added to the playlist
         * as a result of calling this dialog.
         * This is passed as an extra in the caller's [Fragment.onActivityResult].
         *
         * Type: `Int`
         */
        const val RESULT_TRACK_COUNT = "track_count"

        /**
         * The title of the playlist the tracks have been added to
         * as a result of calling this dialog.
         * This is passed as an extra in the caller's [Fragment.onActivityResult].
         *
         * Type: `String`
         */
        const val RESULT_PLAYLIST_TITLE = "playlist_title"

        fun newInstance(caller: Fragment, requestCode: Int, selectedTracksIds: List<MediaItem>) =
            AddToPlaylistDialog().apply {
                setTargetFragment(caller, requestCode)
                arguments = Bundle(1).apply {
                    putParcelableArray(ARG_SELECTED_TRACKS, selectedTracksIds.toTypedArray())
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        playlistAdapter =
                TargetPlaylistsAdapter(this)
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_to_playlist)
            .setAdapter(playlistAdapter, dialogEventHandler)
            .setPositiveButton(R.string.action_create_playlist, dialogEventHandler)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.targetPlaylists.observeK(this) {
            if (it is LoadRequest.Success) {
                playlistAdapter.submitList(it.data)
            }
        }

        viewModel.playlistUpdateResult.observeK(this) { resultEvent ->
            resultEvent?.handle { result ->
                when (result) {
                    is PlaylistEditionResult.Success -> targetFragment?.onActivityResult(
                        targetRequestCode,
                        Activity.RESULT_OK,
                        Intent().apply {
                            putExtra(RESULT_TRACK_COUNT, result.trackCount)
                            putExtra(RESULT_PLAYLIST_TITLE, result.playlistTitle)
                        }
                    )

                    is PlaylistEditionResult.NonExistingPlaylist -> targetFragment?.onActivityResult(
                        targetRequestCode,
                        Activity.RESULT_CANCELED,
                        null
                    )
                }
            }
        }
    }

    private val dialogEventHandler = DialogInterface.OnClickListener { _, position ->
        if (position >= 0) {
            // The clicked element is a playlist
            val playlist = playlistAdapter.getItem(position)
            val trackIds = arguments?.getParcelableArray(ARG_SELECTED_TRACKS) as? Array<MediaItem>
                    ?: emptyArray()
            viewModel.addTracksToPlaylist(playlist, trackIds)

        } else if (position == DialogInterface.BUTTON_POSITIVE) {
            // The "New playlist" action has been selected
            callNewPlaylistDialog()
        }
    }

    private fun callNewPlaylistDialog() {
        val memberTracks = arguments?.getParcelableArray(ARG_SELECTED_TRACKS)
                as? Array<MediaItem> ?: emptyArray()
        NewPlaylistDialog.newInstance(
            targetFragment!!,
            targetRequestCode,
            memberTracks
        )
            .show(fragmentManager, NewPlaylistDialog.TAG)
    }

    override fun onCancel(dialog: DialogInterface?) {
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
    }

    /**
     * The adapter used to display available playlists in the dialog's body.
     */
    private class TargetPlaylistsAdapter(fragment: Fragment) : ListAdapter<MediaItem, PlaylistHolder>() {
        private val glideRequest = GlideApp.with(fragment).asBitmap().circleCrop()

        override fun onCreateViewHolder(container: ViewGroup): PlaylistHolder =
            PlaylistHolder(
                container.inflate(
                    R.layout.playlist_list_item
                )
            )

        override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
            holder.bind(getItem(position), glideRequest)
        }
    }

    /**
     * Holds references to views representing a playlist item.
     */
    private class PlaylistHolder(itemView: View) : ListAdapter.ViewHolder(itemView) {
        private val icon = itemView.findViewById<ImageView>(R.id.icon_view)
        private val title = itemView.findViewById<TextView>(R.id.title_view)

        fun bind(playlist: MediaItem, glide: GlideRequest<*>) {
            title.text = playlist.description.title
            glide.load(playlist.description.iconUri).into(icon)
        }
    }
}