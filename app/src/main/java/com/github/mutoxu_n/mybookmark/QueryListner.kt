package com.github.mutoxu_n.mybookmark.com.github.mutoxu_n.mybookmark

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class QueryListener(private var query: Query): EventListener<QuerySnapshot> {
    companion object {
        private const val TAG = "QueryListener.kt"
    }

    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if(error != null) {
            Log.w(TAG, "onEvent:error", error)
            return
        }

        if(value != null) {
            for(change in value.documentChanges) {
                when(change.type) {
                    DocumentChange.Type.ADDED -> {}
                    DocumentChange.Type.MODIFIED -> {}
                    DocumentChange.Type.REMOVED -> {}
                }
            }
        }
    }
}