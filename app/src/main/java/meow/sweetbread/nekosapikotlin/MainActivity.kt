package meow.sweetbread.nekosapikotlin

import android.Manifest
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileInputStream
import java.net.URL


class MainActivity : AppCompatActivity() {
    var curVersion = "2.0.3"

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val context = this


        GlobalScope.launch(Dispatchers.Main) {
            var actVersion = ""
            GlobalScope.async { actVersion = URL("http://koteika.ml/NA/version").readText() }
                .await()
            if (actVersion != curVersion) {
                longToast("Версия устарела")

                val directory = context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (!directory!!.exists()) directory.mkdirs()
                val actApk = File(directory, "actApk.apk")
                if (!actApk.exists()) actApk.createNewFile()

                toast("Зарузка обновления...")
                GlobalScope.async { actApk.writeBytes(URL("http://koteika.ml/NA/apk").readBytes()) }
                    .await()
                toast("Зарузка завершена")

                val uri =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        FileProvider.getUriForFile(context, "${context.packageName}.provider", actApk)
                    else
                        Uri.fromFile(actApk)

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Log.i("VMDownloader", e.stackTraceToString())
                    toast("Ошибка")
                }

            }
        }
    }
}