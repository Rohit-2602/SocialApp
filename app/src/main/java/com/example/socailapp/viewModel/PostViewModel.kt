package com.example.socailapp.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socailapp.data.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostViewModel : ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    val currentUserId = Firebase.auth.currentUser!!.uid
    private val userPostCollection = firebaseDB.collection("posts")
    private val allPostCollection = firebaseDB.collection("allPosts")
    private val imageCollection = Firebase.storage.reference

    fun addPost(post: Post) = viewModelScope.launch {
        val ref = allPostCollection.document()
        val id = ref.id
        post.id = id
        userPostCollection.document(currentUserId).collection("user_post").document(post.id)
            .set(post).await()
        allPostCollection.document(id).set(post).await()
    }

    fun uploadImage(imageUri: Uri?) = viewModelScope.launch {
        try {
            val path = imageUri.toString()
            Log.i("Post viewModel", "UPLOAD IMAGE")
            if (imageUri != null) {
                imageCollection.child("${currentUserId}/images/$path").putFile(imageUri).await()
            }
        } catch (e: Exception) {
            Log.i("Post viewModel", e.message.toString())
        }
    }

    suspend fun getImageDownloadUrl(imageUri: Uri?): String? {
        Log.i("Post viewModel", "GET DOWNLOAD IMAGE URL")
        return if (imageUri != null) {
            val path = imageUri.toString()
            imageCollection.child("${currentUserId}/images/$path").downloadUrl.await()
                .toString()
        } else {
            null
        }
    }

}