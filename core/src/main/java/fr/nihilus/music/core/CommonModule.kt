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

package fr.nihilus.music.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import fr.nihilus.music.core.os.*
import fr.nihilus.music.core.settings.SettingsModule
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [SettingsModule::class])
internal abstract class CommonModule {

    @Binds
    abstract fun bindsSystemPermissions(permissions: SystemRuntimePermissions): RuntimePermissions

    @Binds
    abstract fun bindsSystemClock(clock: DeviceClock): Clock

    @Binds
    abstract fun bindsAndroidFileSystem(fileSystem: AndroidFileSystem): FileSystem

    companion object {

        @Provides @Singleton
        fun providesSharedPreferences(context: Context): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

        @Provides @Named("internal")
        fun providesInternalStorageRoot(context: Context): File = context.filesDir
    }
}