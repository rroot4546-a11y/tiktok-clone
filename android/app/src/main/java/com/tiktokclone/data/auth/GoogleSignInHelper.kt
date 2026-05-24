package com.tiktokclone.data.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tiktokclone.BuildConfig
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSignIn"
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .requestEmail()
        .requestProfile()
        .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent {
        googleSignInClient.signOut()
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInResult {
        return try {
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "Sign-in successful: ${account.email}")
            GoogleSignInResult(account = account)
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign-in cancelled"
                GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Sign-in already in progress"
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign-in failed. Check SHA-1 fingerprint in Firebase Console"
                10 -> "Developer error: Check SHA-1 fingerprint and Web Client ID in Firebase Console"
                12500 -> "Sign-in failed: Google Play Services error"
                else -> "Google Sign-In error (code: ${e.statusCode})"
            }
            Log.e(TAG, "Sign-in failed with status code: ${e.statusCode}", e)
            GoogleSignInResult(error = errorMessage, statusCode = e.statusCode)
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
            Log.w(TAG, "Firebase auth failed, using Google token directly", e)
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

data class GoogleSignInResult(
    val account: GoogleSignInAccount? = null,
    val error: String? = null,
    val statusCode: Int = 0,
)

data class FirebaseAuthResult(
    val success: Boolean,
    val idToken: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val googleId: String = "",
    val error: String? = null,
)
