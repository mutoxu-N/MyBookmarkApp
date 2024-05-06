package com.github.mutoxu_n.mybookmark;

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = modifier.size(LocalConfiguration.current.screenWidthDp.dp*0.8f),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null
        )

        Button(
            onClick = { onLoginClicked() },
        ) {
            Text(
                modifier = modifier.padding(horizontal = 30.dp),
                text = stringResource(R.string.term_login)
            )
        }
    }
}