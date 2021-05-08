package com.example.socailapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socailapp.data.Comment
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommentViewModel: ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    val currentUserId = Firebase.auth.currentUser!!.uid
    private val userCollection = firebaseDB.collection("users")
    private val userPostCollection = firebaseDB.collection("posts")
    private val allPostCollection = firebaseDB.collection("allPosts")
    private val commentCollection = firebaseDB.collection("comments")

    @ExperimentalCoroutinesApi
    fun getPostComments(postId: String): LiveData<List<Comment>> {
        return getPostComment(postId).asLiveData()
    }

    fun deleteComment(commentId: String, postId: String) = viewModelScope.launch {
        commentCollection.document(postId).collection("post_comment").document(commentId).delete()
        val post = getPostById(postId)
        post.commentedBy.remove(currentUserId)
        allPostCollection.document(postId).set(post)
        userPostCollection.document(currentUserId).collection("user_post").document(postId).set(post)
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            Log.i("FIREBASE SERVICE", "GET USER BY ID")
            userCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            Log.i("FIREBASE SERVICE", e.message.toString())
            return null
        }
    }

    private suspend fun getPostById(postId: String): Post {
        Log.i("Comment ViewModel", "GET POST BY ID")
        return allPostCollection.document(postId).get().await().toObject(Post::class.java)!!
    }

    fun updateLikes(postId: String) {
        GlobalScope.launch {
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

    @ExperimentalCoroutinesApi
    fun getPostComment(postId: String): Flow<List<Comment>> {
        return callbackFlow {
            val comments = commentCollection.document(postId).collection("post_comment")
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreExcetion: FirebaseFirestoreException? ->
                    if (firebaseFirestoreExcetion != null) {
                        cancel(message = "Error Fetching Comments", cause = firebaseFirestoreExcetion)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot!!.documents.mapNotNull { it.toObject(Comment::class.java) }
                    offer(map)
                }
            awaitClose {
                comments.remove()
            }
        }
    }

    fun addComment(comment: Comment, postId: String, postCreatorId: String) = viewModelScope.launch {
        val ref = allPostCollection.document()
        val id = ref.id
        comment.id = id
        commentCollection.document(postId).collection("post_comment").document(comment.id).set(comment)
        val post = getPostById(postId)
        post.commentedBy.add(comment.creatorId)
        allPostCollection.document(postId).set(post)
        userPostCollection.document(postCreatorId).collection("user_post").document(postId).set(post)
    }

}