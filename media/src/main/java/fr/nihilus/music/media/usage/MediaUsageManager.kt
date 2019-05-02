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

package fr.nihilus.music.media.usage

import fr.nihilus.music.media.di.ServiceScoped
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

/**
 * Manages reading and writing media usage statistics.
 */
internal interface MediaUsageManager {

    /**
     * Retrieve the most rated tracks from the music library.
     * Tracks are sorted by descending score.
     */
    fun getMostRatedTracks(): Single<List<TrackScore>>

    /**
     * Report that the track with the given [trackId] has been played until the end.
     *
     * @param trackId The unique identifier of the track that has been played.
     */
    fun reportCompletion(trackId: String)
}

/**
 * The size of the podium for the most rated tracks.
 */
private const val TOP_MOST_RATED = 25

/**
 * Implementation of [MediaUsageManager] that performs operations using Kotlin Coroutines,
 * bridging to the RxJava World.
 *
 * @param scope The scope coroutines should be executed into.
 * @param usageDao The DAO that controls storage of usage statistics.
 */
@ServiceScoped
internal class RxBridgeUsageManager
@Inject constructor(
    private val scope: CoroutineScope,
    private val usageDao: MediaUsageDao
) : MediaUsageManager {

    override fun getMostRatedTracks(): Single<List<TrackScore>> = scope.rxSingle(Dispatchers.IO) {
        usageDao.getMostRatedTracks(TOP_MOST_RATED)
    }

    override fun reportCompletion(trackId: String) {
        scope.launch(Dispatchers.IO) {
            val singleEventList = listOf(MediaUsageEvent(trackId.toLong()))
            usageDao.recordUsageEvents(singleEventList)
        }
    }
}