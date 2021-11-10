package com.example.fragmentsapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class EncryptedFilesViewModel(application: Application): AndroidViewModel(application) {
    private val context = application
    private val file = File(context.filesDir.absolutePath+"/DESEncryptedFiles")
    var encryptedFilesPaths =  MutableLiveData<ArrayList<String>>()
    var encryptedFilesNames =  MutableLiveData<ArrayList<String>>()
    var fileNames = ArrayList<String>()
    var filePaths = ArrayList<String>()
    var list = ArrayList<ArrayList<String>>()
    init{
        list = searchEncryptedFiles(file)
    }

    fun getEncryptedFilePaths():MutableLiveData<ArrayList<String>>{
        encryptedFilesPaths.value = list[1]
        return encryptedFilesPaths
    }

    fun getEncryptedFileNames():MutableLiveData<ArrayList<String>>{
        encryptedFilesNames.value = list[0]
        return encryptedFilesNames
    }
    private fun searchEncryptedFiles(file :File):ArrayList<ArrayList<String>> {
        val fileList = file.listFiles()
        if (fileList != null) {
            for (file in fileList) {
                if (file.isDirectory) {
                    searchEncryptedFiles(file)
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