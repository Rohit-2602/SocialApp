package com.example.socailapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.adapter.HomeAdapter
import com.example.socailapp.adapter.OnClickListener
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.example.socailapp.databinding.FragmentProfileBinding
import com.example.socailapp.viewModel.PostViewModel
import com.example.socailapp.viewModel.UserViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*

class ProfileFragment : Fragment(R.layout.fragment_profile), OnClickListener {

    private lateinit var binding: FragmentProfileBinding
    private val userViewModel = UserViewModel()
    private lateinit var currentUser: User
    private val postViewModel = PostViewModel()

    private lateinit var postHomeAdapter: HomeAdapter
    private val firebaseService = FirebaseService()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        CoroutineScope(Dispatchers.IO).launch {
            currentUser = userViewModel.getCurrentUser()
            withContext(Dispatchers.Main) {
                binding.apply {
                    profilenameTV.text = currentUser.name
                    descriptionTV.text = currentUser.description
                    Glide.with(requireContext()).load(currentUser.imageURL).circleCrop()
                        .into(profileIV)
                    editBTN.setOnClickListener {
                        val action =
                            ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment(
                                currentUser
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }

//        val postAdapter = ProfileActivityAdapter(this)

        val postsCollections = firebaseService.userPostCollection.document(firebaseService.currentUser!!.uid).collection("user_post").limit(3)
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        postHomeAdapter = HomeAdapter(recyclerViewOptions, this)

        binding.apply {
            activityRV.apply {
                adapter = postHomeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            allActivityBTN.setOnClickListener {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToAllActivityFragment(currentUser)
                findNavController().navigate(action)
            }
        }

//        postViewModel.userPost.observe(viewLifecycleOwner) {
//            if (it.size >= 3) {
//                postAdapter.submitList(it.subList(0, 3))
//                Log.i("USER POST LIST", it.toString())
//            } else {
//                postAdapter.submitList(it)
//                Log.i("USER POST LIST", it.toString())
//            }
//            postAdapter.notifyDataSetChanged()
//        }
    }

    override fun onStart() {
        super.onStart()
        postHomeAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        postHomeAdapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postViewModel.updateLike(postId)
        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
    }

    override fun seeUserProfile(userUid: String) {

    }

}