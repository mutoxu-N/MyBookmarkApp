package com.github.mutoxu_n.mybookmark

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.github.mutoxu_n.mybookmark.com.github.mutoxu_n.mybookmark.BookmarkDialog
import com.github.mutoxu_n.mybookmark.model.Bookmark
import com.github.mutoxu_n.mybookmark.model.Tag
import com.github.mutoxu_n.mybookmark.ui.theme.MyBookmarkTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "[MyBookmark] MainActivity.kt"
        private const val AUTH_EMULATOR_HOST = "10.0.2.2"
        private const val AUTH_EMULATOR_PORT = 9099
        private const val FIRESTORE_EMULATOR_HOST = "10.0.2.2"
        private const val FIRESTORE_EMULATOR_PORT = 8080
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var signInClient: SignInClient
    private var bookmarks = mutableStateListOf<Bookmark>()
    private var snapshotListener: ListenerRegistration? = null


    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            if(result.data != null) {
                // Googleアカウント ログイン成功
                val credential = try {
                    signInClient.getSignInCredentialFromIntent(result.data)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                val idToken = credential?.googleIdToken
                if(idToken != null) {
                    // Googleアカウントの資格情報からFirebaseUserにログイン
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful)
                                changeUser(auth.currentUser)

                            else {
                                Log.w(TAG, "Firebaseログインに失敗しました")
                                showLoginFailedMessage()
                            }
                        }


                } else {
                    // ログイン失敗
                    Log.w(TAG, "GoogleアカウントTokenの取得に失敗しました.")
                    showLoginFailedMessage()
                }

            } else {
                // ログイン失敗
                Log.w(TAG, "Googleアカウントの資格情報が不明です.")
                showLoginFailedMessage()
            }

        } else {
            // ログイン失敗
            Log.w(TAG, "OneTap認証が正常に終了しませんでした.")
            showLoginFailedMessage()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SignInClient取得
        signInClient = Identity.getSignInClient(this@MainActivity)

        // Firebase Auth取得
        auth = Firebase.auth

        // Firestore取得
        firestore = Firebase.firestore

        if (BuildConfig.DEBUG) {
            auth.useEmulator(AUTH_EMULATOR_HOST, AUTH_EMULATOR_PORT)
            firestore.useEmulator(FIRESTORE_EMULATOR_HOST, FIRESTORE_EMULATOR_PORT)
        }

        login()

        setContent {
            MyBookmarkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        bookmarks = bookmarks,
                        createBookmark= { bm -> createBookmark(bm) },
                        updateBookmark = {},
                        addTagBookmark ={ _, _ -> },
                        removeTagBookmark = { _, _ -> },
                        searchFromTag= {},
                        login= { login() },
                        logout= { logout() },
                    )
                }
            }
        }
    }


    private fun createBookmark(bookmark: Bookmark) {
        if(auth.uid == null) return

        val ref = firestore
            .collection("users")
            .document(auth.uid!!)
            .collection("bookmarks")
        ref
            .add(bookmark)
            .addOnSuccessListener { docRef ->
                ref.document(docRef.id).update(Bookmark.DOCUMENT_ID, docRef.id)
            }

    }

    private fun updateBookmark(bookmark: Bookmark) {
        if(auth.uid == null) return

        val ref = firestore
            .collection("users")
            .document(auth.uid!!)
            .collection("bookmarks")

        bookmark.documentId?.let {
            ref.document(it).update(Bookmark.FIELD_TITLE, bookmark.title)
            ref.document(it).update(Bookmark.FIELD_URL, bookmark.url)
            ref.document(it).update(Bookmark.FIELD_DESCRIPTION, bookmark.description)
            ref.document(it).update(Bookmark.FIELD_TAGS, bookmark.tags)
        }

    }


    private fun getBookmarkList() {
            firestore
                .collection("users")
                .document(auth.uid!!)
                .collection("bookmarks")
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        bookmarks.clear()
                        task.result.forEach { bookmarks.add(Bookmark.toBookmark(it.data)) }

                        if(this.snapshotListener == null) {
                            snapshotListener = firestore
                                .collection("users")
                                .document(auth.uid!!)
                                .collection("bookmarks")
                                .addSnapshotListener { value, error ->
                                    if(error != null) {
                                        Log.w(TAG, "Firestore / Listen failed!", error)
                                        return@addSnapshotListener
                                    }

                                    if(value != null) {
                                        bookmarks.clear()
                                        value.forEach {
                                            // データをBookmarkに変更してbookmarksを更新
                                            try {
                                                bookmarks.add(Bookmark.toBookmark(it.data))

                                            } catch (e: ClassCastException) {
                                                Log.w(TAG, "Data from Firestore cannot be converted to Bookmark")
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
    }


    private fun login() {
        // ログイン済みならパス
        if(auth.uid != null) {
            getBookmarkList()
            return
        }

        val request: BeginSignInRequest? = try {
                BeginSignInRequest.Builder()
                    .setPasswordRequestOptions(
                        BeginSignInRequest.PasswordRequestOptions.Builder().setSupported(true).build()
                    )
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }


        // Googleアカウントログイン
        request?.let {
            signInClient.beginSignIn(it)
                .addOnSuccessListener { result ->
                    signInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "端末で1つ以上のGoogleアカウントにログインしている必要があります. ")
                    e.printStackTrace()
                }
        }
    }

    private fun logout() {
        // 変更検知リスナ削除
        snapshotListener?.remove()
        snapshotListener = null
        auth.signOut()
    }


    private fun changeUser(user: FirebaseUser?) {
        Log.d(TAG, "Now Logged in: ${user?.displayName}")
        getBookmarkList()
    }


    private fun showLoginFailedMessage() {
        Toast
            .makeText(this, "Some problems occurred while logging in.", Toast.LENGTH_SHORT)
            .show()
    }


}

@Composable
fun MainScreen(
    bookmarks: SnapshotStateList<Bookmark>,
    createBookmark: (Bookmark) -> Unit,
    updateBookmark: (Bookmark) -> Unit,
    addTagBookmark: (Bookmark, Tag) -> Unit,
    removeTagBookmark: (Bookmark, Tag) -> Unit,
    searchFromTag: (Tag) -> Unit,
    login: () -> Unit,
    logout: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) {
        BookmarkList(
            modifier = Modifier.padding(it),
            bookmarks = bookmarks,
        )
    }

    if(showAddDialog) {
        BookmarkDialog(
            onDismissed = { showAddDialog = false },
            onConfirmed = { bookmark ->
                createBookmark(bookmark)
                showAddDialog = false
            },
        )
    }
}
