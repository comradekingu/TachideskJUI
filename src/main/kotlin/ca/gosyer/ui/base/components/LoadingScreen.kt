/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.min

@Composable
fun LoadingScreen(
    isLoading: Boolean = true,
    modifier: Modifier = Modifier.fillMaxSize(),
    /*@FloatRange(from = 0.0, to = 1.0)*/
    progress: Float? = null,
    errorMessage: String? = null,
    retryMessage: String = "Retry",
    retry: (() -> Unit)? = null
) {
    Surface(modifier) {
        BoxWithConstraints {
            if (isLoading) {
                val size = remember(maxHeight, maxWidth) {
                    min(maxHeight, maxWidth) / 2
                }
                // Workaround for random `Cannot round NaN value.` exception
                val floatProgress = progress ?: 0F
                if (progress != null) {
                    CircularProgressIndicator(floatProgress, Modifier.align(Alignment.Center).size(size))
                } else {
                    CircularProgressIndicator(Modifier.align(Alignment.Center).size(size))
                }
            } else {
                ErrorScreen(errorMessage, modifier, retryMessage, retry)
            }
        }
    }
}
