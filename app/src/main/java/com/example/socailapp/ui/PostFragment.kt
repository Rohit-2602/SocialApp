package com.example.socailapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.socailapp.R
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.FragmentPostBinding
import com.example.socailapp.viewModel.PostViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.toolbar_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostFragment : Fragment(R.layout.fragment_post) {

    private lateinit var binding: FragmentPostBinding
    private var imageUri: Uri? = null
    private val RC_IMAGE_PICK = 0
    private val postViewModel = PostViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPostBinding.bind(view)
        enableView()
        hideMainToolbar()

        binding.apply {
            cancel_button.setOnClickListener {
                findNavController().navigateUp()
            }
            postButton.setOnClickListener {
                disableView()
                val text = postET.text.toString().trim()
                if (text == "") {
                    Snackbar.make(postET, "Write Something", Snackbar.LENGTH_SHORT).show()
                    enableView()
                    return@setOnClickListener
                }
                CoroutineScope(Dispatchers.IO).launch {
                    val post = Post(
                        text = text,
                        createdAt = System.currentTimeMillis(),
                        creatorId = postViewModel.currentUserId,
                        image = postViewModel.getImageDownloadUrl(imageUri)
                    )
                    postViewModel.addPost(post)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Post Created", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }
            imageButton.setOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).also {
                    it.type = "image/*"
                    startActivityForResult(it, RC_IMAGE_PICK)
                }
            }
        }
    }

    private fun disableView() {
        binding.apply {
            imageButton.isEnabled = false
            postET.isEnabled = false
            postButton.isEnabled = false
        }
    }

    private fun enableView() {
        binding.apply {
            imageButton.isEnabled = true
            postET.isEnabled = true
            postButton.isEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_IMAGE_PICK) {
            data?.data.let {
                imageUri = it
                postViewModel.uploadImage(it)
                binding.postImage.setImageURI(it)
            }
        }
    }

    private fun hideMainToolbar() {
        val mainActivityLayout =
            requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)
        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.GONE
    }
}