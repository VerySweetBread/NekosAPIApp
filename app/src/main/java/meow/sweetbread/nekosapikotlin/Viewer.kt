package meow.sweetbread.nekosapikotlin

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast
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
        setContentView(R.layout.activity_viewer)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

        val panel = findViewById<LinearLayout>(R.id.panel)
        findViewById<FloatingActionButton>(R.id.reload_button).setOnClickListener { reload() }

        findViewById<FloatingActionButton>(R.id.hide).setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.slide_out)
            panel.startAnimation(animation)
            panel.findViewById<LinearLayout>(R.id.panel).visibility = GONE
        }

        findViewById<ImageView>(R.id.imageView1).setOnClickListener {
            panel.visibility = VISIBLE
            panel.animation = AnimationUtils.loadAnimation(this, R.anim.slide_in)
            panel.animate()
        }

        url = intent.extras!!.getString("url").toString()
        key = intent.extras!!.getString("key").toString()
        image = findViewById(R.id.imageView1)

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
            Glide.with(applicationContext)
                .load(image_url)
                .placeholder(circularProgressDrawable)
                .into(image)
        }
    }

    fun reload() {
        GlobalScope.launch(Dispatchers.Main) {
            var text = ""
            GlobalScope.async { text = URL(url).readText() }.await()
            val json = JSONObject(text)
            last_image_url = image_url
            image_url = json.getString(key)

            try {
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
                    this.runOnUiThread { msg?.let { toast(it) } }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
        }.start()
    }

    private fun statusMessage(url: String, directory: File, status: Int): String {
        return when (status) {
            DownloadManager.STATUS_PENDING -> "Обработка..."
            DownloadManager.STATUS_PAUSED -> "Пауза"
            DownloadManager.STATUS_RUNNING -> "Загрузка..."
            DownloadManager.STATUS_SUCCESSFUL -> "Загрузка успешна. Файл в $directory" + File.separator + url.substring(
                    url.lastIndexOf("/") + 1
            )
            else -> "Ошибка"
        }
    }
}