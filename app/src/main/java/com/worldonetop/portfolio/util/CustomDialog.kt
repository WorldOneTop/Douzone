package com.worldonetop.portfolio.util

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.view.main.WebViewActivity

object CustomDialog {
    fun loading(context: Context): Dialog {
        return Dialog(context).apply {
            setContentView(
                LottieAnimationView(context).apply {
                    setAnimation(R.raw.lottie_loading)
                    playAnimation()
                    repeatCount = ValueAnimator.INFINITE
                    maxWidth = 500
                    maxHeight = 500
                }
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명
            window?.setLayout(500,500) // 사이즈 조절
            setCanceledOnTouchOutside(false) // 클릭 캔슬 x
            setCancelable(false)  // 백버튼 캔슬 x
        }
    }

    fun addLink(context: Context, positiveListener: (String)->Unit) : AlertDialog{
        val editText = EditText(context)
        return AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.add_links))
            .setView(editText)
            .setPositiveButton("OK"){ _, _ ->
                positiveListener(editText.text.toString())
            }
            .create()
    }

    fun selectWebConnect(context: Context, url:String):Dialog{
        return Dialog(context).apply {
            setContentView(R.layout.dialog_web_select)
            findViewById<LinearLayout>(R.id.webViewSelectedApp).setOnClickListener{
                context.startActivity(Intent(context, WebViewActivity::class.java).putExtra("data",parsUrl(url)))
            }
            findViewById<LinearLayout>(R.id.webViewSelectedBrowser).setOnClickListener{
                try{
                    context.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(parsUrl(url))))
                }catch (e: Exception){
                    Toast.makeText(context, context.getString(R.string.error_connect_unknown), Toast.LENGTH_LONG).show()
                }
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명

        }
    }
    private fun parsUrl(url:String):String{
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            return "http://$url"
        return url
    }
}