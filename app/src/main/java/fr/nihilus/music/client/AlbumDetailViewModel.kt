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

package fr.nihilus.music.client

import android.arch.lifecycle.LiveData
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import javax.inject.Inject

class AlbumDetailViewModel
@Inject constructor(
    connection: MediaBrowserConnection
) : MediaListViewModel(connection) {

    /**
     * The track that the player is currently playing, if any.
     * TODO: would be better to share the position of the track that is currently playing
     */
    val nowPlaying: LiveData<MediaMetadataCompat?> get() = connection.nowPlaying

    fun play(item: MediaBrowserCompat.MediaItem) = connection.post {
        it.transportControls.playFromMediaId(item.mediaId, null)
    }
}