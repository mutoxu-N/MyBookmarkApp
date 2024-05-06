package com.github.mutoxu_n.mybookmark

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.mutoxu_n.mybookmark.com.github.mutoxu_n.mybookmark.BookmarkDialog
import com.github.mutoxu_n.mybookmark.model.Bookmark
import com.github.mutoxu_n.mybookmark.ui.theme.MyBookmarkTheme

@Composable
fun BookmarkList(
    modifier: Modifier = Modifier,
    bookmarks: SnapshotStateList<Bookmark>,
    editBookmark: (Bookmark) -> Unit,
    deleteBookmark: (Bookmark) -> Unit,
    addSearchTag: (String) -> Unit,
    addTag: (Bookmark, String) -> Unit,
    deleteTag: (Bookmark, String) -> Unit,
    openUrl: (String) -> Unit,
){
    var currentBookmark by remember { mutableStateOf<Bookmark?>(null) }
    var showEditBookmarkDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(8.dp, 3.dp),
    ) {
        items(bookmarks) { bookmark ->
            key(bookmark.documentId) {
                BookmarkListItem(
                    modifier = modifier,
                    bookmark = bookmark,
                    onEditClicked = {
                        showAddTagDialog = false
                        showEditBookmarkDialog = true
                        currentBookmark = it
                    },
                    onDeleteClicked = { deleteBookmark(it) },
                    onTagClicked = { addSearchTag(it) },
                    onAddTagClicked = {
                        showEditBookmarkDialog = false
                        showAddTagDialog = true
                        currentBookmark = it
                    },
                    onDeleteTagClicked = { bm, tag ->
                        deleteTag(bm, tag)
                    },
                    openUrl = { openUrl(it) }
                )
            }
        }
    }

    if(showAddTagDialog) {
        if(currentBookmark == null)showAddTagDialog = false
        else {
            AddTagDialog(
                modifier = modifier,
                onDismiss = { showAddTagDialog = false },
                onConfirm = {
                    addTag(currentBookmark!!, it)
                    showAddTagDialog = false
                },
            )
        }
    }

    if(showEditBookmarkDialog) {
        if(currentBookmark == null) showEditBookmarkDialog = false
        else {
            BookmarkDialog(
                modifier = modifier,
                bookmark = currentBookmark!!,
                onDismissed = { showEditBookmarkDialog = false },
                onConfirmed = {
                    editBookmark(it)
                    showEditBookmarkDialog = false
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookmarkListItem(
    modifier: Modifier,
    bookmark: Bookmark,
    onEditClicked: (Bookmark) -> Unit,
    onDeleteClicked: (Bookmark) -> Unit,
    onTagClicked: (String) -> Unit,
    onAddTagClicked: (Bookmark) -> Unit,
    onDeleteTagClicked: (Bookmark, String) -> Unit,
    openUrl: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val animArrowRot by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")

    Surface(
        modifier = modifier.fillMaxWidth(1f),
        color = MaterialTheme.colorScheme.primaryContainer,

        ) {
        Column(
            modifier
                .padding(8.dp, 12.dp)
                .animateContentSize()
        ) {
            // ブックマーク名
            Row(
                modifier = modifier.fillMaxWidth(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    text = bookmark.title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if(expanded) Int.MAX_VALUE else 1
                )
                IconButton(
                    modifier = modifier.align(Alignment.Top),
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        modifier = modifier
                            .graphicsLayer { rotationZ = animArrowRot },
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }

            // 展開時
            if(expanded) {
                Spacer(modifier = modifier.size(3.dp))
                Text(text = bookmark.description)
                Divider(modifier.padding(0.dp, 5.dp, 0.dp, 0.dp))

                Row(
                    modifier = modifier.fillMaxWidth(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // URL
                    Row(
                        modifier = modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(imageVector = Icons.Default.Web, contentDescription = null)
                        Text(
                            modifier = modifier
                                .weight(1f)
                                .padding(5.dp, 0.dp)
                                .clickable { openUrl(bookmark.url) },
                            text = bookmark.url
                                .replace("https://", "")
                                .replace("http://", ""),
                            textDecoration = TextDecoration.Underline,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }

                    // アイコンボタン
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 編集ボタン
                        IconButton(onClick = { onEditClicked(bookmark) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                        }

                        // 削除ボタン
                        IconButton(onClick = { onDeleteClicked(bookmark) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                            )
                        }
                    }
                }

                // Tags
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    for(tag in bookmark.tags) {
                        InputChip(
                            selected = false,
                            onClick = { onTagClicked(tag) },
                            label = { Text(text = tag) },
                            trailingIcon = { Icon(
                                modifier = modifier.clickable { onDeleteTagClicked(bookmark, tag) },
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )}
                        )
                    }
                    InputChip(
                        selected = false,
                        onClick = { onAddTagClicked(bookmark) },
                        label = { Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        ) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BookmarkListItemPreview() {
    MyBookmarkTheme {
        BookmarkListItem(
            modifier = Modifier,
            bookmark = Bookmark(
                title = "Very Very Very Very Long Bookmark Title",
                url = "https://www.exaaaaaaample.com",
                description = "Some description about the bookmark",
                tags = listOf("tag1", "tag2", "tag3", "tag4", "tag5", "tag6", "tag7", "tag8", "tag9", "tag10", "tag11", "tag12", "tag13", "tag14", "tag15"),
            ),
            onEditClicked = {},
            onDeleteClicked = {},
            onAddTagClicked = {},
            onTagClicked = {},
            onDeleteTagClicked = { _, _ -> },
            openUrl = {}
        )
    }
}
