package com.example.socailapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.socailapp.R
import com.example.socailapp.data.User
import com.example.socailapp.databinding.FragmentEditProfileBinding
import com.example.socailapp.viewModel.EditProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.toolbar_edit_intro.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var binding: FragmentEditProfileBinding
    private val navArgs: EditProfileFragmentArgs by navArgs()
    private val RC_IMAGE_PICK = 0
    private var imageUri: Uri? = null
    private var imageUrl: String? = null
    private val editProfileViewModel: EditProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProfileBinding.bind(view)

        val currentUser = navArgs.User
        imageUrl = currentUser.imageURL
        enableViews()

        hideMainToolbar()

        binding.apply {
            Glide.with(requireContext()).load(imageUrl).circleCrop().into(chooseImageButton)
            usernameTV.setText(currentUser.name)
            descriptionTV.setText(currentUser.description)

            saveButton.setOnClickListener {
                disableViews()
                CoroutineScope(Dispatchers.IO).launch {
                    if (imageUri != null) {
                        imageUrl = editProfileViewModel.getImageDownloadUrl(imageUri!!)
                    }
                    if (usernameTV.text.toString().trim() == "") {
                        Snackbar.make(binding.root, "Username Can't Be Empty", Snackbar.LENGTH_SHORT).show()
                        return@launch
                    } else {
                        currentUser.name = usernameTV.text.toString().trim()
                    }
                    currentUser.lowercaseName = usernameTV.text.toString().trim().toLowerCase(Locale.ROOT)
                    currentUser.description = descriptionTV.text.toString().trim()
                    currentUser.imageURL = imageUrl!!

                    editProfileViewModel.updateFirebaseUser(
                        name = currentUser.name!!,
                        description = currentUser.description,
                        imageUrl = currentUser.imageURL
                    )
                    editProfileViewModel.updateDBUser(user = currentUser)
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, "Changes Saved", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }

            chooseImageButton.setOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).also {
                    it.type = "image/*"
                    startActivityForResult(it, RC_IMAGE_PICK)
                }
            }

            cancel_button.setOnClickListener {
                val newUser = User(
                    id = currentUser.id,
                    name = usernameTV.text.toString(),
                    lowercaseName = usernameTV.text.toString().toLowerCase(Locale.ROOT),
                    imageURL = imageUrl!!,
                    description = descriptionTV.text.toString()
                )
                if (newUser != currentUser) {
                    showAlertDialog()
                }
                else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun disableViews() {
        binding.apply {
            usernameTV.isEnabled = false
            descriptionTV.isEnabled = false
            saveButton.isEnabled = false
            chooseImageButton.isEnabled = false
            cancel_button.isEnabled = false
        }
    }

    private fun enableViews() {
        binding.apply {
            usernameTV.isEnabled = true
            descriptionTV.isEnabled = true
            saveButton.isEnabled = true
            chooseImageButton.isEnabled = true
            cancel_button.isEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_IMAGE_PICK) {
            data?.data.let {
                binding.chooseImageButton.setImageURI(it)
                imageUri = it
                if (it != null) {
                    editProfileViewModel.uploadImage(it)
                }
            }
        }
    }

    private fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage("You have unsaved changes. Do you want to discard them?")
            .setPositiveButton("Cancel") { _, _ ->
                Toast.makeText(requireContext(), "Clicked Cancel", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Discard") { _, _ ->
                findNavController().navigateUp()
            }
            .create()

        alertDialog.show()
    }

    private fun hideMainToolbar() {
        val mainActivityLayout =
            requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)

        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.GONE
    }

}