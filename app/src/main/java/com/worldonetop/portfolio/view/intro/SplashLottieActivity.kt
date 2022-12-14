package com.worldonetop.portfolio.view.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.worldonetop.portfolio.databinding.ActivitySplashLottieBinding
import com.worldonetop.portfolio.view.main.MainActivity
import kotlinx.coroutines.*
import java.util.*

class SplashLottieActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000L + Random().nextInt(2000))
            withContext(Dispatchers.Main){
                nextMainActivity()
            }
        }
    }

    private fun nextMainActivity(){
        startActivity(Intent(this@SplashLottieActivity, MainActivity::class.java))
    }
}