package meow.sweetbread.nekosapikotlin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import java.lang.reflect.Array
import java.util.*


class SFW : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sfw, container, false)

        val linear = view.findViewById<LinearLayout>(R.id.SFW_linear)
        val buttons = listOf("Meow", "Neko", "Hug", "Kiss", "Pat", "Smug", "Lizard", "8ball",
                "Tickle", "Feed", "Goose", "Poke", "Ngif", "Baka", "Cuddle", "Wallpaper", "Avatar")
        val primColor = listOf(76, 175, 80)
        val endColor = listOf(27, 94, 32)
        val colors = mutableListOf(0, 0, 0)
        for (i in 0..2) colors[i] = (primColor[i]-endColor[i])/(buttons.size-1)


        for (name in buttons) {
            val button = Button(this.context)
            button.text = name
            button.setOnClickListener { listener(it) }
            button.transformationMethod = null;
            button.setBackgroundColor( Color.argb(100,
                    primColor[0]-colors[0]*buttons.indexOf(name),
                    primColor[1]-colors[1]*buttons.indexOf(name),
                    primColor[2]-colors[2]*buttons.indexOf(name)))

            linear.addView(button);
        }

        return view
    }

    fun listener(v: View) {
        val i = Intent(this.context, Viewer::class.java)
        val b = v as Button
        val text = b.getText().toString()

        i.putExtra("key", "url")

        when (text) {
            "Fox girl" -> i.putExtra("url", "https://nekos.life/api/v2/img/fox_girl")

            else ->
                i.putExtra("url", "https://nekos.life/api/v2/img/" + text.lowercase())
        }

        startActivity(i)
    }
}