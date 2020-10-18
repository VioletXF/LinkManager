package com.xfl.linkmanager

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ContinueOnPc : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(MainApplication.isPackageInstalled("com.microsoft.appmanager", packageManager)) {
            val uri = intent.data as Uri
            val str = uri.toString()
            var link = if (intent.scheme == "kakatalkinappbrowser") {
                str.substring(23)
            } else {
                str
            }

            val opener = Intent(Intent.ACTION_SEND)
            opener.component =
                ComponentName(
                    "com.microsoft.appmanager",
                    "com.microsoft.mmx.core.ui.WebPageShareActivity"
                )
            opener.addCategory(Intent.CATEGORY_DEFAULT)
            val newUri = Uri.parse(link)
            if (newUri.scheme == null) link = "https://$link"
            opener.putExtra(Intent.EXTRA_TEXT, link)
            opener.type = "text/plain"
            startActivity(opener)
        } else {
            Toast.makeText(this, "Please install the app and link to your PC", Toast.LENGTH_LONG).show()
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.microsoft.appmanager")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.microsoft.appmanager")
                    )
                )
            }
        }
        finish()
    }
}