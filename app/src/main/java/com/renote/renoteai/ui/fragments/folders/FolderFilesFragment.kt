package com.renote.renoteai.ui.fragments.folders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.renote.renoteai.R
import com.renote.renoteai.databinding.FolderFilesFragmentDataBinding
import com.renote.renoteai.ui.fragments.folders.viewmodel.FolderFilesViewModel
import com.renote.renoteai.ui.main.MainActivity
import com.renote.renoteai.ui.presentation.home.adapters.DocumentsDetailsAdapter
import org.koin.android.ext.android.inject


class FolderFilesFragment : Fragment() {

    private var binding: FolderFilesFragmentDataBinding? = null
    private val viewModel: FolderFilesViewModel by inject()
    private var mContext: Context? = null
    private var folderId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = activity
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_folder_files, container, false)
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)







        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.img_search)
        drawable?.setBounds(0, 0, 40, 40) // Set the desired width and height
        binding?.etSearch?.setCompoundDrawables(drawable, null, null, null)

        val sharedPreference = requireContext().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        folderId = sharedPreference.getString("folderId", "100")
        val folderName =sharedPreference.getString("folderName", "ReNoteAI")
        println("folderId:$folderId")
        println("folderName:$folderName")
        initFolderFilesRecyclerview()
        folderDocumentsObserveData()

        handleBackPress()

        val tvFolderName = view.findViewById<TextView>(R.id.tvFolderName) // Use 'requireView().findViewById' if you're in a Fragment
        tvFolderName.setText(folderName)

        binding!!.tvBreadcrumbs.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }


    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Update shared preferences before navigating back or to any specific logic
                val sharedPreference = requireContext().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                with(sharedPreference.edit()) {
                    putString("folderId", "100")
                    putString("folderName", "ReNoteAI")
                    apply() // apply() is asynchronous, use commit() if you need synchronous operation
                }

                // Option 1: Navigate back by popping the back stack
                isEnabled = false
                requireActivity().onBackPressed()

                // Option 2: Directly navigate to another activity (uncomment if needed)
                // val intent = Intent(requireActivity(), MainActivity::class.java)
                // startActivity(intent)
            }
        })
    }

    private fun folderDocumentsObserveData() {
        viewModel.getFolderDocumentsDetails(folderId.toString())
        viewModel.folderDocumentsDetailsList.observe(viewLifecycleOwner) { documents ->
            if (documents.isNotEmpty()) {
                showEmpty(false)
                viewModel.documentsDetailsAdapter?.submitList(documents)
                viewModel.documentsDetailsAdapter?.notifyDataSetChanged()
            } else {
                showEmpty(true)
            }
        }
    }

    private fun showEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding?.folderFilesRecyclerview?.visibility = View.GONE
            binding?.emptyIV?.visibility = View.VISIBLE
            binding?.emptyTv?.visibility = View.VISIBLE
            binding?.empty2Tv?.visibility = View.VISIBLE
        } else {
            binding?.folderFilesRecyclerview?.visibility = View.VISIBLE
            binding?.emptyIV?.visibility = View.GONE
            binding?.emptyTv?.visibility = View.GONE
            binding?.empty2Tv?.visibility = View.GONE
        }
    }

    private fun initFolderFilesRecyclerview() {
        viewModel.documentsDetailsAdapter = DocumentsDetailsAdapter(requireContext())
        val layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        binding?.folderFilesRecyclerview?.layoutManager = layoutManager
        binding?.folderFilesRecyclerview?.isNestedScrollingEnabled = false
        binding?.folderFilesRecyclerview?.adapter = viewModel.documentsDetailsAdapter
    }
}


