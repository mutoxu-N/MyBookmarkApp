package com.github.mutoxu_n.mybookmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    searchTags: List<String>,
    onTagClicked: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var menuVisibility by remember { mutableStateOf(false) }

    Column {
        Spacer(modifier = modifier.size(5.dp))
        Row {
            FlowRow(
                modifier = modifier
                    .weight(1f)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                for (tag in searchTags) {
                    InputChip(
                        selected = false,
                        onClick = { onTagClicked(tag) },
                        label = { Text(text = tag) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            IconButton(
                modifier = modifier.align(Alignment.Top),
                onClick = { menuVisibility = true }
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                DropdownMenu(
                    expanded = menuVisibility,
                    onDismissRequest = { menuVisibility = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.term_logout)) },
                        onClick = { onLogout() }
                    )
                }
            }


        }
        Divider()
    }
}
