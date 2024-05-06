package com.github.mutoxu_n.mybookmark

import android.content.Intent
import android.net.Uri
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
import com.google.firebase.firestore.Query
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
    private var searchTags = mutableStateListOf<String>()
    private var isConnected by mutableStateOf(false)
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
                                userChanged(auth.currentUser)

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

        // ログイン
        auth.currentUser?.getIdToken(false)?.addOnCompleteListener {
            // ログインに失敗したらログアウトしてログイン画面を表示
            isConnected = it.isSuccessful
            if(!it.isSuccessful)  {
                logout()
                login()
            }
        }
        login()


        setContent {
            MyBookmarkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if(isConnected) {
                        MainScreen(
                            bookmarks = bookmarks,
                            addBookmark= {createBookmark(it) },
                            updateBookmark = { updateBookmark(it)},
                            deleteBookmark = { deleteBookmark(it) },
                            addTag ={ bm, tag -> addTag2Bookmark(bm, tag) },
                            deleteTag = { bm, tag -> deleteTagFromBookmark(bm, tag) },
                            addSearchTag= { addSearchTag(it) },
                            deleteSearchTag = {deleteSearchTag(it)},
                            openUrl = { url -> openUrl(url) },
                            login= { login() },
                            logout= { logout() },
                        )
                    } else {
                        LoadingScreen(
                            modifier = Modifier.fillMaxSize(1f),
                            onLoginClicked = {
                                logout()
                                login()
                            },
                        )
                    }
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
                ref.document(docRef.id).update(Bookmark.FIELD_DOCUMENT_ID, docRef.id)
                ref.document(docRef.id).update(Bookmark.FIELD_TAGS, listOf<String>())
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
        }
    }

    private fun deleteBookmark(bookmark: Bookmark) {
        if(auth.uid == null || bookmark.documentId == null) return

        for(tag in bookmark.tags)
            deleteTagFromBookmark(bookmark, tag)

        firestore
            .collection("users")
            .document(auth.uid!!)
            .collection("bookmarks")
            .document(bookmark.documentId)
            .delete()
    }

    private fun addTag2Bookmark(bookmark: Bookmark, tag: String) {
        if(auth.uid == null) return

        // ブックマークにタグを追加
        val bmRef = firestore
            .collection("users")
            .document(auth.uid!!)
            .collection("bookmarks")
        bookmark.documentId?.let {
            bmRef.document(it).update(
                Bookmark.FIELD_TAGS,
                bookmark.tags.toMutableList().apply { add(tag) }
            )
        }

        val tagRef = firestore
            .collection("users")
            .document(auth.uid!!)
            .collection("tags")
            .document(tag)

        tagRef.get().addOnCompleteListener {
            if(it.result.data == null) {
                // タグが存在しない場合
                tagRef.set(
                    mapOf(
                        Tag.FIELD_NAME to tag,
                        Tag.FIELD_BOOKMARK_IDS to listOf(bookmark.documentId),
                    )
                )

            } else {
                // タグが存在するとき
                val tags = it.result.data?.get(Tag.FIELD_BOOKMARK_IDS) as List<*>?
                if (tags != null)
                    tagRef.update(Tag.FIELD_BOOKMARK_IDS, tags.toMutableList().apply { add(bookmark.documentId) })
            }
        }

    }

    private fun deleteTagFromBookmark(bookmark: Bookmark, tag: String) {
        if(auth.uid == null) return

        // ブックマークからタグを削除
        val bmRef = firestore
            .collection("users")
            .document(auth.uid!!)
            .collection("bookmarks")
        bookmark.documentId?.let {
            bmRef.document(it).update(
                Bookmark.FIELD_TAGS,
                bookmark.tags.toMutableList().apply { remove(tag) }
            )
        }

        // タグを削除
        val tagRef = firestore
            .collection("users")
            .document(auth.uid!!)
            .collection("tags")
            .document(tag)

        tagRef.get().addOnCompleteListener {
            // タグが存在するとき
            val tags = it.result.data?.get(Tag.FIELD_BOOKMARK_IDS) as List<*>? ?: return@addOnCompleteListener
            val newTags = tags.toMutableList().apply { remove(bookmark.documentId) }

            // 対応するブックマークがなくなったら削除
            if(newTags.isEmpty()) tagRef.delete()
            else tagRef.update(Tag.FIELD_BOOKMARK_IDS, newTags)
        }
    }


    // Firestoreリスナを設定
    private fun registerSnapshotListener() {
        snapshotListener?.remove()

        if(searchTags.isEmpty())
            snapshotListener = firestore
                .collection("users")
                .document(auth.uid!!)
                .collection("bookmarks")
                .orderBy(Bookmark.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
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
        else {
            firestore
                .collection("users")
                .document(auth.uid!!)
                .collection("tags")
                .whereIn(Tag.FIELD_NAME, searchTags)
                .get().addOnSuccessListener {
                    // タグに含まれているブックマークIDを探す
                    val bookmarkIds = mutableListOf<String>()
                    it.documents.forEach { docs ->
                        for(id in (docs[Tag.FIELD_BOOKMARK_IDS] as List<*>).map { obj -> obj.toString() })
                            if(!bookmarkIds.contains(id))
                                bookmarkIds.add(id)
                    }

                    // ブックマーク一覧を取得
                    snapshotListener = firestore
                        .collection("users")
                        .document(auth.uid!!)
                        .collection("bookmarks")
                        .whereIn(Bookmark.FIELD_DOCUMENT_ID, bookmarkIds)
                        .orderBy(Bookmark.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
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


    private fun login() {
        // ログイン済みならパス
        if(auth.uid != null) {
            registerSnapshotListener()
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
        isConnected = false
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun addSearchTag(tag: String) {
        if(!searchTags.contains(tag)) {
            searchTags.add(tag)
            registerSnapshotListener()
        }
    }

    private fun deleteSearchTag(tag: String) {
        searchTags.remove(tag)
        registerSnapshotListener()
    }


    private fun userChanged(user: FirebaseUser?) {
        isConnected = true
        Log.d(TAG, "Now Logged in: ${user?.displayName}")
        registerSnapshotListener()
    }


    private fun showLoginFailedMessage() {
        isConnected = false
        Toast
            .makeText(this, "Some problems occurred while logging in.", Toast.LENGTH_SHORT)
            .show()
    }


}

@Composable
fun MainScreen(
    bookmarks: SnapshotStateList<Bookmark>,
    addBookmark: (Bookmark) -> Unit,
    updateBookmark: (Bookmark) -> Unit,
    deleteBookmark: (Bookmark) -> Unit,
    addTag: (Bookmark, String) -> Unit,
    deleteTag: (Bookmark, String) -> Unit,
    addSearchTag: (String) -> Unit,
    deleteSearchTag: (String) -> Unit,
    openUrl: (String) -> Unit,
    login: () -> Unit,
    logout: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // TODO: アプリバーとSearchTag一覧作成
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
    ) {
        BookmarkList(
            modifier = Modifier.padding(it),
            bookmarks = bookmarks,
            editBookmark = { bm -> updateBookmark(bm) },
            deleteBookmark = { bm -> deleteBookmark(bm) },
            addSearchTag = { bm -> addSearchTag(bm) },
            addTag = { bm, tag -> addTag(bm, tag) },
            deleteTag = { bm, tag -> deleteTag(bm, tag) },
            openUrl = { url -> openUrl(url) }
        )
    }

    if(showAddDialog) {
        BookmarkDialog(
            onDismissed = { showAddDialog = false },
            onConfirmed = { bookmark ->
                addBookmark(bookmark)
                showAddDialog = false
            },
        )
    }
}
