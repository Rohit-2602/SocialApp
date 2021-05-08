package com.example.socailapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    val currentUserId = Firebase.auth.currentUser!!.uid
    private val allPostCollection = firebaseDB.collection("allPosts")
    private val userCollection = firebaseDB.collection("users")
    private val userPostCollection = firebaseDB.collection("posts")
    private val commentCollection = firebaseDB.collection("comments")

    @ExperimentalCoroutinesApi
    val allPosts = getAllPosts().asLiveData()

    private suspend fun getPostById(postId: String): Post {
        return allPostCollection.document(postId).get().await().toObject(Post::class.java)!!
    }

    fun deletePost(postId: String) = viewModelScope.launch {
        allPostCollection.document(postId).delete()
        userPostCollection.document(currentUserId).collection("user_post").document(postId).delete()
        commentCollection.document(postId).collection("post_comment")
            .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    cancel(cause = firebaseFirestoreException, message = "Error Fetching All Posts")
                    return@addSnapshotListener
                }
                querySnapshot!!.documents.forEach {
                    it.reference.delete()
                }
        }
    }

    suspend fun getUserById(userId: String): User {
        Log.i("Home ViewModel", "Get User By ID")
        return userCollection.document(userId).get().await().toObject(User::class.java)!!
    }

    @ExperimentalCoroutinesApi
    fun getAllPosts(): Flow<List<Post>> {
        return callbackFlow {
            val posts = allPostCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(cause = firebaseFirestoreException, message = "Error Fetching All Posts")
                        return@addSnapshotListener
                    }
                    val map = querySnapshot!!.documents.mapNotNull { it.toObject(Post::class.java) }
                    offer(map)
                }
            awaitClose {
                posts.remove()
            }
        }
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

}