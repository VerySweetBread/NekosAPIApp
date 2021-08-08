package meow.sweetbread.nekosapikotlin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout


class NSFW : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_nsfw, container, false)

        val linear = view.findViewById<LinearLayout>(R.id.NSFW_linear)
        val buttons = listOf("Erok", "lewdk", "Feetg", "Solog", "Gecg", "Erokemo", "Kuni", "Femdom",
                "Futanari", "Trap", "Boobs", "Tits", "Blowjob", "Hentai", "Hololewd", "Cum",
                "Cum jpg", "Anal", "Random hentai gif", "Lewd neko", "Lesbian", "yuri",
                "NSFW neko gif", "Pussy", "Pussy jpg", "Classic")

        var primColor = listOf(229, 115, 115)
        var endColor = listOf(183, 28, 28)
        var colors = mutableListOf(0, 0, 0)
        for (i in 0..2) {
            colors[i] = (primColor[i]-endColor[i])/(buttons.size-1)
        }

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

        when (text) {
            "Random hentai gif" -> {
                i.putExtra("url", "https://nekos.life/api/v2/img/Random_hentai_gif")
                i.putExtra("key", "url")
            }

            "Lewd neko" -> {
                i.putExtra("url", "https://nekos.life/api/lewd/neko")
                i.putExtra("key", "neko")
            }

            "Lesbian" -> {
                i.putExtra("url", "https://nekos.life/api/v2/img/les")
                i.putExtra("key", "url")
            }

            "NSFW neko gif" -> {
                i.putExtra("url", "https://nekos.life/api/v2/img/nsfw_neko_gif")
                i.putExtra("key", "url")
            }

            "Pussy jpg" -> {
                i.putExtra("url", "https://nekos.life/api/v2/img/pussy_jpg")
                i.putExtra("key", "url")
            }

            "Cum jpg" -> {
                i.putExtra("url", "https://nekos.life/api/v2/img/cum_jpg")
                i.putExtra("key", "url")
            }

            else -> {
                i.putExtra("url", "https://nekos.life/api/v2/img/" + text.toLowerCase())
                i.putExtra("key", "url")
            }
        }

        startActivity(i)
    }
}