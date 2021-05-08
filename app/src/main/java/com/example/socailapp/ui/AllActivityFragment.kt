package com.example.socailapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.adapter.HomeAdapter
import com.example.socailapp.adapter.OnClickListener
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.FragmentAllActivityBinding
import com.example.socailapp.viewModel.PostViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi

class AllActivityFragment : Fragment(R.layout.fragment_all_activity), OnClickListener {

    private lateinit var binding: FragmentAllActivityBinding
    private val postViewModel = PostViewModel()
//    private val homeAdapter = ProfileActivityAdapter(this)
    private val navArgs : AllActivityFragmentArgs by navArgs()

    private lateinit var postHomeAdapter: HomeAdapter
    private val firebaseService = FirebaseService()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAllActivityBinding.bind(view)

        val userUID = navArgs.User.id

        val postsCollections = firebaseService.userPostCollection.document(userUID).collection("user_post")
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        postHomeAdapter = HomeAdapter(recyclerViewOptions, this)

        binding.apply {
            allActivityRV.apply {
                adapter = postHomeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

//        postViewModel.userPost.observe(viewLifecycleOwner) {
//            homeAdapter.submitList(it)
//            homeAdapter.notifyDataSetChanged()
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