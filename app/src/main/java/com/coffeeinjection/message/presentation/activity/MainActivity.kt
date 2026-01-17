package com.coffeeinjection.message.presentation.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.coffeeinjection.message.databinding.ActivityMainBinding
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: SharedViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate")

        setContentView(binding.root)
        viewModel.initForTest()
    }

    override fun onStart() {
        super.onStart()
        Logger.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")
    }

    override fun onPause() {
        Logger.d("onPause")
        super.onPause()
    }

    override fun onStop() {
        Logger.d("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Logger.d("onDestroy")
        viewModel.clearForTest()
        super.onDestroy()
    }
}