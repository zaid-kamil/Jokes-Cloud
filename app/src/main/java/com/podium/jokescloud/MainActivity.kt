package com.podium.jokescloud

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.common.SignInButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.podium.jokescloud.model.MyUser

class MainActivity : AppCompatActivity() {


    private lateinit var signInbtn: SignInButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val COLLECTION_USER = "users"
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            this.onSignInResult(res)
        }

    private fun createSignInIntent() {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher_foreground)
            .setIsSmartLockEnabled(true)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val userInfo = MyUser(user.email, user.uid, "google")
                db.collection(COLLECTION_USER).document(user.uid).set(userInfo)
                    .addOnSuccessListener {
                        updateUI(user)
                    }
                    .addOnFailureListener {
                        Snackbar.make(signInbtn, "error: ${it.message}", Snackbar.LENGTH_LONG)
                            .show()
                        Log.d("Firebase Error", "error: ${it.message}")
                    }
            }

        } else {

            val errorCode = response?.error?.errorCode
            val message = response?.error?.message
            Snackbar.make(signInbtn, "error: ($errorCode) $message", Snackbar.LENGTH_LONG).show()
            Log.d("Firebase Error", "error: ($errorCode) $message")

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        db = Firebase.firestore
        signInbtn = findViewById(R.id.sign_in_button)
        signInbtn.setOnClickListener {
            createSignInIntent()
        }

    }

    override fun onStart() {
        super.onStart()
        auth.currentUser?.let { updateUI(it) }
    }

    private fun updateUI(user: FirebaseUser) {
        intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


}