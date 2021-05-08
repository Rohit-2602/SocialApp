package com.example.socailapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.socailapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.ArrayList

class ConnectionViewModel: ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val userCollection = firebaseDB.collection("users")
    private val currentUserCollection = firebaseDB.collection("users").document(currentUserId)

    @ExperimentalCoroutinesApi
    val connectionRequest = getConnectionRequest().asLiveData()

    @ExperimentalCoroutinesApi
    fun getConnectionRequest(): Flow<List<String>> {
        return callbackFlow {
            val connectionRequest = currentUserCollection
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(message = "Error Fetching", cause = firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val connectionRequest = documentSnapshot?.get("incomingRequest") as ArrayList<String>
                    offer(connectionRequest)
                }
            awaitClose {
                connectionRequest.remove()
            }
        }
    }

    suspend fun getUserById(userId: String): User {
        return userCollection.document(userId).get().await().toObject(User::class.java)!!
    }

    suspend fun acceptConnectionRequest(userId: String) {
        val user = currentUserCollection.get().await().toObject(User::class.java)!!
        user.connections.add(userId)
        user.incomingRequest.remove(userId)
        val requester = getUserById(userId)
        requester.connections.add(user.id)
        requester.connectionRequests.remove(user.id)
        userCollection.document(currentUserId).set(user)
        userCollection.document(userId).set(requester)
    }

    suspend fun declineConnectionRequest(userId: String) {
        val user = currentUserCollection.get().await().toObject(User::class.java)!!
        val requester = getUserById(userId)
        user.incomingRequest.remove(userId)
        requester.connectionRequests.remove(user.id)
        userCollection.document(userId).set(requester)
        userCollection.document(currentUserId).set(user)
    }

}