package meow.sweetbread.nekosapikotlin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.UnsupportedOperationException
import java.net.URL
import java.nio.channels.UnsupportedAddressTypeException
import java.util.*

class UpdateHunter : Service() {
    lateinit var handler: Handler
    lateinit var mRunnable: Runnable
    lateinit var curVersion: String

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Нет!")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        curVersion = intent!!.getStringExtra("curVersion")!!

        handler = Handler()
        mRunnable = Runnable { checkUpdate() }
        handler.postDelayed(mRunnable, 60000)

        return START_STICKY
    }

    private fun checkUpdate() {
        if (isOnline(this)) {
            GlobalScope.launch(Dispatchers.Main) {
                var actVersion: String
                withContext(Dispatchers.Default) {
                    actVersion = URL("http://koteika.ml/NA/version").readText()
                }
                if (actVersion != curVersion) {
                    val notification = NotificationCompat.Builder(this@UpdateHunter, "Meow")
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("Обновление готово!")
                        .setContentText("Вышло новое обновление: $actVersion")
                    with (NotificationManagerCompat.from(this@UpdateHunter)) {
                        // notificationId is a unique int for each notification that you must define
                        notify(0, notification.build())
                    }
                }
            }
        }

        handler.postDelayed(mRunnable, 60000)
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) return true
        return false
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "UpdateHunter"
            val descriptionText = "For notificating about new updates of app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Meow", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}