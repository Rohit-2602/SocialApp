package com.example.socailapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socailapp.R
import com.example.socailapp.adapter.AllActivityAdapter
import com.example.socailapp.adapter.AllActivityItemClick
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.example.socailapp.databinding.FragmentAllActivityBinding
import com.example.socailapp.viewModel.AllActivityViewModel
import kotlinx.android.synthetic.main.toolbar_all_activity.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class AllActivityFragment : Fragment(R.layout.fragment_all_activity), AllActivityItemClick {

    private lateinit var binding: FragmentAllActivityBinding
    private val navArgs : AllActivityFragmentArgs by navArgs()
    private val allActivityViewModel = AllActivityViewModel()
    private lateinit var currentUser: User
    private lateinit var allActivityAdapter: AllActivityAdapter

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAllActivityBinding.bind(view)

        val userUID = navArgs.User.id

        binding.apply {
            backBTN.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        allActivityViewModel.getCurrentUserById(userUID).observe(viewLifecycleOwner) {
            currentUser = it
            setupRecyclerview()
        }

        allActivityViewModel.getUserPost(userUID).observe(viewLifecycleOwner) {
            allActivityAdapter.submitList(it)
            allActivityAdapter.notifyDataSetChanged()
        }
    }

    override fun deletePost(postId: String) {
        allActivityViewModel.deletePost(postId)
    }

    private fun setupRecyclerview() {
        allActivityAdapter = AllActivityAdapter(this, currentUser)
        binding.apply {
            allActivityRV.apply {
                adapter = allActivityAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }

    override fun commentOnPost(post: Post) {
        val action = AllActivityFragmentDirections.actionAllActivityFragmentToCommentFragment(post)
        findNavController().navigate(action)
    }

    override fun onLikeClicked(postId: String) {
        allActivityViewModel.updateLikes(postId)
        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
    }
}