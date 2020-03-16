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

package fr.nihilus.music.service

import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.collection.LruCache
import fr.nihilus.music.core.media.MediaId
import fr.nihilus.music.service.browser.BrowserTree
import fr.nihilus.music.service.browser.PaginationOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * Implementation of [SubscriptionManager] that caches at most [MAX_ACTIVE_SUBSCRIPTIONS].
 *
 * @param serviceScope The lifecycle of the service that owns this manager.
 * @param tree The source of media metadata.
 */
@OptIn(FlowPreview::class)
@ServiceScoped
internal class SubscriptionManagerImpl @Inject constructor(
    serviceScope: CoroutineScope,
    private val tree: BrowserTree
) : SubscriptionManager {

    private val scope = serviceScope + SupervisorJob(serviceScope.coroutineContext[Job])

    private val mutex = Mutex()
    private val cachedSubscriptions = LruSubscriptionCache()
    private val activeSubscriptions = BroadcastChannel<MediaId>(Channel.BUFFERED)

    override val updatedParentIds: Flow<MediaId> = activeSubscriptions.asFlow()

    override suspend fun loadChildren(
        parentId: MediaId,
        options: PaginationOptions?
    ): List<MediaItem> {
        val subscription = mutex.withLock {
            cachedSubscriptions.get(parentId) ?: createSubscription(parentId)
        }

        val children = subscription.consume { receive() }
        return if (options == null) children else {
            val fromIndex = options.page * options.size
            val toIndexExclusive = (fromIndex + options.size)

            return if (fromIndex < children.size && toIndexExclusive <= children.size) {
                children.subList(fromIndex, toIndexExclusive)
            } else emptyList()
        }
    }

    private fun createSubscription(parentId: MediaId): BroadcastChannel<List<MediaItem>> {
        return tree.getChildren(parentId)
            .buffer(Channel.CONFLATED)
            .broadcastIn(scope)
            .also { subscription ->
                cachedSubscriptions.put(parentId, subscription)

                subscription.asFlow()
                    .drop(1)
                    .map { parentId }
                    .catch { if (it !is NoSuchElementException) throw it }
                    .onEach { activeSubscriptions.send(it) }
                    .launchIn(scope)
            }
    }

    override suspend fun getItem(itemId: MediaId): MediaItem? {
        val parentId = itemId.copy(track = null)
        val parentSubscription = mutex.withLock { cachedSubscriptions[parentId] }

        return if (parentSubscription != null) {
            val children = parentSubscription.consume { receive() }
            children.find { it.mediaId == itemId.encoded }
        } else {
            tree.getItem(itemId)
        }
    }

    private class LruSubscriptionCache :
        LruCache<MediaId, BroadcastChannel<List<MediaItem>>>(MAX_ACTIVE_SUBSCRIPTIONS) {

        override fun entryRemoved(
            evicted: Boolean,
            key: MediaId,
            oldValue: BroadcastChannel<List<MediaItem>>,
            newValue: BroadcastChannel<List<MediaItem>>?
        ) {
            if (evicted) {
                oldValue.cancel()
            }
        }
    }
}