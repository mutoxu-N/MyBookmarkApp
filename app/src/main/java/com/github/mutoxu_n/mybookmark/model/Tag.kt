package com.github.mutoxu_n.mybookmark.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Tag(
    var name: String? = null,
    var bookmarkIds: List<String>
) {
    constructor(
        name: String,
        bookmarkId: String,
    ): this(name, listOf(bookmarkId))

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_BOOKMARKS = "bookmarkIds"
    }
}
