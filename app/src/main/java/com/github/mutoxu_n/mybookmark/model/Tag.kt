package com.github.mutoxu_n.mybookmark.model

import android.text.TextUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Tag(
    var name: String? = null,
    var bookmarks: List<Bookmark>
) {
    constructor(
        name: String,
        bookmark: Bookmark
    ): this(name, listOf(bookmark))

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_BOOKMARKS = "bookmarks"
    }
}
