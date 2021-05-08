package com.example.socailapp.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socailapp.UserDao
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileViewModel @ViewModelInject constructor(private val userDao: UserDao) : ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    val currentUserId = Firebase.auth.currentUser!!.uid
    private val imageCollection = Firebase.storage.reference
    private val userCollection = firebaseDB.collection("users")
    val currentUserCollection = userCollection.document(currentUserId)
    private val currentUserPostCollection =
        firebaseDB.collection("posts").document(currentUserId).collection("user_post")
    private val userPostCollection = firebaseDB.collection("posts")
    private val allPostCollection = firebaseDB.collection("allPosts")
    private val commentCollection = firebaseDB.collection("comments")

    val currentDBUser = userDao.getCurrentUser(currentUserId).asLiveData()

    @ExperimentalCoroutinesApi
    val currentFirebaseUser = getUserById(currentUserId).asLiveData()

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
    val userActivity = getUserActivity().asLiveData()

    @ExperimentalCoroutinesApi
    fun getUserActivity(): Flow<List<Post>> {
        return callbackFlow {
            val activity = currentUserPostCollection
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(
                            cause = firebaseFirestoreException,
                            message = "Error Fetching Activity"
                        )
                        return@addSnapshotListener
                    }
                    val map = querySnapshot!!.documents.mapNotNull { it.toObject(Post::class.java) }
                    offer(map)
                }
            awaitClose {
                activity.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getUserById(userId: String): Flow<User> {
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

    fun updateUserBackgroundImage(userId: String, backgroundImage: String) =
        CoroutineScope(Dispatchers.IO).launch {
            userDao.updateUserBackgroundImage(userId, backgroundImage)
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

    fun signOut(webClientId: String, context: Context) {
        val googleSignInClient = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val signInClient = GoogleSignIn.getClient(context, googleSignInClient)
        signInClient.signOut()
        Firebase.auth.signOut()
    }

}