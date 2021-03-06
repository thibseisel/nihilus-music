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

package fr.nihilus.music.spotify.service

/**
 * Encapsulation of the state of a resource available from the Spotify API.
 *
 * Loading a resource from the web with HTTP may fail for various reasons ;
 * this class is meant to limit the number of available states to a manageable amount.
 *
 * @param T The type of the resource to be loaded.
 */
internal sealed class HttpResource<out T : Any> {

    /**
     * The request to load the resource has succeeded and that resource is now available.
     * @param data The loaded resource.
     */
    data class Loaded<T : Any>(val data: T) : HttpResource<T>()

    /**
     * The request to load the resource failed because the requested resource does not exists,
     * or is no longer available.
     */
    object NotFound : HttpResource<Nothing>()

    /**
     * The request to load the resource failed for some reasons and the resource has not been loaded.
     * Depending on the returned [HTTP status code][status] it may be safe to retry the request.
     *
     * @param status The HTTP status associated with the response.
     * @param message An optional description of the error provided by the API response.
     */
    data class Failed(val status: Int, val message: String?) : HttpResource<Nothing>()
}