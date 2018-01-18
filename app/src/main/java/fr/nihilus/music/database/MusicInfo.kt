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

package fr.nihilus.music.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Additional information associated with a music track that can be used for classification.
 * Each instance is bound to a music track by its id.
 *
 * @constructor
 * Create a new set of information associated with a particular music track.
 * Note that this class performs no check on the passed music id.
 *
 * @param musicId the id of the music track associated with those information.
 */
@Entity(tableName = "music_info")
class MusicInfo(
    /**
     * Unique identifier of the track described by those information.
     */
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "music_id")
    val musicId: Long
) {

    /**
     * The number of times the associated track has been played until the end.
     */
    @ColumnInfo(name = "read_count")
    var readCount: Int = 0

    /**
     * The number of times the user skipped this track before it has been played for 5 seconds.
     */
    @ColumnInfo(name = "skip_count")
    var skipCount: Int = 0

    /**
     * An approximation of the mean tempo in beats per minute.
     * This may represent the speed of the rhythm of the associated track.
     */
    @ColumnInfo(name = "tempo")
    var tempo: Int = 0

    /**
     * An approximation of the mean sound energy.
     * This arbitrary number represents how loud the sound is.
     */
    @ColumnInfo(name = "mean_energy")
    var energy: Int = 0
}
