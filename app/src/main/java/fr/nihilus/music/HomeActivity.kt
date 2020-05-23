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

package fr.nihilus.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import fr.nihilus.music.core.os.RuntimePermissions
import fr.nihilus.music.core.ui.ConfirmDialogFragment
import fr.nihilus.music.core.ui.base.BaseActivity
import fr.nihilus.music.core.ui.extensions.darkSystemIcons
import fr.nihilus.music.core.ui.extensions.doOnApplyWindowInsets
import fr.nihilus.music.core.ui.extensions.resolveThemeColor
import fr.nihilus.music.glide.GlideApp
import fr.nihilus.music.library.MusicLibraryViewModel
import fr.nihilus.music.library.nowplaying.NowPlayingViewModel
import fr.nihilus.music.library.nowplaying.PlayerState
import fr.nihilus.music.ui.EXTERNAL_STORAGE_REQUEST
import fr.nihilus.music.ui.requestExternalStoragePermission
import kotlinx.android.synthetic.main.player_collapsed.*
import timber.log.Timber
import javax.inject.Inject

class HomeActivity : BaseActivity() {

    @Inject lateinit var permissions: RuntimePermissions

    private val viewModel: MusicLibraryViewModel by viewModels { viewModelFactory }
    private val playerViewModel: NowPlayingViewModel by viewModels { viewModelFactory }

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    private val statusBarNavListener = object : NavController.OnDestinationChangedListener {
        private val statusBarColor by lazy(LazyThreadSafetyMode.NONE) {
            resolveThemeColor(this@HomeActivity, R.attr.colorPrimaryDark)
        }

        override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
            if (destination.id != R.id.fragment_album_detail) {
                window.statusBarColor = statusBarColor
                window.darkSystemIcons = false
            }
        }
    }

    private val autoHideCollapsedPlayer = NavController.OnDestinationChangedListener { _, destination, _ ->
        if (destination.id == R.id.fragment_now_playing) {
            now_playing_card.isVisible = false
        } else {
            now_playing_card.isVisible = viewModel.playerVisible.value == true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupPlayerView()

        if (savedInstanceState == null) {
            if (permissions.canWriteToExternalStorage) {
                // Load a fragment depending on the intent that launched that activity (shortcuts)
                handleIntent(intent)
            } else this.requestExternalStoragePermission()
        }
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(statusBarNavListener)
        navController.addOnDestinationChangedListener(autoHideCollapsedPlayer)
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(autoHideCollapsedPlayer)
        navController.removeOnDestinationChangedListener(statusBarNavListener)
        super.onPause()
    }

    private fun setupPlayerView() {
        now_playing_card.doOnApplyWindowInsets { view, insets, _, margin ->
            val lp = view.layoutParams as ViewGroup.MarginLayoutParams
            lp.bottomMargin = insets.tappableElementInsets.bottom + margin.bottom
        }

        now_playing_card.setOnClickListener {
            navController.navigate(MainGraphDirections.expandPlayer())
        }

        now_playing_toggle.setOnClickListener {
            playerViewModel.togglePlayPause()
        }

        playerViewModel.state.observe(this) {
            now_playing_toggle.isEnabled = PlayerState.Action.TOGGLE_PLAY_PAUSE in it.availableActions
            now_playing_toggle.isPlaying = it.isPlaying

            now_playing_progress.progress = it.position.toInt()

            if (it.currentTrack != null) {
                now_playing_progress.max = it.currentTrack.duration.toInt()
                now_playing_title.text = it.currentTrack.title
                GlideApp.with(this)
                    .load(it.currentTrack.artworkUri)
                    .error(R.drawable.ic_audiotrack_24dp)
                    .into(now_playing_artwork)
            } else {
                now_playing_progress.max = 0
                now_playing_title.text = null
                now_playing_artwork.setImageDrawable(null)
            }
        }

        // Show / hide BottomSheet on startup without an animation
        viewModel.playerVisible.observe(this, this::onPlayerVisibilityChanged)

        viewModel.playerError.observe(this) { playerErrorEvent ->
            playerErrorEvent.handle { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Called when receiving an intent while the Activity is alive.
     * This is intended to handle actions relative to launcher shortcuts (API25+).
     *
     * @param intent the new intent that was started for the activity
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == EXTERNAL_STORAGE_REQUEST) {

            // Whether it has permission or not, load fragment into interface
            handleIntent(intent)

            // Show an informative dialog message if permission is not granted
            // and user has not checked "Don't ask again".
            if (grantResults[0] == PackageManager.PERMISSION_DENIED &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )) {
                ConfirmDialogFragment.newInstance(
                    null, 0,
                    message = getString(R.string.external_storage_permission_rationale),
                    positiveButton = R.string.core_ok
                ).show(supportFragmentManager, null)
            }
        }
    }

    private fun onPlayerVisibilityChanged(playerVisible: Boolean) {
        now_playing_card.isVisible = playerVisible
    }

    /**
     * Perform an action depending on the received intent.
     * This is intended to handle actions relative to launcher shortcuts (API 25+).
     *
     * @param intent the intent that started this activity, or was received later
     */
    private fun handleIntent(intent: Intent?) {
        Timber.d("Received intent: %s", intent)
    }
}
