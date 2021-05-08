package com.example.socailapp.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socailapp.FirebaseService
import com.example.socailapp.data.Post
import kotlinx.coroutines.*

class PostViewModel : ViewModel() {

    private val firebaseService = FirebaseService()

    @ExperimentalCoroutinesApi
    val allPosts = firebaseService.getAllPost().asLiveData()

    @ExperimentalCoroutinesApi
    val currentUserPost = firebaseService.getUserPost(firebaseService.currentUser!!.uid).asLiveData()

    suspend fun addPost(post: Post) {
        firebaseService.addPost(post)
    }

    fun uploadImage(imageUri: Uri?) = viewModelScope.launch {
        if (imageUri != null) {
            firebaseService.uploadImage(imageUri)
        }
    }

    suspend fun getImageDownloadUrl(imageUri: Uri?): String = withContext(Dispatchers.Default) {
        return@withContext firebaseService.getImageDownloadUrl(imageUri)

    }

//    @ExperimentalCoroutinesApi
//    fun userPost(uid: String) = viewModelScope.launch {
//        firebaseService.getUserPost(uid)
//    }

    fun updateLike(postId: String) {
        firebaseService.updateLikes(postId)
    }

//    fun getPostById(postId: String): Post {
//        var post = Post()
//        CoroutineScope(Dispatchers.IO).launch {
//            post = firebaseService.getPostById(postId)!!
//        }
//        return post
//    }

}