package com.worldonetop.portfolio.view.intro

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import com.worldonetop.portfolio.databinding.ActivityIntroBinding
import com.worldonetop.portfolio.util.DoubleClick


class IntroActivity : AppCompatActivity() {
    private val doubleClick by lazy { // 연속 클릭으로 인한 중복 실행 방지
        DoubleClick(lifecycle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextTextView.startAnimation(getBlinkAnimation())


        binding.root.setOnClickListener {
            doubleClick.run {
                nextSplashActivity()
            }
        }
    }

    private fun getBlinkAnimation(): AlphaAnimation =
        AlphaAnimation(0.0f, 1.0f).apply {
            duration = 400
            startOffset = 100
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }

    private fun nextSplashActivity(){
        startActivity(
            Intent(this@IntroActivity, SplashLottieActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        )
        this@IntroActivity.finish()
    }
}