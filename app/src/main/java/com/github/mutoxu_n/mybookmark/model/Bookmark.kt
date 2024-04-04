package com.github.mutoxu_n.mybookmark.model

import android.text.TextUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@IgnoreExtraProperties
data class Bookmark(
    var userId: String? = null,
    var userName: String? = null,
    var title: String? = null,
    var url: String? = null,
    var description: String? = null,
    var tags: List<Tag>? = null,
    @ServerTimestamp var timestamp: Date? = null,
) {
    constructor(
        user: FirebaseUser,
        title: String,
        url: String,
        description: String?,
        tags: List<Tag>,
    ): this() {
        this.userId = user.uid
        this.userName = user.displayName
        if(TextUtils.isEmpty(this.userName)) this.userName = user.email

        this.title = title
        this.url = url
        this.description = description
        this.tags = tags
    }

    companion object {
        const val FIELD_USER_ID = "userId"
        const val FIELD_USER_NAME = "userName"
        const val FIELD_TITLE = "title"
        const val FIELD_URL = "url"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_TAGS = "tags"
        const val FIELD_TIMESTAMP = "timestamp"
    }
}
