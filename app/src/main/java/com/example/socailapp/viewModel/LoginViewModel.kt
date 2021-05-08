package com.example.socailapp.viewModel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socailapp.UserDao
import com.example.socailapp.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class LoginViewModel @ViewModelInject constructor(private val userDao: UserDao): ViewModel() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userCollection = firebaseDB.collection("users")

    fun addUserDB(user: User) = viewModelScope.launch {
        userDao.addUser(user)
        Log.i("Login ViewModel", "INSERT")
    }

    fun addUser(user: User) = viewModelScope.launch {
        Log.i("Login ViewModel", "ADD User")
        val userRef = userCollection.document(user.id).get().await()
        if (userRef.data != null) {
            Log.i("Login ViewModel", "User Data Not Null")
            return@launch
        } else {
            Log.i("Login ViewModel", "User Data Null")
            userCollection.document(user.id).set(user)
        }
    }

}