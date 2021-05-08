package com.example.socailapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socailapp.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class SearchViewModel: ViewModel() {

    val currentUserId = Firebase.auth.currentUser!!.uid
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userCollection = firebaseDB.collection("users")
    fun sendRequest(userId: String) = viewModelScope.launch {
        sendConnectionRequest(userId)
    }

    suspend fun getCurrentUser() = getUserById(currentUserId)

    suspend fun getUserById(userId: String) : User {
        return userCollection.document(userId).get().await().toObject(User::class.java)!!
    }

    val searchQuery = MutableStateFlow("#")

    @ExperimentalCoroutinesApi
    private val userQuery = searchQuery.flatMapLatest {
        getUserSearch(it)
    }

    @ExperimentalCoroutinesApi
    val searchedUser = userQuery.asLiveData()

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
                Log.d("SearchViewModel", "Cancelling Get Search Listener")
                users.remove()
            }
        }
    }

    private suspend fun sendConnectionRequest(userId: String) {
        val user = getUserById(userId)
        val currentUser = getUserById(currentUserId)
        if (user.incomingRequest.contains(currentUser.id)) {
            Log.i("SearchViewModel", "Request Already Sent")
        } else {
            user.incomingRequest.add(currentUser.id)
            currentUser.connectionRequests.add(userId)
            userCollection.document(currentUser.id).set(currentUser)
            userCollection.document(userId).set(user)
        }
    }

}