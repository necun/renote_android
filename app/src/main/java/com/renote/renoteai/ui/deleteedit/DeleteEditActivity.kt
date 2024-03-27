package com.renote.renoteai.ui.deleteedit

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.databinding.DeleteEditActivityBinding
import com.renote.renoteai.databinding.EditActivityDataBinding
import com.renote.renoteai.ui.activities.cropedit.CropEditActivity
import com.renote.renoteai.ui.activities.edit.EditActivity
import com.renote.renoteai.ui.activities.edit.viewmodel.EditViewModel
import com.renote.renoteai.ui.deleteedit.viewmodel.DeleteEditViewModel
import org.koin.android.ext.android.inject

class DeleteEditActivity : AppCompatActivity() {
    private lateinit var binding: DeleteEditActivityBinding
    private val viewModel: DeleteEditViewModel by inject()
    var fileId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@DeleteEditActivity, R.layout.activity_delete_edit
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        supportActionBar?.hide()
        val fileName = intent.getStringExtra("fileName")
        fileId = intent.getStringExtra("fileId")
        val fileUri = intent.getStringExtra("fileUri")
        Glide.with(this@DeleteEditActivity)
            .load(Uri.parse(fileUri))
            .into(binding.previewImgg)
        observeData()
    }

    fun observeData() {
        viewModel.resourseClick.observe(this) { integer ->
            when (integer) {
                R.id.deleteBtn -> {
                    if (fileId != null) {
                        viewModel.deleteFileByFileId(fileId!!)
                        deleteFileEntity(this@DeleteEditActivity,fileId!!)
                        val intent = Intent(this@DeleteEditActivity, EditActivity::class.java)

                        startActivity(intent)
                    }else{
                        Toast.makeText(this@DeleteEditActivity,"Error in deleting file, please try again after sometime",
                            Toast.LENGTH_LONG).show()
                    }


                }
                R.id.cancelBtn -> {

                        val intent = Intent(this@DeleteEditActivity, EditActivity::class.java)

                        startActivity(intent)



                }
            }

        }
    }


    fun deleteFileEntity(context: Context, fileId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val editor = prefs.edit()

        // Retrieve the current list of FileEntity objects
        val fileEntitiesJson = prefs.getString(FILE_ENTITIES_KEY, null)
        if (fileEntitiesJson != null) {
            // Convert the JSON string to a mutable list of FileEntity objects
            val fileEntities = gson.fromJson(fileEntitiesJson, Array<FileEntity>::class.java).toMutableList()

            // Find and remove the FileEntity with the given ID
            val iterator = fileEntities.iterator()
            while (iterator.hasNext()) {
                val fileEntity = iterator.next()
                if (fileEntity.id == fileId) {
                    iterator.remove()
                    break
                }
            }

            // Convert the updated list back to JSON
            val updatedFileEntitiesJson = gson.toJson(fileEntities)

            // Save the updated JSON string back into SharedPreferences
            editor.putString(FILE_ENTITIES_KEY, updatedFileEntitiesJson).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val FILE_ENTITIES_KEY = "fileEntities"
    }
}