package com.renote.renoteai.ui.fragments.folders

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.gson.Gson
import com.renote.renoteai.R
import com.renote.renoteai.database.custom_models.DocumentsContainer
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.databinding.FragmentFolderScreenBinding
import com.renote.renoteai.ui.presentation.home.adapters.DocumentsDetailsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class FolderScreenFragment : Fragment() {

    private var _binding: FragmentFolderScreenBinding? = null
    private val binding get() = _binding!!

    var mContext: Context? = null
    var mDrive: Drive? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFolderScreenBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.img_search)
        drawable?.setBounds(0, 0, 40, 40) // Set the desired width and height
        binding?.etSearch?.setCompoundDrawables(drawable, null, null, null)


    }

}