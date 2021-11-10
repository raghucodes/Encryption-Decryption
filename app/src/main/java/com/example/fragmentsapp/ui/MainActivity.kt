package com.example.fragmentsapp.ui

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.fragmentsapp.R
import com.example.fragmentsapp.viewmodel.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.security.SecureRandom
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity() {

    private var toolbar: ActionBar? = null
    private lateinit var secretAESKey: SecretKey
    private lateinit var  aesInitializationVector: ByteArray
    private lateinit var secretDESKey: SecretKey
    private lateinit var  desInitializationVector: ByteArray
    lateinit var sharedPreferences: SharedPreferences
    private var fragmentid : Int = 0
    private var SAVE_INSTANCE_FRAGMENT_KEY = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_INSTANCE_FRAGMENT_KEY,fragmentid)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = supportActionBar
        val navigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navigationView.setOnNavigationItemSelectedListener(navigationListener)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                createEncryptedPreferences()
                if(savedInstanceState == null) {
//                    val fragmentid = savedInstanceState?.getInt(SAVE_INSTANCE_FRAGMENT_KEY)
//                    val fragment = fragmentid?.let { fragmentManager.findFragmentById(it) }
                    toolbar!!.title = "All Files"
                    loadFragment(AllFilesFragment())
                }
            } else ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        if(checkFirstRun()){
            secretAESKey = createAESKey()
            aesInitializationVector = createAESIntializationVector()
            secretDESKey = createDESKey()
            desInitializationVector = createDESIntializationVector()
            sharedPreferences.edit()
                .putString("AESKey",Base64.getEncoder().encodeToString(secretAESKey.encoded))
                .putString("AESIVector",Base64.getEncoder().encodeToString(aesInitializationVector))
                .putString("DESKey", Base64.getEncoder().encodeToString(secretDESKey.encoded))
                .putString("DESIVector", Base64.getEncoder().encodeToString(desInitializationVector))
                .apply()
        }
        else
        {
            val encodedAESKey = sharedPreferences.getString("AESKey","")
            val decodedAESKey = Base64.getDecoder().decode(encodedAESKey)
            secretAESKey = SecretKeySpec(decodedAESKey, 0, decodedAESKey.size, "AES")
            val encodedAESArray = sharedPreferences.getString("AESIVector","")
            val decodedAESArray = Base64.getDecoder().decode(encodedAESArray)
            aesInitializationVector = decodedAESArray
            val encodedDESKey = sharedPreferences.getString("DESKey","")
            val decodedDESKey = Base64.getDecoder().decode(encodedDESKey)
            secretDESKey = SecretKeySpec(decodedDESKey, 0, decodedDESKey.size, "DESede")
            val encodedDESArray = sharedPreferences.getString("DESIVector","")
            val decodedDESArray = Base64.getDecoder().decode(encodedDESArray)
            desInitializationVector = decodedDESArray
        }
    }


     // Function to check first run of application.

    private fun checkFirstRun():Boolean{
        val value = sharedPreferences.getBoolean("first_run", true)
        if(value){
            sharedPreferences.edit()
                .putBoolean("first_run",false)
                .apply()
        }
        return value
    }

    // Function to create Encrypted Shared Preferences

     private fun createEncryptedPreferences(){

        // Step 1: Create or retrieve the Master Key for encryption/decryption
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // Step 2: Initialize/open an instance of EncryptedSharedPreferences
         sharedPreferences = EncryptedSharedPreferences.create(
            "MySharedPreferences",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val navigationListener: BottomNavigationView.OnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                val fragment: Fragment
                fragmentid = menuItem.itemId
                when (menuItem.itemId) {
                    R.id.AllFilesNavigation -> {
                        toolbar!!.title = "All Files"
                        fragment = AllFilesFragment()
                        loadFragment(fragment)
                        return true
                    }
                    R.id.encryptNavigation -> {
                        toolbar!!.title = "EncryptedFiles"
                        fragment =
                            EncryptedFilesFragment()
                        loadFragment(fragment)
                        return true
                    }
                    R.id.decryptNavigation -> {
                        toolbar!!.title = "DecryptedFiles"
                        fragment =
                            DecryptedFilesFragment()
                        loadFragment(fragment)
                        return true
                    }
                }
                return false
            }
        }
    private fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /*
     * create a random secretKey of key size 256
     * using keyGenerator to create Key
     */
    private fun createAESKey(): SecretKey {
        val secureRandom = SecureRandom()
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256,secureRandom)
        return keyGenerator.generateKey()
    }

    /*
     * create intialization vector of size 16.
     */
    private fun createAESIntializationVector(): ByteArray{
        val initializationVector = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(initializationVector)
        return initializationVector
    }

    private fun createDESKey(): SecretKey {
        val secureRandom = SecureRandom()
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("DESede")
        keyGenerator.init(168,secureRandom)
        return keyGenerator.generateKey()
    }

    private fun createDESIntializationVector(): ByteArray{
        val initializationVector = ByteArray(8)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(initializationVector)
        println("in function size of ivector is "+ initializationVector.size)
        return initializationVector
    }

}
