package com.example.socailapp.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socailapp.R
import com.example.socailapp.adapter.ConnectionAdapter
import com.example.socailapp.adapter.ConnectionInterface
import com.example.socailapp.adapter.OnConnectionClick
import com.example.socailapp.databinding.FragmentConnectionBinding
import com.example.socailapp.viewModel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectionFragment : Fragment(R.layout.fragment_connection), ConnectionInterface {

    private lateinit var binding: FragmentConnectionBinding
    private val connectionAdapter = ConnectionAdapter(this)
    private val userViewModel = UserViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConnectionBinding.bind(view)

        binding.apply {
            connectionRV.apply {
                adapter = connectionAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

        userViewModel.connectionRequest.observe(viewLifecycleOwner) {
            connectionAdapter.submitList(it)
            connectionAdapter.notifyDataSetChanged()
        }
    }

    override fun acceptRequest(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.acceptConnectionRequest(uid)
        }
    }

    override fun declineRequest(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.declineConnectionRequest(uid)
        }
    }
}