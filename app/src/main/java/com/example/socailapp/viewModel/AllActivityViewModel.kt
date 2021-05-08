package com.example.socailapp.viewModel

import android.util.Log
import androidx.lifecycle.*
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

class AllActivityViewModel : ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    val currentUserId = Firebase.auth.currentUser!!.uid
    private val userCollection = firebaseDB.collection("users")
    private val userPostCollection = firebaseDB.collection("posts")
    private val allPostCollection = firebaseDB.collection("allPosts")
    private val commentCollection = firebaseDB.collection("comments")

    @ExperimentalCoroutinesApi
    fun getUserPost(userId: String): LiveData<List<Post>> {
        return getUserPosts(userId).asLiveData()
    }

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

    @ExperimentalCoroutinesApi
    fun getCurrentUserById(userId: String) : LiveData<User> {
        return getUserById(userId).asLiveData()
    }

    @ExperimentalCoroutinesApi
    fun getUserById(userId: String): Flow<User> {
        Log.i("All Activity", "Get User By ID")
        return callbackFlow {
            val user = userCollection.document(userId)
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(
                            cause = firebaseFirestoreException,
                            message = "Error Fetching All Posts"
                        )
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

    fun updateLikes(postId: String) = viewModelScope.launch {
        val post = getPostById(postId)

        if (post.likedBy.contains(currentUserId)) {
            post.likedBy.remove(currentUserId)
        } else {
            post.likedBy.add(currentUserId)
        }
        userPostCollection.document(post.creatorId).collection("user_post").document(postId)
            .set(post)
        allPostCollection.document(postId).set(post)
    }

    @ExperimentalCoroutinesApi
    fun getUserPosts(userId: String): Flow<List<Post>> {
        return callbackFlow {
            val posts = userPostCollection.document(userId).collection("user_post")
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(
                            cause = firebaseFirestoreException,
                            message = "Error Fetching User Post"
                        )
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

}