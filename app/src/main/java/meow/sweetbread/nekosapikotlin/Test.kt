package meow.sweetbread.nekosapikotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton


class Test : Fragment() {
    lateinit var circularProgressDrawable: CircularProgressDrawable

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        val linear = view.findViewById<LinearLayout>(R.id.Test_linear)
        val buttons = listOf("Hentai")

        for (name in buttons) {
            val button = Button(this.context)
            button.text = name
            button.setOnClickListener { listener(it) }
            button.transformationMethod = null;

            linear.addView(button);
        }

        return view
    }

    fun listener(v: View) {
        val i = Intent(this.context, Viewer::class.java)
        val b = v as Button
        val text = b.getText().toString()

        when (text) {
            "Hentai" -> {
                i.putExtra("url", "http://koteika.ml/hentai")
                i.putExtra("key", "url")
            }
        }

        startActivity(i)
    }
}