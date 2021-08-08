package meow.sweetbread.nekosapikotlin

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import java.io.File

class WhatsNew : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whats_new)

        val internalStorageDir = filesDir
        val WN = File(internalStorageDir, "WhatsNew.txt")
        findViewById<TextView>(R.id.WN_view).text = WN.readText()

        findViewById<Button>(R.id.return_).setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit { putBoolean("firstLaunch", false) }
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}