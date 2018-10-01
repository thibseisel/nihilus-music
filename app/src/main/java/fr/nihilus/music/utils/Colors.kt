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

package fr.nihilus.music.utils

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.FloatRange
import android.support.v4.graphics.ColorUtils

/**
 * Produce a darker shade of this color by a given factor.
 */
@ColorInt
fun darker(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) factor: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] *= factor
    return Color.HSVToColor(hsv)
}

/**
 * Return the alpha component of a color int.
 */
inline val @receiver:ColorInt Int.alpha get() = (this shr 24) and 0xff

/**
 * Return the red component of a color int.
 */
inline val @receiver:ColorInt Int.red get() = (this shr 16) and 0xff

/**
 * return the green component of a color int.
 */
inline val @receiver:ColorInt Int.green get() = (this shr 8) and 0xff

/**
 * Return the blue component of a color int.
 */
inline val @receiver:ColorInt Int.blue get() = this and 0xff

/**
 * Computes the relative luminance of a color.
 * Assumes sRGB encoding. Based on the formula for relative luminance
 * defined in WCAG 2.0, W3C Recommendation 11 December 2008.
 *
 * @receiver A color packed integer in the sRGB color space.
 * @return a value between 0 (darkest black) and 1 (lightest white).
 * @see Color.luminance
 */
val @receiver:ColorInt Int.luminance: Float
    get() = (0.2126f * red) + (0.7152f * green) + (0.0722f * blue)

/**
 * Convert RGB components of a color to HSL (hue-saturation-lightness).
 * - `outHsl[0]` is Hue in `[0..360[`
 * - `outHsl[1]` is Saturation in `[0..1]`
 * - `outHsl[2]` is Lightness in `[0..1]`
 *
 * @receiver A color from the sRGB space from which HSL components should be extracted.
 * @param outHsl An optional 3-element array which holds the resulting HSL components.
 * If this argument is not provided, a new array will be created.
 *
 * @return The resulting HSL components, for convenience. This is the same as [outHsl].
 */
fun @receiver:ColorInt Int.toHsl(outHsl: FloatArray = FloatArray(3)) = outHsl.also {
    ColorUtils.colorToHSL(this, it)
}