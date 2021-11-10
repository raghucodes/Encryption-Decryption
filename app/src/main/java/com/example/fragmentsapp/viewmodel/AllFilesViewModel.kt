package com.example.fragmentsapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class AllFilesViewModel(application: Application) : AndroidViewModel(application) {

    private val file = File("/storage/emulated/0")
    var allFilesPaths =  MutableLiveData<ArrayList<String>>()
    var allFilesNames =  MutableLiveData<ArrayList<String>>()
    var fileNames = ArrayList<String>()
    var filePaths = ArrayList<String>()
    var list = ArrayList<ArrayList<String>>()
    init {
        list = searchAllFiles(file)
    }

    fun getAllFilePaths(): LiveData<ArrayList<String>>? {
        // allFilesNames.value = searchAllFiles(file)[1]
        allFilesPaths.value = list[0]
        println(allFilesPaths.value)
        return allFilesPaths
    }

    fun getAllFileNames(): LiveData<ArrayList<String>>? {
        // allFilesNames.value = searchAllFiles(file)[1]
        allFilesNames.value = list[1]
        println(allFilesNames.value)
        return allFilesNames
    }

    private fun searchAllFiles(folder: File): ArrayList<ArrayList<String>>{
        val encryptPattern = "com."
        val fileList = folder.listFiles()
        if (fileList != null) {
            for (file in fileList) {
                if (file.isDirectory && !file.name.startsWith(encryptPattern)) {
                    searchAllFiles(file)
                } else {
                    if (!file.name.startsWith(encryptPattern)){
                        fileNames.add(file.name)
                        filePaths.add(file.path)
                    }
                }
            }
        }
        list.add(filePaths)
        list.add(fileNames)
        return list
    }
}