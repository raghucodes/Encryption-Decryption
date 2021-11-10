package com.example.fragmentsapp.ui


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.fragmentsapp.R
import com.example.fragmentsapp.encryptdecrypt.Helper
import com.example.fragmentsapp.viewmodel.AllFilesViewModel
import kotlin.system.measureTimeMillis


class AllFilesFragment: Helper() {
    var fileNames = ArrayList<String>()
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var clickedPosition: Int = 0
    private lateinit  var mListView: ListView
    private val doDelete = false
    lateinit var viewModel : AllFilesViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_allfiles,container,false)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mListView = view.findViewById(R.id.filesList)
        viewModel = ViewModelProvider(this).get(AllFilesViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAllFilePaths()?.observe(viewLifecycleOwner, Observer {
            mListView.setOnItemClickListener { _, _, position, _ ->
                clickedPosition = position
                    doAESEncryption(it[clickedPosition],doDelete)
                if(doDelete)
                    fileNames.removeAt(clickedPosition)
                arrayAdapter.notifyDataSetChanged()
            }
        })

        viewModel.getAllFileNames()?.observe(viewLifecycleOwner, Observer {
            fileNames = it
           showListView(fileNames)
        })
    }
    private fun showListView(list: ArrayList<String>) {
            arrayAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1, list)
            mListView.adapter = arrayAdapter
        }
}

