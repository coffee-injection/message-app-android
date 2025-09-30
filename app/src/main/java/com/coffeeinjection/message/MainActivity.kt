package com.coffeeinjection.message

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.coffeeinjection.message.databinding.ActivityMainBinding
import com.coffeeinjection.message.util.Logger

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate")
        enableEdgeToEdge()
        setContentView(binding.root)

    }
    override fun onStart() {
        super.onStart()
        Logger.d( "onStart")
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
        super.onDestroy()
    }
}