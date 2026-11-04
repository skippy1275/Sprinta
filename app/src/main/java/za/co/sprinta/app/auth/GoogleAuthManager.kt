package za.co.sprinta.app.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

object GoogleAuthManager {
    const val WEB_CLIENT_ID = "394025609994-sprinta-web-client-id.apps.googleusercontent.com"

    suspend fun launchGoogleSignIn(
        context: Context,
        onTokenReceived: (idToken: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = result.credential
            if (credential is GoogleIdTokenCredential) {
                onTokenReceived(credential.idToken)
            } else {
                onError("Received unexpected credential type.")
            }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Google Sign-In failed.")
        }
    }
}
