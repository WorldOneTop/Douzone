package com.example.portfolio.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.portfolio.databinding.ActivitySplashLottieBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SplashLottieActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            Thread.sleep(1000L + Random().nextInt(2000))
            withContext(Dispatchers.Main){
                nextMainActivity()
            }
        }
    }

    private fun nextMainActivity(){
        startActivity(Intent(this@SplashLottieActivity, MainActivity::class.java))
        this@SplashLottieActivity.finish()
    }
}