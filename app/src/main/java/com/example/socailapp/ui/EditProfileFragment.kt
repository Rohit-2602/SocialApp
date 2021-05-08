package com.example.socailapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.data.User
import com.example.socailapp.databinding.FragmentEditProfileBinding
import com.example.socailapp.viewModel.PostViewModel
import com.example.socailapp.viewModel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var binding: FragmentEditProfileBinding
    private val navArgs: EditProfileFragmentArgs by navArgs()
    private val userViewModel = UserViewModel()
    private val RC_IMAGE_PICK = 0
    private var imageUri: Uri? = null
    private val postViewModel = PostViewModel()
    private var imageUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProfileBinding.bind(view)

        imageUrl = navArgs.User.imageURL

        binding.apply {
            Glide.with(requireContext()).load(imageUrl).into(profileIV)
            usernameTV.setText(navArgs.User.name)
            descriptionTV.setText(navArgs.User.description)

            saveButton.setOnClickListener {
                usernameTV.isEnabled = false
                descriptionTV.isEnabled = false
                saveButton.isEnabled = false
                chooseImageButton.isEnabled = false
                CoroutineScope(Dispatchers.IO).launch {
                    val currentUserId = FirebaseService().currentUser!!.uid
                    val user = UserViewModel().getUserById(currentUserId)!!
                    if (imageUri != null) {
                        imageUrl = postViewModel.getImageDownloadUrl(imageUri)
                    }
                    val newUser = User(
                        name = usernameTV.text.toString(),
                        lowercaseName = usernameTV.text.toString().toLowerCase(Locale.ROOT),
                        id = currentUserId,
                        description = descriptionTV.text.toString(),
                        imageURL = imageUrl!!,
                        connectionRequests = user.connectionRequests
                    )
                    userViewModel.updateUser(newUser = newUser)
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, "Changes Saved", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            chooseImageButton.setOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).also {
                    it.type = "image/*"
                    startActivityForResult(it, RC_IMAGE_PICK)
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_IMAGE_PICK) {
            data?.data.let {
                binding.profileIV.setImageURI(it)
                imageUri = it
                postViewModel.uploadImage(it)
//                imageUrl = postViewModel.getImageDownloadUrl(it)
            }
        }
    }

}