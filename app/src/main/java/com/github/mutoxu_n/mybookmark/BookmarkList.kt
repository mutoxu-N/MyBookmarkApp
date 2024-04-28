package com.github.mutoxu_n.mybookmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mutoxu_n.mybookmark.model.Bookmark

@Composable
fun BookmarkList(
    modifier: Modifier = Modifier,
    bookmarks: SnapshotStateList<Bookmark>,
){
    LazyColumn(
        modifier = modifier.fillMaxWidth(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(8.dp, 3.dp),
    ) {
        items(bookmarks) { bookmark ->
            key(bookmark.documentId) {
                BookmarkListItem(
                    modifier = modifier,
                    bookmark = bookmark
                )
            }
        }

    }
}

@Composable
fun BookmarkListItem(
    modifier: Modifier,
    bookmark: Bookmark,
) {
    Surface(
        modifier = modifier.fillMaxWidth(1f),
        color = MaterialTheme.colorScheme.primaryContainer,

        ) {
        Column(modifier.padding(8.dp, 12.dp)) {
            Text(text = bookmark.title?:"null")
            Text(text = bookmark.documentId.toString())
        }
    }
}