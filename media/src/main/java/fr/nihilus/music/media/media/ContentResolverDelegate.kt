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

package fr.nihilus.music.media.media

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri

/**
 * A replacement for the Android platform's [ContentResolver].
 */
internal interface ContentResolverDelegate {

    /**
     * Query the given URI, returning a [Cursor] over the result set.
     *
     * For best performance, the caller should follow these guidelines:
     * - Provide an explicit projection,
     * to prevent reading data from storage that aren't going to be used.
     * - Use question mark parameter markers such as 'phone=?'
     * instead of explicit values in the [selection] parameter,
     * so that queries that differ only by those values will be recognized as the same
     * for caching purposes.
     *
     * @param uri The URI, using the _content://_ scheme, for the content to retrieve.
     * @param projection A list of which columns to return.
     * Passing `null` will return all columns, which is inefficient.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
     * (excluding the WHERE itself). Passing `null` will return all rows for the given URI.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     * from [selectionArgs], in the order that they appear in the selection.
     * The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause
     * (excluding the ORDER BY itself).
     * Passing `null` will use the default sort order, which may be unordered.
     * @return A Cursor object, which is positioned before the first entry, or `null`
     * @see Cursor
     */
    fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor?

    /**
     * Deletes row(s) specified by a content URI.
     *
     * If the content provider supports transactions, the deletion will be atomic.
     *
     * @param uri The URI of the row to delete.
     * @param where A filter to apply to rows before deleting,
     * formatted as an SQL WHERE clause (excluding the WHERE itself).
     * @return The number of rows deleted.
     */
    fun delete(uri: Uri, where: String?, whereArgs: Array<String>?): Int

    /**
     * Register an observer class that gets callbacks when data identified by a
     * given content URI changes.
     *
     * Starting in [android.os.Build.VERSION_CODES.O], all content notifications must be backed
     * by a valid [android.content.ContentProvider].
     *
     * @param uri The URI to watch for changes. This can be a specific row URI,
     * or a base URI for a whole class of content.
     * @param notifyForDescendants When `false`, the observer will be notified
     * whenever a change occurs to the exact URI specified by [uri]
     * or to one of the URI's ancestors in the path hierarchy.
     * When `true`, the observer will also be notified whenever a change occurs
     * to the URI's descendants in the path hierarchy.
     * @param observer The object that receives callbacks when changes occur.
     * @see unregisterContentObserver
     */
    fun registerContentObserver(uri: Uri, notifyForDescendants: Boolean, observer: ContentObserver)

    /**
     * Unregisters a change observer.
     *
     * @param observer The previously registered observer that is no longer needed.
     * @see registerContentObserver
     */
    fun unregisterContentObserver(observer: ContentObserver)
}

/**
 * A resolver that delegates calls to the platform's [ContentResolver].
 *
 * @param platformResolver The wrapped [ContentResolver] to which requests are delegated.
 */
internal class PlatformResolverDelegate(
    private val platformResolver: ContentResolver
) : ContentResolverDelegate {

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = platformResolver.query(uri, projection, selection, selectionArgs, sortOrder)

    override fun delete(uri: Uri, where: String?, whereArgs: Array<String>?): Int =
        platformResolver.delete(uri, where, whereArgs)

    override fun registerContentObserver(
        uri: Uri,
        notifyForDescendants: Boolean,
        observer: ContentObserver
    ) = platformResolver.registerContentObserver(uri, notifyForDescendants, observer)

    override fun unregisterContentObserver(observer: ContentObserver) =
        platformResolver.unregisterContentObserver(observer)

}