package com.xfl.linkmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.webkit.WebSettings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.UnknownHostException

class NotificationListener : NotificationListenerService() {
    private lateinit var notificationManager: NotificationManager
    override fun onCreate() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {

        notificationManager.createNotificationChannel(
            NotificationChannel(
                "com.xfl.linkmanager.notification.openlink",
                "Open Link",
                NotificationManager.IMPORTANCE_LOW
            )
        )

    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (sbn.packageName == applicationContext.packageName) return
        val extras = sbn.notification.extras
        var text = extras.get("android.text")?.toString()


        if (text != null) {
            val tokens = text.split("""[ \n]""".toRegex())

            for (token in tokens) {
                if (validURL(token)) {
                    Log.d("Notification", "valid link:${token}")
                    val noti = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        createNotificationChannel()
                        NotificationCompat.Builder(
                            this,
                            "com.xfl.linkmanager.notification.openlink"
                        )
                    } else {
                        NotificationCompat.Builder(this)
                    }
                    var uri = Uri.parse(token)

                    if (uri.scheme == null) uri = Uri.parse("https://$token")
                    val openBrowser = Intent(this, LinkOpener::class.java)
                    openBrowser.data = uri
                    val openBrowserPending = PendingIntent.getActivity(
                        this,
                        0,
                        openBrowser,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    val openBrowserAction = NotificationCompat.Action.Builder(
                        R.mipmap.ic_launcher,
                        "Open",
                        openBrowserPending
                    ).build()
                    val openPC = Intent(this, ContinueOnPc::class.java)
                    openPC.data = uri
                    val openPCPending = PendingIntent.getActivity(
                        this,
                        1,
                        openPC,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    val openPCAction = NotificationCompat.Action.Builder(
                        R.mipmap.ic_launcher,
                        "PC",
                        openPCPending
                    ).build()
                    noti.apply {
                        setSmallIcon(R.mipmap.ic_launcher)
                        setStyle(
                            NotificationCompat.BigTextStyle().bigText("$token: Loading Title...")
                        )
                        setContentText("$token: Loading Title...")
                        setContentIntent(openBrowserPending)
                        addAction(openPCAction)
                        addAction(openBrowserAction)
                        setAutoCancel(true)
                    }
                    val id = token.hashCode()
                    notificationManager.notify(id, noti.build())
                    GlobalScope.launch(Dispatchers.IO) {
                        val ua =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36"
                        var doc: Document? = null
                        try {
                            doc = Jsoup.connect(uri.toString()).userAgent(ua)
                                .method(Connection.Method.HEAD).referrer("https://www.google.com")
                                .get()
                        } catch (e: UnknownHostException) {
                            notificationManager.cancel(id)
                            return@launch
                        }
                        val content = try {
                            val metaOgTitle: Elements = doc.select("meta[property=og:title]")
                            val title = if (metaOgTitle.count() != 0) {
                                metaOgTitle.attr("content")
                            } else {
                                doc.select("title").text()
                            }
                            "$title: $token"
                        } catch (e: Exception){
                            token
                        }
                        noti.apply {
                            setStyle(
                                NotificationCompat.BigTextStyle().bigText(content)
                            )
                            setContentText(content)
                        }
                        notificationManager.notify(id, noti.build())


                    }


                }
            }
        }

    }

    private fun validURL(str: String): Boolean {
        val pattern = "^(https?:\\/\\/)?" + // protocol
                "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" + // domain name
                "((\\d{1,3}\\.){3}\\d{1,3}))" + // OR ip (v4) address
                "(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*" + // port and path
                "(\\?[;&a-z\\d%_.~+=-]*)?" + // query string
                "(\\#[-a-z\\d_]*)?$" // fragment locator
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        return regex.matches(str)
    }


}