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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import fr.nihilus.music.base.BaseViewModel
import fr.nihilus.music.client.MediaBrowserConnection
import fr.nihilus.music.media.command.DeletePlaylistCommand
import fr.nihilus.music.media.utils.MediaID
import fr.nihilus.music.ui.LoadRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class MembersViewModel
@Inject constructor(
    private val connection: MediaBrowserConnection
): BaseViewModel() {

    private val _playlistMembers = MutableLiveData<LoadRequest<List<MediaBrowserCompat.MediaItem>>>()
    val playlistMembers: LiveData<LoadRequest<List<MediaBrowserCompat.MediaItem>>>
        get() = _playlistMembers

    private var loadMembersJob: Job? = null

    fun loadTracksOfPlaylist(playlist: MediaBrowserCompat.MediaItem) {
        loadMembersJob?.cancel()
        _playlistMembers.postValue(LoadRequest.Pending())

        launch {
            connection.subscribe(playlist.mediaId!!).consumeEach { membersUpdate ->
                _playlistMembers.postValue(LoadRequest.Success(membersUpdate))
            }
        }
    }

    fun playTrack(track: MediaBrowserCompat.MediaItem) {
        launch {
            connection.playFromMediaId(track.mediaId!!)
        }
    }

    fun deletePlaylist(playlist: MediaBrowserCompat.MediaItem) {
        launch {
            val playlistId = MediaID.categoryValueOf(playlist.mediaId!!).toLong()
            val params = Bundle(1).apply {
                putLong(DeletePlaylistCommand.PARAM_PLAYLIST_ID, playlistId)
            }

            connection.sendCommand(DeletePlaylistCommand.CMD_NAME, params)
        }
    }
}
