package com.example.socailapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socailapp.R
import com.example.socailapp.adapter.HomeAdapter
import com.example.socailapp.adapter.HomeItemClickListener
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.FragmentHomeBinding
import com.example.socailapp.viewModel.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

class HomeFragment : Fragment(R.layout.fragment_home), HomeItemClickListener {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel = HomeViewModel()
    private val homeAdapter = HomeAdapter(this)

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        showMainToolbar()

        homeViewModel.allPosts.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
            homeAdapter.notifyDataSetChanged()
        }

        binding.apply {
            recyclerview.apply {
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }

    override fun deletePost(postId: String) {
        homeViewModel.deletePost(postId)
    }

    private fun showMainToolbar() {
        val mainActivityLayout = requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)

        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.VISIBLE
    }

    @ExperimentalCoroutinesApi
    override fun onLikeClicked(postId: String) {
        homeViewModel.updateLikes(postId)
        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
    }

    override fun seeUserProfile(userUid: String) {
        if (userUid == homeViewModel.currentUserId) {
            findNavController().navigate(R.id.profileFragment)
        }
        else {
            val action = HomeFragmentDirections.actionHomeFragmentToUserProfileFragment(userUid)
            findNavController().navigate(action)
        }
    }

    override fun commentOnPost(post: Post) {
        val action = HomeFragmentDirections.actionHomeFragmentToCommentFragment(post)
        findNavController().navigate(action)
    }

}