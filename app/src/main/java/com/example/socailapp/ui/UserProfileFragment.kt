package com.example.socailapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socailapp.R
import com.example.socailapp.adapter.UserProfileAdapter
import com.example.socailapp.adapter.UserProfileItemClick
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.example.socailapp.databinding.FragmentUserProfileBinding
import com.example.socailapp.viewModel.UserProfileViewModel
import kotlinx.android.synthetic.main.toolbar_user_profile.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class UserProfileFragment : Fragment(R.layout.fragment_user_profile), UserProfileItemClick {

    private val navArgs : UserProfileFragmentArgs by navArgs()
    private lateinit var binding : FragmentUserProfileBinding
    private var currentUser : User = User()
    private lateinit var userProfileAdapter : UserProfileAdapter
    private val userProfileViewModel = UserProfileViewModel()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)
        hideMainToolbar()

        val userUid = navArgs.UserUid
        userProfileViewModel.getCurrentUser(userUid).observe(viewLifecycleOwner) {
            currentUser = it
            setupRecyclerview()
            updateViews()
        }

        userProfileViewModel.userActivity(userUid).observe(viewLifecycleOwner) {
            if (it.size >= 3) userProfileAdapter.submitList(it.subList(0, 3))
            else userProfileAdapter.submitList(it)

            userProfileAdapter.notifyDataSetChanged()
        }

        binding.apply {
            allActivityBTN.setOnClickListener {
                val action =
                    UserProfileFragmentDirections.actionUserProfileFragmentToAllActivityFragment(currentUser)
                findNavController().navigate(action)
            }
        }

    }

    private fun setupRecyclerview() {
        userProfileAdapter = UserProfileAdapter(this, currentUser)
        binding.apply {
            activityRV.apply {
                adapter = userProfileAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateViews() {
        binding.apply {
            Glide.with(requireContext()).load(currentUser.imageURL).circleCrop().into(profileIV)
            profilenameTV.text = currentUser.name
            descriptionTV.text = currentUser.description
            Glide.with(requireContext()).load(currentUser.backgroundImage).into(backgroundIV)
            toolbar_title.text = "${currentUser.name}'s Profile"
            back_button.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun hideMainToolbar() {
        val mainActivityLayout = requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)

        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.GONE
    }

    override fun onLikeClicked(postId: String) {
        userProfileViewModel.updateLikes(postId)
        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
    }

    override fun commentOnPost(post: Post) {
        val action = UserProfileFragmentDirections.actionUserProfileFragmentToCommentFragment(post)
        findNavController().navigate(action)
    }

}