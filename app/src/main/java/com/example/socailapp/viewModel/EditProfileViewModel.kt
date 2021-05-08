package com.example.socailapp.viewModel

import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socailapp.UserDao
import com.example.socailapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class EditProfileViewModel @ViewModelInject constructor(private val userDao: UserDao) : ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val imageCollection = Firebase.storage.reference
    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val currentUserCollection = firebaseDB.collection("users").document(currentUserId)

    fun updateDBUser(user: User) = viewModelScope.launch {
        userDao.updateUser(user)
    }

    fun updateFirebaseUser(name: String, description: String, imageUrl: String) {
        currentUserCollection.update(
            "name", name,
            "lowercaseName", name.toLowerCase(Locale.ROOT),
            "description", description,
            "imageURL", imageUrl
        )
    }

    fun uploadImage(imageUri: Uri) = viewModelScope.launch {
        try {
            val path = imageUri.toString()
            Log.i("Profile", "UPLOAD IMAGE")
            imageCollection.child("${currentUserId}/images/$path").putFile(imageUri).await()
        } catch (e: Exception) {
            Log.i("Profile", e.message.toString())
        }
    }

    suspend fun getImageDownloadUrl(imageUri: Uri): String {
        Log.i("Profile", "GET DOWNLOAD IMAGE URL")
        val path = imageUri.toString()
        return imageCollection.child("${currentUserId}/images/$path").downloadUrl.await().toString()
    }

}