package com.github.mutoxu_n.mybookmark

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.mutoxu_n.mybookmark.model.Bookmark
import com.github.mutoxu_n.mybookmark.ui.theme.MyBookmarkTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    companion object {
        private const val AUTH_EMULATOR_HOST = "10.0.2.2"
        private const val AUTH_EMULATOR_PORT = 9099
        private const val FIRESTORE_EMULATOR_HOST = "10.0.2.2"
        private const val FIRESTORE_EMULATOR_PORT = 8080
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var gsc: GoogleSignInClient
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        Log.e("MainActivity.kt", "code: ${result.resultCode}")

        if(result.resultCode == RESULT_OK) {
            if(result.data != null) {
                val credential = try {
                    Identity.getSignInClient(this).getSignInCredentialFromIntent(result.data)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                Log.e("MainActivity.kt", "${credential?.displayName}")

                val idToken = credential?.googleIdToken
                if(idToken != null)
                    Toast.makeText(this, "Logged in as ${credential.displayName}!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("MainActivity.kt", "data: ${result.data}")
            try {
                Identity.getSignInClient(this).getSignInCredentialFromIntent(result.data)
            } catch (e: Exception) {
                Log.e("MainActivity.kt", e.message.toString())
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Auth取得
        firebaseAuth = Firebase.auth

        // Firestore取得
        firestore = Firebase.firestore

        if (BuildConfig.DEBUG) {
            firebaseAuth.useEmulator(AUTH_EMULATOR_HOST, AUTH_EMULATOR_PORT)
            firestore.useEmulator(FIRESTORE_EMULATOR_HOST, FIRESTORE_EMULATOR_PORT)
        }

        gsc = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())

        setContent {
            MyBookmarkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onAddItemClicked = { login() }
                    )
                }
            }
        }
    }


    private fun onAddItemClicked() {
        val ref = firestore.collection("test")
        ref.add(Bookmark(
            title = "Google",
            url = "https://www.google.com/",
        ))
    }

    private fun login() {
        val oneTapClient = Identity.getSignInClient(this)
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

        request?.let {
            oneTapClient.beginSignIn(it)
                .addOnSuccessListener { result ->
                    signInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }

//        val intent = gsc.signInIntent
//        signInLauncher.launch(intent)
    }
}

@Composable
fun MainScreen(
    onAddItemClicked: () -> Unit,
) {
    Button(onClick = {
        onAddItemClicked()
    }) {
        Text(text = "Add!")
    }
}