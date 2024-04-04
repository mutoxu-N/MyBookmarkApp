package com.github.mutoxu_n.mybookmark.model

import android.text.TextUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Tag(
    var userId: String? = null,
    var userName: String? = null,
    var name: String? = null,
) {
    constructor(
        user: FirebaseUser,
        name: String,
    ): this() {
        this.userId = user.uid
        this.userName = user.displayName
        if(TextUtils.isEmpty(this.userName)) this.userName = user.email

        this.name = name
    }
    companion object {
        const val FIELD_USER_ID = "userId"
        const val FIELD_USER_NAME = "userName"
        const val FIELD_NAME = "name"
    }
}
