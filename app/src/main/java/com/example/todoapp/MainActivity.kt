package com.example.todoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    fun getBundle(): Bundle? {
        return bundle
    }

    fun setBundle(bundle: Bundle) {
        this.bundle = bundle
    }
}