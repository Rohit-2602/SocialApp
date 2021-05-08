package com.example.socailapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.data.User
import com.example.socailapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class LoginActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 123
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInButton.setOnClickListener {
            googleSignInClient.signInIntent.also {
                startActivityForResult(it, RC_SIGN_IN)
                binding.progressbar.visibility = View.VISIBLE
            }
        }

    }

    override fun onStart() {
        super.onStart()
        binding.progressbar.visibility = View.GONE
        val currentUser = FirebaseService().currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == RC_SIGN_IN && data != null) {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
                account?.let {
                    googleAuthForFirebase(it)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "No Account Selected", Toast.LENGTH_SHORT).show()
            binding.progressbar.visibility = View.GONE
        }

    }

    private fun googleAuthForFirebase(googleSignInAccount: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Firebase.auth.signInWithCredential(credentials).await()
                withContext(Dispatchers.Main) {
                    val firebaseUser = Firebase.auth.currentUser!!
                    val user = User(
                        name = firebaseUser.displayName,
                        lowercaseName = firebaseUser.displayName!!.toLowerCase(Locale.ROOT),
                        id = firebaseUser.uid,
                        imageURL = firebaseUser.photoUrl!!.toString()
                    )
                    FirebaseService().addUser(user)
                    updateUI(FirebaseService().currentUser)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error " + e.message, Toast.LENGTH_SHORT)
                        .show()
                    binding.progressbar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            binding.progressbar.visibility = View.GONE
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}