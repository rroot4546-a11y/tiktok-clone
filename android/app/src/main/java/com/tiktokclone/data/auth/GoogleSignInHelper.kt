package com.tiktokclone.data.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tiktokclone.BuildConfig
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(private val context: Context) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .requestEmail()
        .requestProfile()
        .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        return try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            null
        }
    }

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): FirebaseAuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            val idToken = firebaseUser?.getIdToken(true)?.await()?.token

            FirebaseAuthResult(
                success = true,
                idToken = idToken ?: account.idToken ?: "",
                email = firebaseUser?.email ?: account.email ?: "",
                displayName = firebaseUser?.displayName ?: account.displayName ?: "",
                photoUrl = firebaseUser?.photoUrl?.toString() ?: account.photoUrl?.toString() ?: "",
                googleId = firebaseUser?.uid ?: account.id ?: "",
            )
        } catch (e: Exception) {
            // Fallback: use Google Sign-In token directly without Firebase
            FirebaseAuthResult(
                success = true,
                idToken = account.idToken ?: "",
                email = account.email ?: "",
                displayName = account.displayName ?: "",
                photoUrl = account.photoUrl?.toString() ?: "",
                googleId = account.id ?: "",
            )
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }
}

data class FirebaseAuthResult(
    val success: Boolean,
    val idToken: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val googleId: String = "",
    val error: String? = null,
)
