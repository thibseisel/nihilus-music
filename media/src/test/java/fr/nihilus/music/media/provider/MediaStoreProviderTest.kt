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

package fr.nihilus.music.media.provider

import android.Manifest
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore.Audio.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.TruthJUnit.assume
import fr.nihilus.music.media.assertThrows
import fr.nihilus.music.media.fail
import fr.nihilus.music.media.failAssumption
import fr.nihilus.music.media.os.ContentResolverDelegate
import fr.nihilus.music.media.permissions.DeniedPermission
import fr.nihilus.music.media.permissions.GrantedPermission
import fr.nihilus.music.media.permissions.PermissionDeniedException
import fr.nihilus.music.media.provider.FailingMediaStore.query
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

private const val MUSIC_FOLDER_NAME = "Music"
private const val TEST_FILENAME = "1741_(The_Battle_of_Cartagena).mp3"

@RunWith(AndroidJUnit4::class)
class MediaStoreProviderTest {
    private lateinit var mediaStoreSurrogate: MediaStoreSurrogate

    @Before
    fun setUp() {
        mediaStoreSurrogate = MediaStoreSurrogate(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        mediaStoreSurrogate.release()
    }

    @Test
    fun givenDeniedPermission_whenQueryingTracks_thenFailWithPermissionDeniedException() {
        assertProviderThrowsWhenPermissionIsDenied(MediaProvider::queryTracks)
    }

    @Test
    fun givenDeniedPermission_whenQueryingAlbums_thenFailWithPermissionDeniedException() {
        assertProviderThrowsWhenPermissionIsDenied(MediaProvider::queryAlbums)
    }

    @Test
    fun givenDeniedPermission_whenQueryingArtists_thenFailWithPermissionDeniedException() {
        assertProviderThrowsWhenPermissionIsDenied(MediaProvider::queryArtists)
    }

    @Test
    fun whenQueryingTracks_thenThereShouldBeTheSameNumberOfTracks() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allTracks = provider.queryTracks()
        assertThat(allTracks).hasSize(10)
    }

    @Test
    fun whenQueryingTracks_thenTrackInfoShouldMatch() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allTracks = provider.queryTracks()
        val aTrack = allTracks.find { it.id == 161L } ?: failAssumption("Missing a track with id = 161")

        assertThat(aTrack.id).isEqualTo(161L)
        assertThat(aTrack.title).isEqualTo("1741 (The Battle of Cartagena)")
        assertThat(aTrack.albumId).isEqualTo(65L)
        assertThat(aTrack.album).isEqualTo("Sunset on the Golden Age")
        assertThat(aTrack.artistId).isEqualTo(26L)
        assertThat(aTrack.artist).isEqualTo("Alestorm")
        assertThat(aTrack.duration).isEqualTo(437603L)
        assertThat(aTrack.availabilityDate).isEqualTo(1000003673L)
    }

    @Test
    fun whenQueryingTracks_thenMediaUriShouldBeContentUriForTrack() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)

        val allTracks = provider.queryTracks()
        val aTrack = allTracks.find { it.id == 161L } ?: failAssumption("Missing a track with id = 161")

        assertThat(aTrack.mediaUri).isEqualTo("${Media.EXTERNAL_CONTENT_URI}/161")
    }

    @Test
    fun whenQueryingTracks_thenDiscNoAndTrackNoAreCalculated() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allTracks = provider.queryTracks()

        // Test with a track present on disc 1 : "Give It Up"
        val trackOnDiscOne = allTracks.find { it.id == 48L } ?: failAssumption("Missing a track on disc 1")
        assertThat(trackOnDiscOne.discNumber).isEqualTo(1)
        assertThat(trackOnDiscOne.trackNumber).isEqualTo(19)

        // Test with a track present on disc 2 : "Jailbreak"
        val trackOnDiscTwo = allTracks.find { it.id == 125L } ?: failAssumption("Missing a track on disc 2")
        assertThat(trackOnDiscTwo.discNumber).isEqualTo(2)
        assertThat(trackOnDiscTwo.trackNumber).isEqualTo(14)
    }

    @Test
    fun whenQueryingTracks_thenCopyAlbumArtPathFromCorrespondingAlbum() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allTracks = provider.queryTracks()

        assume().that(allTracks).isNotEmpty()

        val firstTrack = allTracks.first()
        assertThat(firstTrack.albumArtUri).isEqualTo("file:///storage/emulated/0/Android/data/com.android.providers.media/albumthumbs/1509626970548")
    }

    @Test
    fun whenQueryingTracks_tracksShouldBeSortedByAlphabeticOrderWithoutCommonPrefixes() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allTracks = provider.queryTracks()

        assume().that(allTracks.size).isAtLeast(3)
        val trackIds = allTracks.map { it.id }
        assertThat(trackIds).containsExactly(161L, 309L, 481L, 48L, 125L, 294L, 219L, 75L, 464L, 477L).inOrder()
    }

    @Test
    fun whenQueryingAlbums_thenThereShouldBeTheSameNumberOfAlbums() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allAlbums = provider.queryAlbums()

        assertThat(allAlbums).hasSize(8)
    }

    @Test
    fun whenQueryingAlbums_thenAlbumInfoShouldMatch() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)

        val allAlbums = provider.queryAlbums()
        val anAlbum = allAlbums.find { it.id == 40L } ?: fail("Missing an album with id = 40")

        assertThat(anAlbum.id).isEqualTo(40L)
        assertThat(anAlbum.title).isEqualTo("The 2nd Law")
        assertThat(anAlbum.artistId).isEqualTo(18L)
        assertThat(anAlbum.artist).isEqualTo("Muse")
        assertThat(anAlbum.releaseYear).isEqualTo(2012)
        assertThat(anAlbum.trackCount).isEqualTo(1)
        assertThat(anAlbum.albumArtUri).isEqualTo("file:///storage/emulated/0/Android/data/com.android.providers.media/albumthumbs/1509627051019")
    }

    @Test
    fun whenQueryingAlbums_thenAlbumsShouldBeSortedByAlphabeticOrderWithoutCommonPrefixes() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)

        val allAlbums = provider.queryAlbums()
        assume().that(allAlbums.size).isAtLeast(3)

        val albumIds = allAlbums.map { it.id }
        assertThat(albumIds).containsExactly(40L, 38L, 102L, 95L, 7L, 6L, 65L, 26L).inOrder()
    }

    @Test
    fun whenQueryingArtists_thenThereShouldBeTheSameNumberOfArtists() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)

        val allArtists = provider.queryArtists()
        assertThat(allArtists).hasSize(5)
    }

    @Test
    fun whenQueryingArtists_thenArtistInfoShouldMatch() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allArtists = provider.queryArtists()
        val anArtist = allArtists.find { it.id == 5L } ?: failAssumption("Missing an artist with id = 5")

        assertThat(anArtist.id).isEqualTo(5L)
        assertThat(anArtist.name).isEqualTo("AC/DC")
        assertThat(anArtist.albumCount).isEqualTo(1)
        assertThat(anArtist.trackCount).isEqualTo(2)
    }

    @Test
    fun whenQueryingArtists_thenItsIconShouldBeThatOfTheMostRecentAlbum() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allArtists = provider.queryArtists()

        // Alestorm only have one album here ; its icon should be that of that album
        val artistWithOneAlbum = allArtists.find { it.id == 26L } ?: failAssumption("Missing an artist with id = 26")
        assertThat(artistWithOneAlbum.iconUri).isEqualTo("file:///storage/emulated/0/Android/data/com.android.providers.media/albumthumbs/1509626970548")

        // Foo Fighters have 3 albums, use the icon of "Concrete and Gold"
        val artistWithMultipleAlbums = allArtists.find { it.id == 13L } ?: failAssumption("Missing an artist with id = 13")
        assertThat(artistWithMultipleAlbums.iconUri).isEqualTo("file:///storage/emulated/0/Android/data/com.android.providers.media/albumthumbs/1509627413029")
    }

    @Test
    fun whenQueryingArtists_thenArtistsShouldBeSortedByAlphabeticOrderWithoutCommonPrefixes() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val allArtists = provider.queryArtists()
        assume().that(allArtists.size).isAtLeast(3)

        val artistIds = allArtists.map { it.id }
        assertThat(artistIds).containsExactly(5L, 26L, 4L, 13L, 18L).inOrder()
    }

    @Test
    fun whenRegisteringTrackObserver_thenRegisterContentObserverForMediaUri() {
        assertRegisterObserver(MediaProvider.MediaType.TRACKS, Media.EXTERNAL_CONTENT_URI)
    }

    @Test
    fun whenRegisteringAlbumObserver_thenRegisterContentObserverForAlbumUri() {
        assertRegisterObserver(MediaProvider.MediaType.ALBUMS, Albums.EXTERNAL_CONTENT_URI)
    }

    @Test
    fun whenRegisteringArtistObserver_thenRegisterContentObserverForArtistUri() {
        assertRegisterObserver(MediaProvider.MediaType.ARTISTS, Artists.EXTERNAL_CONTENT_URI)
    }

    @Test
    fun givenRegisteredTrackObserver_whenUnregistering_thenUnregisterForMediaUri() {
        assertUnregisterObserver(MediaProvider.MediaType.TRACKS, Media.EXTERNAL_CONTENT_URI)
    }

    @Test
    fun givenRegisteredAlbumObserver_whenUnregistering_thenUnregisterForAlbumUri() {
        assertUnregisterObserver(MediaProvider.MediaType.ALBUMS, Albums.EXTERNAL_CONTENT_URI)
    }

    @Test
    fun givenRegisteredArtistObserver_whenUnregistering_thenUnregisterForArtistUri() {
        assertUnregisterObserver(MediaProvider.MediaType.ARTISTS, Artists.EXTERNAL_CONTENT_URI)
    }

    @Test
    fun givenFailingMediaStore_whenQueryingTracks_thenReturnAnEmptyTrackList() {
        assertQueryFailsGracefully(MediaProvider::queryTracks)
    }

    @Test
    fun givenFailingMediaStore_whenQueryingAlbums_thenReturnAnEmptyAlbumList() {
        assertQueryFailsGracefully(MediaProvider::queryAlbums)
    }

    @Test
    fun givenFailingMediaStore_whenQueryingArtists_thenReturnAnEmptyArtistList() {
        assertQueryFailsGracefully(MediaProvider::queryArtists)
    }

    @Test
    fun givenDeniedPermission_whenDeletingTracks_thenFailWithPermissionDeniedException() {
        val provider = MediaStoreProvider(mediaStoreSurrogate, DeniedPermission)
        val exception = assertThrows<PermissionDeniedException> {
            provider.deleteTracks(longArrayOf(161, 309))
        }

        assertThat(exception.permission).isEqualTo(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @Test
    fun whenDeletingTrack_thenDeleteTheCorrespondingFile() {
        val tracksDir = File(MUSIC_FOLDER_NAME)
        val trackFile = File(tracksDir, TEST_FILENAME)

        try {
            tracksDir.mkdir()
            trackFile.createNewFile()
            assume().that(trackFile.exists()).isTrue()

            val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
            provider.deleteTracks(longArrayOf(161L))

            assertThat(trackFile.exists()).isFalse()

        } finally {
            tracksDir.deleteRecursively()
        }
    }

    @Test
    fun whenTrackIsDeleted_thenDeleteItsMetadataFromDatabase() {
        val tracksDir = File(MUSIC_FOLDER_NAME)
        val trackFile = File(tracksDir, TEST_FILENAME)

        try {
            tracksDir.mkdir()
            trackFile.createNewFile()
            val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
            provider.deleteTracks(longArrayOf(161L))

            val existsInDatabase = mediaStoreSurrogate.exist(MediaProvider.MediaType.TRACKS, 161L)
            assertThat(existsInDatabase).isFalse()

        } finally {
            tracksDir.deleteRecursively()
        }
    }

    @Test
    fun whenTrackCannotBeDeleted_thenDoNotDeleteItsMetadata() {
        // Files doesn't exist, so deleting them will fail.

        val existsPriorDeleting = mediaStoreSurrogate.exist(MediaProvider.MediaType.TRACKS, 161L)
        assume().that(existsPriorDeleting).isTrue()

        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        provider.deleteTracks(longArrayOf(161L))

        val existsInDatabase = mediaStoreSurrogate.exist(MediaProvider.MediaType.TRACKS, 161L)
        assertThat(existsInDatabase).isTrue()
    }

    private fun assertQueryFailsGracefully(queryFun: MediaProvider.() -> List<Any>) {
        val provider = MediaStoreProvider(FailingMediaStore, GrantedPermission)

        assertThat(provider.queryFun()).isEmpty()
    }

    private fun assertRegisterObserver(observerType: MediaProvider.MediaType, expectedRegisteredUri: Uri) {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val observer = TestObserver(observerType)
        provider.registerObserver(observer)

        val observedUris = mediaStoreSurrogate.observers.map { it.uri }
        assertThat(observedUris).contains(expectedRegisteredUri)
    }

    private fun assertUnregisterObserver(observerType: MediaProvider.MediaType, registeredUri: Uri) {
        val provider = MediaStoreProvider(mediaStoreSurrogate, GrantedPermission)
        val observer = TestObserver(observerType)
        provider.registerObserver(observer)
        provider.unregisterObserver(observer)

        val observedUris = mediaStoreSurrogate.observers.map { it.uri }
        assertThat(observedUris).doesNotContain(registeredUri)
    }

    private fun assertProviderThrowsWhenPermissionIsDenied(queryFun: MediaProvider.() -> List<Any>) {
        val provider = MediaStoreProvider(mediaStoreSurrogate, DeniedPermission)

        val exception = assertThrows<PermissionDeniedException> {
            provider.queryFun()
        }

        assertThat(exception.permission).isEqualTo(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

private class TestObserver(mediaType: MediaProvider.MediaType) : MediaProvider.Observer(mediaType) {
    var changeCount: Int = 0

    override fun onChanged() {
        changeCount++
    }
}

/**
 * A test fixture for a media provider whose query always fail
 * (i.e. [query] always return a `null` cursor).
 */
private object FailingMediaStore : ContentResolverDelegate {

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun delete(uri: Uri, where: String?, whereArgs: Array<String>?): Int = 0

    override fun registerContentObserver(
        uri: Uri,
        notifyForDescendants: Boolean,
        observer: ContentObserver
    ) = Unit

    override fun unregisterContentObserver(observer: ContentObserver) = Unit
}

