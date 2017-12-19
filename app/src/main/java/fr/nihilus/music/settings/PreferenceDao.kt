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

package fr.nihilus.music.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatDelegate
import fr.nihilus.music.R
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_NIGHT_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
private const val KEY_SHUFFLE_MODE = "shuffle_mode"
private const val KEY_REPEAT_MODE = "repeat_mode"
private const val KEY_LAST_PLAYED = "last_played"
private const val KEY_STARTUP_SCREEN = "startup_screen"

/**
 * Centralizes access to properties saved to Android SharedPreferences.
 */
@Singleton
class PreferenceDao
@Inject constructor(app: Application, private val mPrefs: SharedPreferences) {

    private val mAppContext: Context = app

    /**
     * The current enabled/disabled state of the night mode setting.
     * This property value is always an `AppCompatDelegate.MODE_NIGHT_*` constant.
     */
    val nightMode: Int
        get() = Integer.parseInt(mPrefs.getString(mAppContext.getString(R.string.pref_night_mode),
                DEFAULT_NIGHT_MODE))

    /**
     * The last configured shuffle mode.
     * When shuffle mode is enabled, tracks in a playlist are read in random order.
     * This property value should be an `PlaybackStateCompat.SHUFFLE_MODE_*` constant.
     */
    var shuffleMode: Int
        get() = mPrefs.getInt(KEY_SHUFFLE_MODE, PlaybackStateCompat.SHUFFLE_MODE_NONE)
        set(shuffleMode) = mPrefs.edit().putInt(KEY_SHUFFLE_MODE, shuffleMode).apply()

    /**
     * The last configured repeat mode.
     * This property value should be an `PlaybackStateCompat.REPEAT_MODE_*` constant.
     */
    var repeatMode: Int
        get() = mPrefs.getInt(KEY_REPEAT_MODE, PlaybackStateCompat.REPEAT_MODE_NONE)
        set(repeatMode) = mPrefs.edit().putInt(KEY_REPEAT_MODE, repeatMode).apply()

    /**
     * Defines the screen to be shown to the user when the application starts.
     * Must be the media ID of the media item whose children should be shown on startup.
     */
    val startupScreenMediaId: String
        get() = mPrefs.getString(KEY_STARTUP_SCREEN,
                mAppContext.getString(R.string.pref_default_startup_screen))

    /**
     * The media ID of the last played item.
     * This will be null if no item has been played.
     */
    var lastPlayedMediaId: String?
        get() = mPrefs.getString(KEY_LAST_PLAYED, null)
        set(mediaId) = mPrefs.edit().putString(KEY_LAST_PLAYED, mediaId).apply()
}