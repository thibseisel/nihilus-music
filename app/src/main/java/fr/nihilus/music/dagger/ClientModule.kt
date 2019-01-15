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

package fr.nihilus.music.dagger

import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import fr.nihilus.music.client.DaggerViewModelFactory
import fr.nihilus.music.glide.palette.AlbumColorModule
import fr.nihilus.music.library.MusicLibraryModule
import fr.nihilus.music.settings.SettingsModule

/**
 * Configure dependencies for the client-side GUI application.
 * This groups all dependencies required to display and interaction with the media browser service.
 */
@Module(includes = [
    MusicLibraryModule::class,
    SettingsModule::class,
    FileViewerModule::class,
    AlbumColorModule::class
])
internal abstract class ClientModule {
    @Binds
    internal abstract fun bindsVmFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory
}