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

package fr.nihilus.music.service.actions

import android.os.Bundle
import fr.nihilus.music.core.media.CustomActions

/**
 * A service-side handler for executing media browser custom actions.
 * Each custom action has a unique name that should be a constant from `CustomActions.ACTION_*`,
 * and can have an arbitrary number of client-specified parameters.
 *
 * In case the action cannot be performed for some reason (missing or invalid parameter, etc.)
 * an [ActionFailure] exception should be thrown.
 *
 * When completing normally, actions may return a [Bundle] containing the result of executing the action
 * for use by the calling media browser client.
 */
interface BrowserAction {

    /**
     * The name that uniquely identified this custom action.
     * Media browser clients should use this name to execute this action.
     *
     * The name of a custom action must be constant.
     */
    val name: String

    /**
     * Execute the action with the specified parameters, returning an optional result [Bundle] on success
     * or throwing an [ActionFailure] otherwise.
     *
     * Implementations should check that all required parameters are specified,
     * throwing an [ActionFailure] with code [CustomActions.ERROR_CODE_PARAMETER] if an expected parameters is missing.
     *
     * @param parameters The parameters for executing the action as provided by the calling media browser client.
     * @return an optional bundle containing results computed by the action.
     *
     * @throws ActionFailure When the action cannot be completed for some reason described by [ActionFailure.errorCode].
     */
    suspend fun execute(parameters: Bundle?): Bundle?
}

/**
 * Denote that the execution of a [BrowserAction] has failed for some reason.
 *
 * @param errorCode A numeric code from `CustomActions.ERROR_CODE_*` that describes the error to media browser clients.
 * @param errorMessage An optional message describing the error.
 * Depending on the error code, this message may be displayed directly to users.
 */
class ActionFailure(
    val errorCode: Int,
    val errorMessage: String?
) : Exception(errorMessage)