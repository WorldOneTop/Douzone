package com.example.portfolio.view

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import com.example.portfolio.databinding.ActivityIntroBinding


class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextTextView.startAnimation(getBlinkAnimation())

        binding.root.setOnClickListener {
            nextSplashActivity()
        }
    }

    private fun getBlinkAnimation(): AlphaAnimation =
        AlphaAnimation(0.0f, 1.0f).apply {
            val anim = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 400
            anim.startOffset = 100
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = Animation.INFINITE
        }

    private fun nextSplashActivity(){
        startActivity(Intent(this@IntroActivity, SplashLottieActivity::class.java))
        this@IntroActivity.finish()
    }
}