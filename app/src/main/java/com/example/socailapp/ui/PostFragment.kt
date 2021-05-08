package com.example.socailapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.FragmentPostBinding
import com.example.socailapp.viewModel.PostViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat

class PostFragment : Fragment(R.layout.fragment_post) {

    private lateinit var binding: FragmentPostBinding
    private val firebaseService = FirebaseService()
    private var imageUri: Uri? = null
    private val RC_IMAGE_PICK = 0
    private val postViewModel = PostViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPostBinding.bind(view)

        binding.apply {

            postButton.setOnClickListener {
                val text = postET.text.toString()
                val creationTime =
                    DateFormat.getDateTimeInstance().format(System.currentTimeMillis())
                CoroutineScope(Dispatchers.IO).launch {
//                    val currentUser = firebaseService.getCurrentUser()!!
                    if (imageUri != null) {
                        val post = Post(
                            text = text,
//                            creator = firebaseService.getUserById(currentUser.id)!!,
                            createdAt = creationTime,
                            creatorId = firebaseService.currentUser!!.uid,
                            image = postViewModel.getImageDownloadUrl(imageUri)
                        )
                        postViewModel.addPost(post)
                    }
                    else {
                        val post = Post(
                            text = text,
//                            creator = firebaseService.getUserById(currentUser.id)!!,
                            createdAt = creationTime,
                            creatorId = firebaseService.currentUser!!.uid,
                            image = null
                        )
                        postViewModel.addPost(post)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Post Created", Toast.LENGTH_SHORT).show()
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
}