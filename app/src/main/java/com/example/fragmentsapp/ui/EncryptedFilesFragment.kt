package com.example.fragmentsapp.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.fragmentsapp.R
import com.example.fragmentsapp.encryptdecrypt.Helper
import com.example.fragmentsapp.viewmodel.EncryptedFilesViewModel

class EncryptedFilesFragment: Helper() {

    lateinit var arrayAdapter: ArrayAdapter<String>
    lateinit  var mListView: ListView
    private var clickPosition:Int = 0
    private lateinit var viewModel : EncryptedFilesViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_encryptedfiles,container,false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mListView = view.findViewById(R.id.encryptedFilesList)
        viewModel = ViewModelProvider(this).get(EncryptedFilesViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)

        viewModel.getEncryptedFileNames().observe(viewLifecycleOwner, Observer {
            showListView(it)
        })
        viewModel.getEncryptedFilePaths().observe(viewLifecycleOwner, Observer {
            mListView.setOnItemClickListener { _, _, position, _ ->
                clickPosition = position
                doAESDecryption(it[clickPosition])
            }
        })
    }

    private fun showListView(list: ArrayList<String>){
        arrayAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, list)
        mListView.adapter = arrayAdapter
    }

}
