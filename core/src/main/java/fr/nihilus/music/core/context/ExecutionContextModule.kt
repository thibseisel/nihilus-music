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

package fr.nihilus.music.core.context

import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Provides execution contexts for RxJava and Kotlin Coroutines.
 * The provided contexts schedule program execution on multiple threads.
 */
@Module
internal object ExecutionContextModule {

    @Provides @Singleton
    fun providesRxSchedulers() = RxSchedulers(
        AndroidSchedulers.mainThread(),
        Schedulers.computation(),
        Schedulers.io(),
        Schedulers.single()
    )

    @Provides @Singleton
    fun providesCoroutineDispatchers() = AppDispatchers(
        Dispatchers.Main,
        Dispatchers.Default,
        Dispatchers.IO
    )

    @Provides @Singleton
    fun providesAppCoroutineScope(
        dispatchers: AppDispatchers
    ): CoroutineScope = CoroutineScope(dispatchers.Main)
}