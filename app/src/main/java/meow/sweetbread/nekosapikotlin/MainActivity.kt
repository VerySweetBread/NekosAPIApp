package meow.sweetbread.nekosapikotlin

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.snackbar.Snackbar
import com.roger.catloadinglibrary.CatLoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import java.io.File
import java.net.URL
import java.security.MessageDigest
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var circularProgressDrawable: CircularProgressDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        val drawer = findViewById<DrawerLayout>(R.id.mainActivity)
        val toggle = ActionBarDrawerToggle (this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.about_button -> {
                    val i = Intent(this, About::class.java)
                    startActivity(i)
                }
            }
            return@setNavigationItemSelectedListener true
        }

        val context = this
        val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val curVersion = pInfo.versionName

        val i = Intent(this, UpdateHunter::class.java)
        i.putExtra("curVersion", curVersion)
        startService(i)

        Log.i("MEOW", hash("meow"))

        circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 10f
        circularProgressDrawable.centerRadius = 100f
        circularProgressDrawable.setColorSchemeColors(R.drawable.progress)
        circularProgressDrawable.start()

        findViewById<ImageView>(R.id.blocker).setImageDrawable(circularProgressDrawable)

        if (isOnline(context)) {
            findViewById<ImageView>(R.id.blocker).visibility = View.GONE
            GlobalScope.launch(Dispatchers.Main) {
                var actVersion: String
                withContext(Dispatchers.Default) {
                    actVersion = URL("http://koteika.ml/NA/version").readText()
                }
                if (actVersion != curVersion) {
                    val sb: Snackbar = Snackbar.make(
                        drawer,
                        "Версия устарела",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Обновить") {
                            GlobalScope.launch(Dispatchers.Main) {
                                val directory = context.applicationContext.getExternalFilesDir(
                                    Environment.DIRECTORY_DOWNLOADS
                                )
                                if (!directory!!.exists()) directory.mkdirs()
                                val actApk = File(directory, "actApk.apk")
                                if (!actApk.exists()) actApk.createNewFile()

                                val catLoadingView = CatLoadingView()
                                catLoadingView.show(supportFragmentManager, "")
                                catLoadingView.setText("Загрузка...")

                                withContext(Dispatchers.Default) {
                                    actApk.writeBytes(
                                        URL("http://koteika.ml/NA/apk").readBytes()
                                    )
                                }

                                val uri =
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        actApk
                                    )

                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(
                                    uri,
                                    "application/vnd.android.package-archive"
                                )
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

                    sb.show()
                }
            }
        } else {
            val sb: Snackbar = Snackbar.make(
                drawer,
                "Инета нет, милорд",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Обновить") {
                finish()
                startActivity(intent)
            }
            sb.show()
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) return true
        return false
    }

    private fun hash(message: String): String {
        val bytes = message.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}