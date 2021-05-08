package com.example.socailapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socailapp.FirebaseService
import com.example.socailapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel : ViewModel() {

    private val firebaseService = FirebaseService()

    val searchQuery = MutableStateFlow("#")

    @ExperimentalCoroutinesApi
    val connectionRequest = firebaseService.getConnectionRequest().asLiveData()

    @ExperimentalCoroutinesApi
    private val userQuery = searchQuery.flatMapLatest {
        firebaseService.getUserSearch(it)
    }

    fun sendConnectionRequest(userUid: String) = viewModelScope.launch {
        firebaseService.sendConnectionRequest(userUid)
    }

    @ExperimentalCoroutinesApi
    val searchedUser = userQuery.asLiveData()

    suspend fun getCurrentUser(): User {
        return firebaseService.getCurrentUser()!!
    }

    suspend fun getUserById(uid: String): User? = withContext(Dispatchers.IO) {
        return@withContext firebaseService.getUserById(uid)
    }

    suspend fun updateUser(newUser: User) {
        firebaseService.updateUser(newUser)
    }

    suspend fun acceptConnectionRequest(userUid: String) {
        firebaseService.acceptConnectionRequest(userUid)
    }

    suspend fun declineConnectionRequest(userUid: String) {
        firebaseService.declineConnectionRequest(userUid)
    }

}