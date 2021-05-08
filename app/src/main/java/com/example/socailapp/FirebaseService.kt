package com.example.socailapp

import android.net.Uri
import android.util.Log
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.google.firebase.auth.FirebaseUser
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
import java.util.*
import kotlin.collections.ArrayList

class FirebaseService {

    private val auth = Firebase.auth
    val currentUser: FirebaseUser? = auth.currentUser
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userCollection = firebaseDB.collection("users")
    val userPostCollection = firebaseDB.collection("posts")
    val allPostCollection = firebaseDB.collection("allPosts")
    private val imageCollection = Firebase.storage.reference

    suspend fun getCurrentUser(): User? {
        Log.i("FIREBASE SERVICE", "GET CURRENT USER")
        return if (currentUser != null) {
            getUserById(currentUser.uid)
        } else {
            null
        }
    }

    // Checking if User is already present in database (restoring its fields)
    fun addUser(user: User) = CoroutineScope(Dispatchers.IO).launch {
        val userRef = userCollection.document(user.id).get().await()
        if (userRef.data != null) {
            return@launch
        } else {
            userCollection.document(user.id).set(user)
        }
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

    suspend fun updateUser(newUser: User) {
        userCollection.document(currentUser!!.uid).set(newUser).await()
    }

    suspend fun addPost(post: Post) {
        val ref = allPostCollection.document()
        val id = ref.id
        post.id = id
        userPostCollection.document(currentUser!!.uid).collection("user_post").document(post.id)
            .set(post).await()
        allPostCollection.document(id).set(post).await()
    }

    fun updateLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = currentUser!!.uid
            val post = getPostById(postId)!!
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
    fun getUserPost(uid: String): Flow<List<Post>> {
        return callbackFlow {
            val posts = userPostCollection
                .document(uid)
                .collection("user_post")
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(message = "Error Fetching Posts", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot!!.documents.mapNotNull { it.toObject(Post::class.java) }
                    offer(map)
                }
            awaitClose {
                Log.d("Posts", "Cancelling Post Listener")
                posts.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getAllPost(): Flow<List<Post>> {
        Log.i("FIREBASE SERVICE", "GET ALL POST")
        return callbackFlow {
            val posts = allPostCollection
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(message = "Error Fetching Posts", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot!!.documents.mapNotNull { it.toObject(Post::class.java) }
                    offer(map)
                }
            awaitClose {
                Log.d("Posts", "Cancelling Post Listener")
                posts.remove()
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getUserSearch(query: String): Flow<List<User>> {
        return callbackFlow {
            val users = userCollection
                .orderBy("lowercaseName")
                .startAt(query.toLowerCase(Locale.ROOT))
                .endAt(query.toLowerCase(Locale.ROOT) + "\uf8ff")
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(message = "Error Fetching Posts", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot!!.documents.mapNotNull { it.toObject(User::class.java) }
                    offer(map)
                }
            awaitClose {
                Log.d("FIREBASESERVICE", "Cancelling Get Search Listener")
                users.remove()
            }
        }
    }

    suspend fun uploadImage(imageUri: Uri) {
        try {
            val path = imageUri.toString()
            imageCollection.child("${currentUser!!.uid}/images/$path").putFile(imageUri).await()
        } catch (e: Exception) {
            Log.i("FirebaseService", e.message.toString())
        }

    }

    suspend fun getImageDownloadUrl(imageUri: Uri?): String {
        Log.i("FIREBASE SERVICE", "GET DOWNLOAD IMAGE URL")
        val path = imageUri.toString()
        return imageCollection.child("${currentUser!!.uid}/images/$path").downloadUrl.await()
            .toString()
    }

    private suspend fun getPostById(postId: String): Post? {
        Log.i("FIREBASE SERVICE", "GET POST BY ID")
        return allPostCollection.document(postId).get().await().toObject(Post::class.java)
    }

    suspend fun sendConnectionRequest(userId: String) {
        val user = getUserById(userId)!!
        if (user.connectionRequests.contains(currentUser!!.uid)) {
            Log.i("Connection Request", "Request Already Sent")
        }
        else {
            user.connectionRequests.add(currentUser.uid)
            userCollection.document(userId).set(user)
        }
    }

    @ExperimentalCoroutinesApi
    fun getConnectionRequest(): Flow<List<String>> {
        return callbackFlow {
            val connectionRequest = userCollection.document(currentUser!!.uid)
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(message = "Error Fetching", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val connectionRequest = documentSnapshot?.get("connectionRequests") as ArrayList<String>
                    offer(connectionRequest)
                }
            awaitClose {
                connectionRequest.remove()
            }
        }
    }

    suspend fun acceptConnectionRequest(userId: String) {
        val user = getCurrentUser()!!
        user.mutualConnections.add(userId)
        user.connectionRequests.remove(userId)
        userCollection.document(currentUser!!.uid).set(user)
    }

    suspend fun declineConnectionRequest(userId: String) {
        val user = getCurrentUser()!!
        user.connectionRequests.remove(userId)
        userCollection.document(currentUser!!.uid).set(user)
    }

}