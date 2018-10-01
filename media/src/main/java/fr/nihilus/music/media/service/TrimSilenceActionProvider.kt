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

package fr.nihilus.music.media.service

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import fr.nihilus.music.media.R
import javax.inject.Inject

/**
 * Exposes the "silence trimming" feature as a Media Session custom action.
 */
class TrimSilenceActionProvider
@Inject constructor(
    context: Context,
    private val player: ExoPlayer
) : MediaSessionConnector.CustomActionProvider {

    private val actionDescription = context.getString(R.string.abc_pref_title_skip_silence)

    override fun getCustomAction(): PlaybackStateCompat.CustomAction =
        PlaybackStateCompat.CustomAction.Builder(
            ACTION_SKIP_SILENCE,
            actionDescription,
            R.drawable.abc_ic_hearing_24dp
        ).build()

    override fun onCustomAction(action: String?, extras: Bundle?) {
        setEnabled(extras?.getBoolean(EXTRA_ENABLED) == true)
    }

    fun setEnabled(shouldEnable: Boolean) {
        player.playbackParameters =
                if (shouldEnable) SKIP_SILENCE_PARAMETERS
                else PlaybackParameters.DEFAULT
    }

    companion object {
        const val ACTION_SKIP_SILENCE = "fr.nihilus.music.action.SKIP_SILENCE"
        const val EXTRA_ENABLED = "fr.nihilus.music.extras.SKIP_SILENCE_ENABLED"

        private val SKIP_SILENCE_PARAMETERS = PlaybackParameters(1f, 1f, true)
    }
}