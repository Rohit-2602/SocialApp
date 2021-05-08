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
import java.util.*

class ManageConnectionViewModel: ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val userCollection = firebaseDB.collection("users")

    @ExperimentalCoroutinesApi
    val connections = getConnections().asLiveData()

    suspend fun getUserById(userId: String): User {
        return userCollection.document(userId).get().await().toObject(User::class.java)!!
    }

    @ExperimentalCoroutinesApi
    fun getConnections(): Flow<List<String>> {
        return callbackFlow {
            val connection = userCollection.document(currentUserId)
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel("Error Fetching Connections", firebaseFirestoreException)
                        return@addSnapshotListener
                    }
                    val map = documentSnapshot?.get("connections") as ArrayList<String>
                    offer(map)
                }
            awaitClose {
                connection.remove()
            }
        }
    }

    suspend fun removeConnection(userId: String) {
        val currentUser = getUserById(currentUserId)
        val userConnection = getUserById(userId)
        currentUser.connections.remove(userId)
        userConnection.connections.remove(currentUser.id)
        userCollection.document(currentUser.id).set(currentUser)
        userCollection.document(userId).set(userConnection)
    }

}