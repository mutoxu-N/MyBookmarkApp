package com.github.mutoxu_n.mybookmark.com.github.mutoxu_n.mybookmark

import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import com.github.mutoxu_n.mybookmark.R
import com.github.mutoxu_n.mybookmark.model.Bookmark

@Composable
fun BookmarkDialog(
    modifier: Modifier = Modifier,
    bookmark: Bookmark? = null,
    onConfirmed: (Bookmark) -> Unit,
    onDismissed: () -> Unit,
    isEdit: Boolean = false,
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismissed() },
        dismissButton = {
            TextButton(onClick = { onDismissed() }) {
                Text(text = "破棄")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val bm = Bookmark(
                    title = title,
                    url = url,
                    description = description
                )
                onConfirmed(bm)

            }) {
                Text(text = "完了")
            }

        },
        title = {
            Text(
                text = if(isEdit) stringResource(R.string.dialog_title_bm_edit)
                       else stringResource(R.string.dialog_title_bm_create),
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier
            )
        },
        text = {
            TextField(
                value = title,
                onValueChange = { title = it }
            )
        },
    )
}