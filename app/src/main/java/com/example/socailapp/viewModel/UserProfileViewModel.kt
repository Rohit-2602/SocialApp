package com.example.socailapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserProfileViewModel: ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val userPostCollection = firebaseDB.collection("posts")
    private val userCollection = firebaseDB.collection("users")
    private val allPostCollection = firebaseDB.collection("allPosts")

    @ExperimentalCoroutinesApi
    fun userActivity(userId: String): LiveData<List<Post>> {
        return getUserPost(userId).asLiveData()
    }

    @ExperimentalCoroutinesApi
    fun getCurrentUser(userId: String): LiveData<User> {
        Log.i("User Profile ViewModel", "Get Current User")
        return getUserById(userId).asLiveData()
    }

    private suspend fun getPostById(postId: String): Post {
        return allPostCollection.document(postId).get().await().toObject(Post::class.java)!!
    }

    fun updateLikes(postId: String) = viewModelScope.launch {
        val post = getPostById(postId)
        val isLiked = post.likedBy.contains(currentUserId)

        if (isLiked) {
            post.likedBy.remove(currentUserId)
        } else {
            post.likedBy.add(currentUserId)
        }
        allPostCollection.document(postId).set(post)
        userPostCollection.document(post.creatorId).collection("user_post").document(postId)
            .set(post)
    }

    @ExperimentalCoroutinesApi
    fun getUserById(userId: String): Flow<User> {
        return callbackFlow {
            val user = userCollection.document(userId)
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(cause = firebaseFirestoreException, message = "Error Fetching All Posts")
                        return@addSnapshotListener
                    }
                    val map = documentSnapshot!!.toObject(User::class.java)!!
                    offer(map)
                }
            awaitClose {
                user.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getUserPost(userId: String): Flow<List<Post>> {
        return callbackFlow {
            val userPost = userPostCollection.document(userId).collection("user_post")
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(cause = firebaseFirestoreException, message = "Error Fetching All Posts")
                        return@addSnapshotListener
                    }
                    val map = querySnapshot!!.documents.mapNotNull { it.toObject(Post::class.java) }
                    offer(map)
                }
            awaitClose {
                userPost.remove()
            }
        }
    }

}