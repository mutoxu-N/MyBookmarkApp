package com.github.mutoxu_n.mybookmark.com.github.mutoxu_n.mybookmark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.mutoxu_n.mybookmark.R
import com.github.mutoxu_n.mybookmark.model.Bookmark

@Composable
fun BookmarkDialog(
    modifier: Modifier = Modifier,
    bookmark: Bookmark? = null,
    onConfirmed: (Bookmark) -> Unit,
    onDismissed: () -> Unit,
) {
    var title by remember { mutableStateOf(bookmark?.title ?: "") }
    var isTitleError by remember { mutableStateOf(bookmark == null) }
    var url by remember { mutableStateOf(bookmark?.url ?: "") }
    var isUrlError by remember { mutableStateOf(bookmark == null) }
    var description by remember { mutableStateOf(bookmark?.description ?: "") }
    
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismissed() },
        dismissButton = {
            TextButton(onClick = { onDismissed() }) {
                Text(text = stringResource(id = R.string.term_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val bm = Bookmark(
                        documentId = bookmark?.documentId,
                        title = title,
                        url = url,
                        description = description,
                        tags = bookmark?.tags ?: emptyList(),
                        timestamp = bookmark?.timestamp,
                    )
                    onConfirmed(bm)
                },
                enabled = !isTitleError && !isUrlError
            ) {
                Text(text = stringResource(id = R.string.term_confirm))
            }

        },
        title = {
            Text(
                text = if(bookmark == null) stringResource(R.string.dialog_title_bm_create)
                       else stringResource(R.string.dialog_title_bm_edit),
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier
            )
        },
        text = {
           Column {
               Divider(modifier = Modifier.padding(bottom = 15.dp))

               TextField(
                   value = title,
                   leadingIcon = { Icon(imageVector = Icons.Default.Title, contentDescription = null)},
                   label = { Text(text = stringResource(id = R.string.term_title)) },
                   onValueChange = {
                       title = it.replace("\n", "")
                       isTitleError = title.isEmpty()
                   },
                   maxLines = 1,
                   isError = isTitleError,
                   supportingText = {
                        if(isTitleError)
                            Text(
                                text = stringResource(id = R.string.tf_empty_error_mes,
                                    stringResource(R.string.term_title)
                                )
                            )
                   },
               )
               TextField(
                   value = url,
                   leadingIcon = { Icon(imageVector = Icons.Default.Link, contentDescription = null)},
                   label = { Text(text = stringResource(id = R.string.term_url)) },
                   onValueChange = {
                       url = it.replace("\n", "")
                       isUrlError = url.isEmpty()
                   },
                   maxLines = 1,
                   isError = isUrlError,
                   supportingText = {
                       if(isUrlError)
                           Text(
                               text = stringResource(id = R.string.tf_empty_error_mes,
                                   stringResource(R.string.term_url)
                               )
                           )
                   },
               )
               TextField(
                   value = description,
                   leadingIcon = { Icon(imageVector = Icons.Default.ShortText, contentDescription = null)},
                   label = { Text(text = stringResource(id = R.string.term_description)) },
                   onValueChange = { description = it },
               )
           }
        },
    )
}