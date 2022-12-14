package com.worldonetop.portfolio.view.main

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.util.CustomDialog


class WebViewActivity : AppCompatActivity() {
    private lateinit var loadingDialog: Dialog
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)
        initData()
        initView()
    }

    fun initData() {
        loadingDialog = CustomDialog.loading(this)

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingDialog.show()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingDialog.dismiss()
            }


            override fun onReceivedError(view: WebView?, request: WebResourceRequest?,error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                when (error?.errorCode) {
                    ERROR_BAD_URL -> { // 잘못된 URL
                        Toast.makeText(this@WebViewActivity, getString(R.string.error_bad_url), Toast.LENGTH_LONG).show()
                    }
                    ERROR_FILE_NOT_FOUND -> {  // 파일을 찾을 수 없습니다
                        Toast.makeText(this@WebViewActivity, getString(R.string.error_bad_url), Toast.LENGTH_LONG).show()
                    }
                    ERROR_TIMEOUT -> { // 연결 시간 초과
                        Toast.makeText(this@WebViewActivity, getString(R.string.error_check_internet), Toast.LENGTH_LONG).show()
                    }
                    else->{
                        Toast.makeText(this@WebViewActivity, getString(R.string.error_connect_unknown), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        val ws: WebSettings = webView.settings
        ws.javaScriptEnabled = true // javascript 사용
        ws.javaScriptCanOpenWindowsAutomatically = true // // 자바스크립트가 창을 자동으로 열 수 있게
        ws.useWideViewPort = true //페이지에 뷰포트 메타 태그가 있으면 태그에 지정된 너비 값이 사용
        ws.loadWithOverviewMode = true //컨텐츠가 웹뷰보다 클때 스크린크기에 맞추기
    }

    fun initView() {
        intent.getStringExtra("data").let {
            if(it == null){
                Toast.makeText(this, getString(R.string.error_connect_unknown), Toast.LENGTH_LONG).show()
            }else{
                webView.loadUrl(it)
            }

        }
    }


    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}