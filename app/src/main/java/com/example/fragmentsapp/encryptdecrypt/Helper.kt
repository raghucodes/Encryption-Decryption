package com.example.fragmentsapp.encryptdecrypt

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StatFs
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.fragmentsapp.R
import kotlinx.android.synthetic.main.fragment_allfiles.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.system.measureTimeMillis


open class Helper: Fragment() {
    private val AES = "AES"
    private val AES_CIPHER_ALGORITHM = "AES/GCM/NoPadding"
    private val DES_CIPHER_ALGORITHM = "DESede/CBC/PKCS5Padding"
    lateinit var sharedPreferences: SharedPreferences
    private var freeSpace : Long = 0
    private lateinit var chunk:ByteArray
    private var status = 0
    private lateinit var mView: View
    private lateinit var progressBar: ProgressBar
    private lateinit var progress : TextView
    private var handler = Handler()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createEncryptedPreferences()
        val statFs = StatFs(requireContext().filesDir.absolutePath)
        val blockSize = statFs.blockSizeLong
        val availableBlocks = statFs.availableBlocksLong
        println("free space is "+ (blockSize*availableBlocks)/1000)
        freeSpace = blockSize*availableBlocks
        mView = view

    }

    private fun createEncryptedPreferences(){
        // Step 1: Create or retrieve the Master Key for encryption/decryption
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // Step 2: Initialize/open an instance of EncryptedSharedPreferences
        sharedPreferences = EncryptedSharedPreferences.create(
            "MySharedPreferences",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAESKey(): SecretKey{
        val encodedKey = sharedPreferences.getString("AESKey","")
        val decodedKey = Base64.getDecoder().decode(encodedKey)
        val sKey =  SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        println("secret key from preferences is  $sKey")
        return sKey
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAESIVector(): ByteArray {
        val encodedArray = sharedPreferences.getString("AESIVector","")
        // initializationVector = decodedArray
        return Base64.getDecoder().decode(encodedArray)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDESKey(): SecretKey{
        val encodedKey = sharedPreferences.getString("DESKey","")
        val decodedKey = Base64.getDecoder().decode(encodedKey)
        val sKey =  SecretKeySpec(decodedKey, 0, decodedKey.size, "DESede")
        println("secret key from preferences is  $sKey")
        return sKey
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDESIVector(): ByteArray {
        val encodedArray = sharedPreferences.getString("DESIVector","")
        // initializationVector = decodedArray
        return Base64.getDecoder().decode(encodedArray)
    }

    @RequiresApi(Build.VERSION_CODES.O)
      fun doAESEncryption(originalFilePath: String, doDelete: Boolean){
        try {
            //val pdfPath: Path = Paths.get(originalFilePath)
           // val pdf: ByteArray = Files.readAllBytes(pdfPath)
            progress = mView.findViewById(R.id.progresstext)
            progressBar = mView.findViewById(R.id.progressBar)
            val fileAtPath = File(originalFilePath)
            var name = fileAtPath.name
            if (fileAtPath.length() < freeSpace) {
                println("files.Dir is " + requireContext().filesDir)
                val fin = FileInputStream(originalFilePath)
                val newFile = File(getOrCreateDirectory("AESEncryptedFiles") + "/" + name)
                //val newFile = File("/storage/emulated/0/Newdirectory/$name")
                chunk = ByteArray((fileAtPath.length()/10).toInt())
                val fOut = FileOutputStream(newFile,true)
                val cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM)
                val ivParameterSpec = IvParameterSpec(getAESIVector())
                cipher.init(Cipher.ENCRYPT_MODE,getAESKey(),ivParameterSpec)
                if(newFile.length() <= 0) {
                    val start = System.currentTimeMillis()
                    while (fin.read(chunk) > 0) {
                        fOut.write(cipher.update(chunk))
                        status += 10
                    }
                    cipher.doFinal()
                    val time = System.currentTimeMillis()-start
                    showToast(time.toString())
                }else{
                    showToast("encrypted version of this file already exists")
                }
                fin.close()
                fOut.flush()
                fOut.close()
                if (doDelete)
                    fileAtPath.delete()
            } else {
                noSpaceDialogBox(fileAtPath.length())
            }
        }catch (exception : Exception){
            showToast(exception.toString())
        }
    }

    private fun aesEncryption (fileContent:ByteArray, secretKey: SecretKey, initializationVector: ByteArray):ByteArray{
        val cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM)
        val ivParameterSpec = IvParameterSpec(initializationVector)
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,ivParameterSpec)
        return cipher.update(fileContent)
       // return cipher.doFinal(fileContent)
    }

    private fun showToast(toast: String){
        requireActivity().runOnUiThread(Runnable {
            kotlin.run {
                Toast.makeText(requireContext(),toast,Toast.LENGTH_LONG).show()
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun doAESDecryption(originalFilePath: String) {
        try {
            //val path: Path = Paths.get(originalFilePath)
            progress = mView.findViewById(R.id.progresstext1)
            progressBar = mView.findViewById(R.id.progressBar1)
            val fileAtPath = File(originalFilePath)
            var name = fileAtPath.name
            if (fileAtPath.length() < freeSpace) {
                val newFile = File(getOrCreateDirectory("AESDecryptedFiles") + "/" + name)
                val fin = FileInputStream(originalFilePath)
                val fOut = FileOutputStream(newFile,true)
                chunk = ByteArray((fileAtPath.length()/10).toInt())
                val cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM)
                val ivParameterSpec = IvParameterSpec(getAESIVector())
                cipher.init(Cipher.DECRYPT_MODE,getAESKey(),ivParameterSpec)
                var i = 0
                if(newFile.length() <= 0) {
                    while (fin.read(chunk) > 0) {
                        i += 1
                        Log.e("output", "$i")
                        fOut.write(cipher.update(chunk))
                    }
                }
                else{
                    showToast("this file is decrypted ")
                }
               // cipher.doFinal()  use this if you are using padding.
                fin.close()
                fOut.flush()
                fOut.close()
            } else {
                noSpaceDialogBox(fileAtPath.length())
            }
        }catch (exception: Exception){
            showToast(exception.toString())
        }
    }

    private fun aesDecryption(fileContent: ByteArray,secretKey: SecretKey,initializationVector: ByteArray):ByteArray {
        val cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM)
        val ivParameterSpec = IvParameterSpec(initializationVector)
        cipher.init(Cipher.DECRYPT_MODE,secretKey,ivParameterSpec)
        return cipher.update(fileContent)
        // return cipher.doFinal(fileContent)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun doDESEncryption(originalFilePath: String, doDelete: Boolean){
        try {
                    progress = mView.findViewById(R.id.progresstext)
                    progressBar = mView.findViewById(R.id.progressBar)
                    progress.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    progressBar.min = 0
                    progressBar.max = 100
                    progressBar.isIndeterminate = false
                    progressBar.progress = 0
                    // val pdfPath: Path = Paths.get(originalFilePath)
                    //val pdf: ByteArray = Files.readAllBytes(pdfPath)
                    val fileAtPath = File(originalFilePath)
                    var name = fileAtPath.name
                    if (fileAtPath.length() < freeSpace) {
                        //println("files.Dir is " + requireContext().filesDir)
                        val fin = FileInputStream(originalFilePath)
                        val newFile = File(getOrCreateDirectory("DESEncryptedFiles") + "/" + name)
                        //val newFile = File("/storage/emulated/0/Newdirectory/$name")
                        chunk = ByteArray((fileAtPath.length() / 10).toInt())
                        val fOut = FileOutputStream(newFile, true)
                        val cipher = Cipher.getInstance(DES_CIPHER_ALGORITHM)
                        val ivParameterSpec = IvParameterSpec(getDESIVector())
                        cipher.init(Cipher.ENCRYPT_MODE, getDESKey(), ivParameterSpec)
                        if (newFile.length() <= 0) {
                            Thread(Runnable {
                                kotlin.run {
                                    val start = System.currentTimeMillis()
                                    while (fin.read(chunk) > 0) {
                                        fOut.write(cipher.update(chunk))
                                        status += 10
                                        // progressBar.progress = status
                                        handler.post(Runnable {
                                            kotlin.run {
                                                if (status < 100) {
                                                    progressBar.progress = status
                                                    val percentage = "$status%"
                                                    progress.text = percentage
                                                } else {
                                                    progressBar.visibility = View.INVISIBLE
                                                    progress.visibility = View.INVISIBLE
                                                    status = 0
                                                }
                                            }
                                        })
                                    }
                                    cipher.doFinal()
                                    fin.close()
                                    fOut.flush()
                                    fOut.close()
                                    val end = System.currentTimeMillis()-start
                                   // showToast(end.toString())
                                }
                            }).start()
                            //progressBar.visibility = View.INVISIBLE
                        } else {
                            progressBar.visibility = View.INVISIBLE
                            progress.visibility = View.INVISIBLE
                            status = 0
                            showToast("encrypted version of this file already exists")
                        }
                        if (doDelete)
                            fileAtPath.delete()
                    } else {
                        noSpaceDialogBox(fileAtPath.length())
                    }
        }catch (exception : Exception){
            showToast(exception.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun doDESDecryption(originalFilePath: String) {
        try {
            progress = mView.findViewById(R.id.progresstext1)
            progressBar = mView.findViewById(R.id.progressBar1)
            progress.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            progressBar.min = 0
            progressBar.max = 100
            progressBar.isIndeterminate = false
            progressBar.progress = 0
            val path: Path = Paths.get(originalFilePath)
            val fileAtPath = File(originalFilePath)
            var name = fileAtPath.name
            if (fileAtPath.length() < freeSpace) {
                val newFile = File(getOrCreateDirectory("DESDecryptedFiles") + "/" + name)
                val fin = FileInputStream(originalFilePath)
                val fOut = FileOutputStream(newFile,true)
                chunk = ByteArray((fileAtPath.length()/10).toInt())
                val cipher = Cipher.getInstance(DES_CIPHER_ALGORITHM)
                val ivParameterSpec = IvParameterSpec(getDESIVector())
                cipher.init(Cipher.DECRYPT_MODE,getDESKey(),ivParameterSpec)
                var i = 0
                if(newFile.length() <= 0) {
                    Thread(Runnable {
                        kotlin.run {
                            while (fin.read(chunk) > 0) {
                                fOut.write(cipher.update(chunk))
                                status += 10
                                handler.post(Runnable {
                                    kotlin.run {
                                        if(status < 100) {
                                            progressBar.progress = status
                                            val percentage = "$status%"
                                            progress.text = percentage
                                        }else{
                                            progressBar.visibility = View.INVISIBLE
                                            progress.visibility = View.INVISIBLE
                                            status = 0
                                        }
                                    }
                                })
                            }
                            fin.close()
                            fOut.flush()
                            fOut.close()
                        }
                    }).start()

                   // cipher.doFinal()
                }
                else{
                    progressBar.visibility = View.INVISIBLE
                    progress.visibility = View.INVISIBLE
                    status = 0
                    showToast("this file is decrypted ")
                }
                // cipher.doFinal()  use this if you are using padding.

            } else {
                noSpaceDialogBox(fileAtPath.length())
            }
        }catch (exception: Exception){
            showToast(exception.toString())
        }
    }

    private fun noSpaceDialogBox(size: Long){
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())

        // set message of alert dialog
        dialogBuilder.setMessage("RequiredSpace : $size Bytes\nAvailableSpace : $freeSpace Bytes")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Free-up space", DialogInterface.OnClickListener {
                    dialog, id -> openFileManager()
            })
            // negative button text and action
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("No enough space")
        // show alert dialog
        alert.show()
    }

    private fun openFileManager(){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.type = "*/*"
        startActivity(intent)
    }

//    private fun getOrCreateDecryptDirectory(): String {
//        val file = File(requireContext().filesDir.absolutePath+"/AESDecryptedFiles")
//        if (!file.exists())
//        {
//            if (file.mkdirs())
//                Toast.makeText(requireContext(),"file creation success",Toast.LENGTH_LONG).show()
//            else
//                Toast.makeText(requireContext(),"file creation failed",Toast.LENGTH_LONG).show()
//        }
//        return file.path
//    }

    private fun getOrCreateDirectory(newDirectory: String): String {
        val file = File(requireContext().filesDir.absolutePath+"/"+newDirectory)
        if (!file.exists())
        {
            if (file.mkdirs())
                Toast.makeText(requireContext(),"file creation success",Toast.LENGTH_LONG).show()
            else
                Toast.makeText(requireContext(),"file creation failed",Toast.LENGTH_LONG).show()
        }
        return file.path
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun enryptManyFiles(list :ArrayList<String>, bool :Boolean){
//        for(file in list){
//            doAESEncryption(file,bool)
//        }
//    }

}