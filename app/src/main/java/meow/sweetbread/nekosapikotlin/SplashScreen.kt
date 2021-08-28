package meow.sweetbread.nekosapikotlin

import android.content.Intent
import android.content.pm.PackageInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val internalStorageDir = filesDir
        val context = this
        val pInfo: PackageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
        val curVersion = pInfo.versionName

        val FL = File(internalStorageDir, "FirstLaunch")
        if (!FL.exists()) FL.createNewFile()

        if (getDefaultSharedPreferences(this).getBoolean("firstLaunch", true)) {
            FL.writeText(curVersion)

            GlobalScope.launch(Dispatchers.Main) {
                var WN_text = ""
                try {
                    GlobalScope.async {
                        WN_text = URL("http://koteika.ml/NA/${curVersion}/WN").readText()
                    }.await()
                } catch (e: FileNotFoundException) {
                    WN_text = "Че-та добавлено, но что - ХЗ\n(А вообще, это, типа, сообщение об ошибке)"
                }

                val WN = File(internalStorageDir, "WhatsNew.txt")
                if (!WN.exists()) WN.createNewFile()
                WN.writeText(WN_text)

                startActivity(Intent(context, WhatsNew::class.java))
                finish()
            }
        } else {
            if (FL.readText() != curVersion) {
                FL.writeText(curVersion)

                GlobalScope.launch(Dispatchers.Main) {
                    var WN_text = ""
                    try {
                        GlobalScope.async {
                            WN_text = URL("http://koteika.ml/NA/${curVersion}/WN").readText()
                        }.await()
                    } catch (e: FileNotFoundException) {
                        WN_text = "Че-та добавлено, но что - ХЗ\n(А вообще, это, типа, сообщение об ошибке)"
                    }
                    val WN = File(internalStorageDir, "WhatsNew.txt")
                    if (!WN.exists()) WN.createNewFile()
                    WN.writeText(WN_text)

                    startActivity(Intent(context, WhatsNew::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

    }
}