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

package fr.nihilus.music.extensions

import android.support.annotation.LayoutRes
import android.support.v4.widget.DrawerLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Inflate the given layout as a child of this view group.
 *
 * @receiver the view parent in which inflate this layout
 * @param resource id for an XML resource to load
 * @param attach whether the inflated layout should be attached to this view group.
 * If false, the view group will be used to create the correct layout params.
 * @return the root view of the inflated hierarchy. If [attach] is `true`,
 * this will be this view group, otherwise the root of the inflated XML file.
 */
fun ViewGroup.inflate(@LayoutRes resource: Int, attach: Boolean = false): View =
    LayoutInflater.from(context).inflate(resource, this, attach)

/**
 * Whether a View is visible.
 * Setting this property to `false` will set the View's visibility to [View.GONE].
 */
var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

/**
 * Lock user interactions with the given [DrawerLayout].
 * @param isLocked Whether interactions with this drawer should be ignored.
 */
fun DrawerLayout.lockDrawer(isLocked: Boolean) {
    requestDisallowInterceptTouchEvent(isLocked)
    setDrawerLockMode(
        if (isLocked) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        else DrawerLayout.LOCK_MODE_UNLOCKED
    )
}