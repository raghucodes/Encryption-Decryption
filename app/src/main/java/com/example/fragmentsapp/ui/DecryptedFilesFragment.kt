package com.example.fragmentsapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.fragmentsapp.BuildConfig
import com.example.fragmentsapp.encryptdecrypt.Helper
import com.example.fragmentsapp.R
import com.example.fragmentsapp.viewmodel.DecryptedFilesViewModel
import java.io.File

class DecryptedFilesFragment: Helper() {
    lateinit var arrayAdapter: ArrayAdapter<String>
    lateinit  var mListView: ListView
    private var clickPosition:Int = 0
    private lateinit var viewModel : DecryptedFilesViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_decryptedfiles,container,false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListView = view.findViewById(R.id.decryptedFilesList)
        viewModel = ViewModelProvider(this).get(DecryptedFilesViewModel::class.java)

        viewModel.getDecryptedFileNames().observe(viewLifecycleOwner, Observer {
            showListView(it)
        })
        viewModel.getDecryptedFilePaths().observe(viewLifecycleOwner, Observer {
            mListView.setOnItemClickListener { parent, view, position, id ->
                openFile(it[clickPosition])
            }
        })
//        val file = File(requireContext().filesDir.absolutePath+"/DecryptedFiles")
//        searchDirectory(file)
//        showListView()
//
//        mListView.setOnItemClickListener { _, _, position, _ ->
//            clickPosition = position
//           // openFile(decryptedFilePaths[clickPosition])
//        }
    }
    private fun showListView(list: ArrayList<String>){
        arrayAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, list)
        mListView.adapter = arrayAdapter
    }

    private fun openFile(path: String){
        try {
            //  var element = arrayAdapter.getItem(position)
            val file = File(path)
            val intent = Intent(Intent.ACTION_VIEW)
            var uri: Uri = FileProvider.getUriForFile(requireContext(),
                BuildConfig.APPLICATION_ID +".provider",file)
            intent.setDataAndType(uri, getMimeType(path))
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        }catch (e: Exception){
            Toast.makeText(requireContext(), ""+ e, Toast.LENGTH_LONG).show()
        }
    }

    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}