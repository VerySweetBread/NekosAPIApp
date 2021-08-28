package meow.sweetbread.nekosapikotlin

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL

class Viewer : AppCompatActivity() {
    lateinit var url: String
    lateinit var key: String
    lateinit var image: ImageView
    var image_url: String = ""
    var last_image_url: String = ""
    var needToReload = true
    lateinit var circularProgressDrawable: CircularProgressDrawable
    var msg: String? = ""
    var lastMsg = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_viewer)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        supportActionBar?.hide()

        val buttons = findViewById<BottomNavigationView>(R.id.buttons)
        val header = findViewById<LinearLayout>(R.id.header)

        window.statusBarColor = Color.argb(40, 0, 0, 0)

        fun showButtons() {
            buttons.visibility = VISIBLE
            buttons.animation = AnimationUtils.loadAnimation(this, R.anim.slide_in)
            buttons.animate()

            header.visibility = VISIBLE
            header.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            header.animate()

            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        fun hideButtons() {
            val animation = AnimationUtils.loadAnimation(this, R.anim.slide_out)
            buttons.startAnimation(animation)
            buttons.visibility = GONE

            header.animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            header.animate()
            header.visibility = GONE

            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }

        findViewById<ImageView>(R.id.imageView).setOnClickListener {
            when (buttons.visibility) {
                VISIBLE -> hideButtons()
                GONE    -> showButtons()
            }
        }
        findViewById<ImageButton>(R.id.download_button).setOnClickListener { download() }
        buttons.setOnNavigationItemSelectedListener {
            reload()
            return@setOnNavigationItemSelectedListener true
        }


        url = intent.extras!!.getString("url").toString()
        key = intent.extras!!.getString("key").toString()
        image = findViewById(R.id.imageView)

        circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 10f
        circularProgressDrawable.centerRadius = 100f
//        circularProgressDrawable.arrowEnabled = true
        circularProgressDrawable.setColorSchemeColors(R.drawable.progress)
        circularProgressDrawable.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("needToReload", false)
        outState.putString("image_url", image_url)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        needToReload = savedInstanceState.getBoolean("needToReload")
        image_url = savedInstanceState.getString("image_url")!!
    }

    override fun onResume() {
        super.onResume()

        if (needToReload) {
            reload()
        } else {
            needToReload = true
            findViewById<TextView>(R.id.name).text = image_url.substring(
                url.lastIndexOf("/")
            )
            Glide.with(applicationContext)
                .load(image_url)
                .placeholder(circularProgressDrawable)
                .into(image)
        }
    }

    fun reload() {
        GlobalScope.launch(Dispatchers.Main) {
            var text = ""
            withContext(Dispatchers.Default) {
                text = URL(url).readText()
            }
            val json = JSONObject(text)
            last_image_url = image_url
            image_url = json.getString(key)

            try {
                findViewById<TextView>(R.id.name).text = image_url.substring(
                    url.lastIndexOf("/")
                )
                Glide.with(applicationContext)
                    .load(image_url)
                    .placeholder(circularProgressDrawable)
                    .into(image)
            } catch (e: com.bumptech.glide.load.engine.GlideException) {
                reload()
            }

        }
    }

    fun download() {
        if (ContextCompat.checkSelfPermission(this@Viewer, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED) {

            val directory = File(Environment.DIRECTORY_DOWNLOADS)

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val downloadUri = Uri.parse(image_url)

            val request = DownloadManager.Request(downloadUri).apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(image_url.substring(image_url.lastIndexOf("/") + 1))
                    .setDescription("")
                    .setDestinationInExternalPublicDir(
                        directory.toString(),
                        image_url.substring(image_url.lastIndexOf("/") + 1)
                    )
            }

            val downloadId = downloadManager.enqueue(request)
            val query = DownloadManager.Query().setFilterById(downloadId)

            Thread {
                var downloading = true
                while (downloading) {
                    val cursor: Cursor = downloadManager.query(query)
                    cursor.moveToFirst()
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                    }
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    msg = statusMessage(url, directory, status)
                    if (msg != lastMsg) {
                        val sb = Snackbar.make(
                            findViewById(R.id.viewerActivity),
                            msg!!,
                            Snackbar.LENGTH_SHORT
                        )
                        this.runOnUiThread { sb.show() }
                        lastMsg = msg ?: ""
                    }
                    cursor.close()
                }
            }.start()
        } else {
            val sb = Snackbar.make(findViewById(R.id.viewerActivity), "Запрещено системой", Snackbar.LENGTH_LONG)
                .setAction("Разрешить") {
                    ActivityCompat.requestPermissions(this@Viewer, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
                }
            sb.show()
        }
    }

    private fun statusMessage(url: String, directory: File, status: Int): String {
        return when (status) {
//            DownloadManager.STATUS_PENDING -> "Обработка..."
//            DownloadManager.STATUS_PAUSED -> "Пауза"
            DownloadManager.STATUS_RUNNING -> "Загрузка..."
            DownloadManager.STATUS_SUCCESSFUL -> "Загрузка успешна. Файл в $directory"
            else -> "Ошибка"
        }
    }
}