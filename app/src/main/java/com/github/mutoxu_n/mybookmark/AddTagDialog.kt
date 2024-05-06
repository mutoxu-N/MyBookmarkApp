package com.github.mutoxu_n.mybookmark

import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AddTagDialog(
    modifier: Modifier,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newTagName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(true) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.term_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newTagName) },
                enabled = !isError
            ) {
                Text(text = stringResource(R.string.term_confirm))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_title_tag_create),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            TextField(
                value = newTagName,
                onValueChange = {
                    isError = it.isEmpty()
                    newTagName = it
                },
                supportingText = {
                    if(isError) Text(text = stringResource(R.string.tf_empty_error_mes,
                        stringResource(
                            R.string.term_tag_name
                        )
                    ))
                },
                isError = isError,
                label = { Text(text = stringResource(id = R.string.term_tag_name))},
                maxLines = 1,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            )
        })
}