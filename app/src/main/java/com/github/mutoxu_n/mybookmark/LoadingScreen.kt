package com.github.mutoxu_n.mybookmark;

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit
) {
    Column {
        Image(
            modifier = modifier.size(100.dp),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null
        )

        Button(onClick = { onLoginClicked() }) {
            Text(text = stringResource(R.string.term_login))
        }
    }
}