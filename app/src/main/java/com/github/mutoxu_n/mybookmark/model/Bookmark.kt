package com.github.mutoxu_n.mybookmark.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.Objects

@IgnoreExtraProperties
data class Bookmark(
    val documentId: String? = null,
    var title: String,
    var url: String,
    var description: String = "",
    var tags: List<Tag> = listOf(),
    @ServerTimestamp var timestamp: Date? = null,
) {
    companion object {
        const val DOCUMENT_ID = "documentId"
        const val FIELD_TITLE = "title"
        const val FIELD_URL = "url"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_TAGS = "tags"
        const val FIELD_TIMESTAMP = "timestamp"

        @Suppress("UNCHECKED_CAST")
        fun toBookmark(map: Map<String, Any>): Bookmark {
            return Bookmark(
                documentId = map[DOCUMENT_ID] as String?,
                title = map[FIELD_TITLE] as String,
                url = map[FIELD_URL] as String,
                description = map[FIELD_DESCRIPTION] as String,
                tags = map[FIELD_TAGS] as List<Tag>,
                timestamp = (map[FIELD_TIMESTAMP] as Timestamp).toDate(),
            )
        }
    }
}
