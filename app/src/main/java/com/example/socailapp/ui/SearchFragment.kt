package com.example.socailapp.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socailapp.R
import com.example.socailapp.adapter.OnConnectionClick
import com.example.socailapp.adapter.SearchAdapter
import com.example.socailapp.databinding.FragmentSearchBinding
import com.example.socailapp.viewModel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SearchFragment : Fragment(R.layout.fragment_search), OnConnectionClick {

    private val userViewModel = UserViewModel()
    private lateinit var binding: FragmentSearchBinding
    private val searchAdapter = SearchAdapter(this)

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        setHasOptionsMenu(true)

        binding.apply {
            searchRV.apply {
                adapter = searchAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

        userViewModel.searchedUser.observe(viewLifecycleOwner) {
            searchAdapter.submitList(it)
            searchAdapter.notifyDataSetChanged()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.searchFragment)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    userViewModel.searchQuery.value = newText
                }
                if (newText == "") {
                    userViewModel.searchQuery.value = "#"
                }
                searchAdapter.notifyDataSetChanged()
                return true
            }
        })
    }

    override fun connect(userId: String) {
        userViewModel.sendConnectionRequest(userId)
        Snackbar.make(binding.root, "Connection Request Sent", Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.bottomNavigationView)
    }
}