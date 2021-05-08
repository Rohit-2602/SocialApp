package com.example.socailapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socailapp.R
import com.example.socailapp.adapter.ProfileActivityAdapter
import com.example.socailapp.adapter.ProfileActivityItemClick
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.example.socailapp.databinding.FragmentProfileBinding
import com.example.socailapp.viewModel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.toolbar_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile), ProfileActivityItemClick {

    private lateinit var binding: FragmentProfileBinding
    private val BACKGROUND_IMAGE_PICK = 0
    private lateinit var currentDBUser: User
    private lateinit var currentFirebaseUser: User
    private lateinit var profileActivityAdapter : ProfileActivityAdapter
    private val profileViewModel: ProfileViewModel by viewModels()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        hideMainToolbar()

        profileViewModel.currentDBUser.observe(viewLifecycleOwner) {
            currentDBUser = it[0]
            updateViews(currentDBUser)
        }

        profileViewModel.currentFirebaseUser.observe(viewLifecycleOwner) {
            currentFirebaseUser = it
            Log.i("Profile", currentFirebaseUser.toString())
            setUpRecyclerview()
        }

        profileViewModel.userActivity.observe(viewLifecycleOwner) {
            if (it.size >= 3) profileActivityAdapter.submitList(it.subList(0, 3))
            else profileActivityAdapter.submitList(it)

            profileActivityAdapter.notifyDataSetChanged()
        }

        binding.apply {
            editBTN.setOnClickListener {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment(currentDBUser)
                findNavController().navigate(action)
            }
            allActivityBTN.setOnClickListener {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToAllActivityFragment(currentDBUser)
                findNavController().navigate(action)
            }
            chooseBackgroundImageBTN.setOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).also {
                    it.type = "image/*"
                    startActivityForResult(it, BACKGROUND_IMAGE_PICK)
                }
            }
            signoutBTN.setOnClickListener {
                profileViewModel.signOut(
                    getString(R.string.default_web_client_id),
                    requireContext()
                )
                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
                Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }
    }

    override fun deletePost(postId: String) {
        profileViewModel.deletePost(postId)
    }

    private fun setUpRecyclerview() {
        profileActivityAdapter = ProfileActivityAdapter(this, currentFirebaseUser)
        binding.activityRV.apply {
            adapter = profileActivityAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun updateViews(currentUser: User) {
        binding.apply {
            profilenameTV.text = currentUser.name
            Glide.with(requireContext()).load(currentUser.imageURL).circleCrop().into(profileIV)
            Glide.with(requireContext()).load(Uri.parse(currentUser.backgroundImage))
                .into(backgroundIV)
            descriptionTV.text = currentUser.description
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == BACKGROUND_IMAGE_PICK) {
            data?.data.let {
                binding.backgroundIV.setImageURI(it)
                profileViewModel.updateUserBackgroundImage(currentDBUser.id, it.toString())
                if (it != null) {
                    profileViewModel.uploadImage(it)
                    CoroutineScope(Dispatchers.IO).launch {
                        val downloadUrl = profileViewModel.getImageDownloadUrl(it)
                        profileViewModel.currentUserCollection.update("backgroundImage", downloadUrl)
                    }
                }
            }
        }
    }

    override fun onLikeClicked(postId: String) {
        profileViewModel.updateLikes(postId)
        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
    }

    override fun comment(post: Post) {
        val action = ProfileFragmentDirections.actionProfileFragmentToCommentFragment(post)
        findNavController().navigate(action)
    }

    private fun hideMainToolbar() {
        val mainActivityLayout =
            requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)

        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.GONE
    }

}