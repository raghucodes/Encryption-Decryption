package com.example.fragmentsapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainActivityViewModel(application: Application): AndroidViewModel(application) {

    init{
        println("main activity view model is created $application")
    }

    

}