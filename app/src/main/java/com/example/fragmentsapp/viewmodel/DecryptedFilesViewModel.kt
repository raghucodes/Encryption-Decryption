package com.example.fragmentsapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.io.File

class DecryptedFilesViewModel(application: Application):AndroidViewModel(application) {
    private val context = application
    private val file = File(context.filesDir.absolutePath+"/DESDecryptedFiles")
    var decryptedFilesPaths =  MutableLiveData<ArrayList<String>>()
    var decryptedFilesNames =  MutableLiveData<ArrayList<String>>()
    var fileNames = ArrayList<String>()
    var filePaths = ArrayList<String>()
    var list = ArrayList<ArrayList<String>>()
    init{
        list = searchDecryptedFiles(file)
    }

    fun getDecryptedFilePaths(): MutableLiveData<ArrayList<String>> {
        decryptedFilesPaths.value = list[1]
        return decryptedFilesPaths
    }

    fun getDecryptedFileNames(): MutableLiveData<ArrayList<String>> {
        decryptedFilesNames.value = list[0]
        return decryptedFilesNames
    }
    private fun searchDecryptedFiles(file : File):ArrayList<ArrayList<String>> {
        val fileList = file.listFiles()
        if (fileList != null) {
            for (file in fileList) {
                if (file.isDirectory) {
                    searchDecryptedFiles(file)
                } else {
                    fileNames.add(file.name)
                    filePaths.add(file.path)
                }
            }
        }
        list.add(fileNames)
        list.add(filePaths)
        return list
    }

}