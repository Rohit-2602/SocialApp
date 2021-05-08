package com.example.socailapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.adapter.HomeAdapter
import com.example.socailapp.adapter.OnClickListener
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.FragmentHomeBinding
import com.example.socailapp.viewModel.PostViewModel
import com.example.socailapp.viewModel.UserViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi

class HomeFragment : Fragment(R.layout.fragment_home), OnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private val postViewModel = PostViewModel()
//    private lateinit var postAdapter : ProfileActivityAdapter
    private lateinit var postHomeAdapter : HomeAdapter
    private val firebaseService = FirebaseService()
    private val userViewModel = UserViewModel()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)


        userViewModel.connectionRequest.observe(viewLifecycleOwner) {
            Log.i("CONNECTION REQUEST", it.toString())
        }

//        postAdapter = ProfileActivityAdapter(this)
        val postsCollections = firebaseService.allPostCollection
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        postHomeAdapter = HomeAdapter(recyclerViewOptions, this)

        binding.apply {
            recyclerview.apply {
//                adapter = postAdapter
                adapter = postHomeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

//        postViewModel.allPosts.observe(viewLifecycleOwner) {
//            postAdapter.submitList(it)
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

    @ExperimentalCoroutinesApi
    override fun onLikeClicked(postId: String) {
        postViewModel.updateLike(postId)
//        postViewModel.allPosts.observe(viewLifecycleOwner) {
//            postAdapter.submitList(it)
//            postAdapter.notifyDataSetChanged()
//        }
//        postAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
    }

    override fun seeUserProfile(userUid: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToUserProfileFragment(userUid)
        findNavController().navigate(action)
    }

}