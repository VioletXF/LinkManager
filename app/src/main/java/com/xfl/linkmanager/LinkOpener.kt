package com.xfl.linkmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class LinkOpener : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data as Uri
        val str = uri.toString()
        val opener = Intent(Intent.ACTION_VIEW)
        opener.addCategory(Intent.CATEGORY_BROWSABLE)
        if(intent.scheme == "kakatalkinappbrowser"){
            val link = str.substring(23)
            opener.data = Uri.parse(link)
        } else {
            if(uri.scheme == null){
                opener.data = Uri.parse("https://${str}")
            } else {
                opener.data = uri
            }

        }

        startActivity(opener)
        finish()

    }

}