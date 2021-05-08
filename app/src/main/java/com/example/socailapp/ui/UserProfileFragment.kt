package com.example.socailapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.adapter.HomeAdapter
import com.example.socailapp.adapter.OnClickListener
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.example.socailapp.databinding.FragmentUserProfileBinding
import com.example.socailapp.viewModel.PostViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileFragment : Fragment(R.layout.fragment_user_profile), OnClickListener {

    private val navArgs : UserProfileFragmentArgs by navArgs()
    private val firebaseService = FirebaseService()
    private lateinit var binding : FragmentUserProfileBinding
    private lateinit var postAdapter : HomeAdapter
    private val postViewModel = PostViewModel()
    private lateinit var currentUser : User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)

        val userUid = navArgs.UserUid
        CoroutineScope(Dispatchers.IO).launch {
            currentUser = firebaseService.getUserById(userUid)!!

            withContext(Dispatchers.Main) {
                binding.apply {
                    Glide.with(requireContext()).load(currentUser.imageURL).circleCrop().into(profileIV)
                    profilenameTV.text = currentUser.name
                    descriptionTV.text = currentUser.description
                }
            }
        }

//        val postsCollections = firebaseService.allPostCollection.limit(3)
        val userPostCollection = firebaseService.userPostCollection.document(userUid).collection("user_post").limit(3)
        val query = userPostCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        postAdapter = HomeAdapter(recyclerViewOptions, this)

        binding.apply {
            activityRV.apply {
                adapter = postAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

        binding.allActivityBTN.setOnClickListener {
            val action =
                UserProfileFragmentDirections.actionUserProfileFragmentToAllActivityFragment(currentUser)
            findNavController().navigate(action)
        }

    }

    override fun onStart() {
        super.onStart()
        postAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        postAdapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postViewModel.updateLike(postId)
        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
    }

    override fun seeUserProfile(userUid: String) {

    }
}