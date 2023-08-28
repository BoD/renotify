/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2022-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jraf.android.renotify.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jraf.android.renotify.R
import org.jraf.android.renotify.ui.theme.RenotifyTheme
import org.jraf.android.renotify.util.toDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    shouldShowRequestPermissionRationale: Boolean,
    shouldShowGoToSettingsText: Boolean,
    isServiceEnabled: Boolean,
    onServiceEnabledClick: () -> Unit,
    onRequestPermissionRationaleClick: () -> Unit,
    onGoToSettingsClick: () -> Unit,
) {
    RenotifyTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(stringResource(id = R.string.app_name)) })
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (shouldShowRequestPermissionRationale) {
                        ExplainerWithButton(
                            explainerResId = R.string.main_requestPermissionRationale_text,
                            buttonTextResId = R.string.main_requestPermissionRationale_button,
                            onButtonClick = onRequestPermissionRationaleClick
                        )
                        Spacer(modifier = Modifier.size(8.sp.toDp()))
                    }

                    if (shouldShowGoToSettingsText) {
                        ExplainerWithButton(
                            explainerResId = R.string.main_goToSettings_text,
                            buttonTextResId = R.string.main_goToSettings_button,
                            onButtonClick = onGoToSettingsClick
                        )
                        Spacer(modifier = Modifier.size(8.sp.toDp()))
                    }

                    OnOffButton(
                        isServiceEnabled = isServiceEnabled,
                        onServiceEnabledClick = onServiceEnabledClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplainerWithButton(
    @StringRes explainerResId: Int,
    @StringRes buttonTextResId: Int,
    onButtonClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(8.sp.toDp())
                .weight(1F),
            text = stringResource(explainerResId),
        )

        Button(onClick = onButtonClick) {
            Text(text = stringResource(buttonTextResId))
        }
        Spacer(modifier = Modifier.size(8.sp.toDp()))
    }
}


@Composable
private fun OnOffButton(isServiceEnabled: Boolean, onServiceEnabledClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onServiceEnabledClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(if (isServiceEnabled) R.string.main_service_switch_enabled else R.string.main_service_switch_disabled),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = isServiceEnabled,
            onCheckedChange = null,
        )
    }
}


@Composable
@Preview
private fun MainLayoutPreview() {
    MainLayout(
        shouldShowRequestPermissionRationale = true,
        shouldShowGoToSettingsText = true,
        isServiceEnabled = false,
        onServiceEnabledClick = {},
        onRequestPermissionRationaleClick = {},
        onGoToSettingsClick = {},
    )
}
