package com.example.socailapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socailapp.R
import com.example.socailapp.adapter.OnConnectionClick
import com.example.socailapp.adapter.SearchAdapter
import com.example.socailapp.databinding.FragmentSearchBinding
import com.example.socailapp.viewModel.SearchViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.toolbar_search.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SearchFragment : Fragment(R.layout.fragment_search), OnConnectionClick {

    private lateinit var binding: FragmentSearchBinding
    private val searchAdapter = SearchAdapter(this)
    private val searchViewModel = SearchViewModel()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        hideMainToolbar()

        binding.apply {
            searchRV.apply {
                adapter = searchAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText != null) {
                        searchViewModel.searchQuery.value = newText
                    }
                    if (newText == "") {
                        searchViewModel.searchQuery.value = "#"
                    }
                    searchAdapter.notifyDataSetChanged()
                    return true
                }
            })

            backBTN.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        searchViewModel.searchedUser.observe(viewLifecycleOwner) {
            searchAdapter.submitList(it)
            searchAdapter.notifyDataSetChanged()
        }

    }

    private fun hideMainToolbar() {
        val mainActivityLayout =
            requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)

        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.GONE
    }

    override fun connect(userId: String) {
        searchViewModel.sendRequest(userId)
        Snackbar.make(binding.root, "Connection Request Sent", Snackbar.LENGTH_SHORT)
    }

    override fun seeUserProfile(userId: String) {
        if (userId == searchViewModel.currentUserId) {
            findNavController().navigate(R.id.profileFragment)
        }
        else {
            val action = SearchFragmentDirections.actionSearchFragmentToUserProfileFragment(userId)
            findNavController().navigate(action)
        }
    }
}