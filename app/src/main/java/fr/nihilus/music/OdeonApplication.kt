/*
 * Copyright 2020 Thibault Seisel
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

import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import androidx.work.WorkerFactory
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import fr.nihilus.music.core.DaggerCoreComponent
import fr.nihilus.music.core.settings.Settings
import fr.nihilus.music.dagger.DaggerAppComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject

/**
 * An Android Application component that can inject dependencies into Activities and Services.
 * This class also performs general configuration tasks.
 */
class OdeonApplication : DaggerApplication(), Configuration.Provider {
    @Inject lateinit var settings: Settings
    @Inject lateinit var workerFactory: WorkerFactory

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // Print logs to Logcat
            Timber.plant(Timber.DebugTree())
            StrictMode.enableDefaults()
        }

        // Apply theme whenever it is changed via preferences.
        settings.currentTheme.onEach { theme ->
            AppCompatDelegate.setDefaultNightMode(theme.value)
        }.launchIn(GlobalScope + Dispatchers.Main)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val coreDependencies = DaggerCoreComponent.factory().create(this)
        return DaggerAppComponent.factory().create(this, coreDependencies)
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}
