package com.example.networkapp

import android.content.SharedPreferences
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    lateinit var titleTextView: TextView
    lateinit var descriptionTextView: TextView
    lateinit var numberEditText: EditText
    lateinit var showButton: Button
    lateinit var comicImageView: ImageView
    private val myFile = "my_file"
    private lateinit var file: File
    private var save = false
    private  lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)

        titleTextView = findViewById<TextView>(R.id.comicTitleTextView)
        descriptionTextView = findViewById<TextView>(R.id.comicDescriptionTextView)
        numberEditText = findViewById<EditText>(R.id.comicNumberEditText)
        showButton = findViewById<Button>(R.id.showComicButton)
        comicImageView = findViewById<ImageView>(R.id.comicImageView)
        preferences = getPreferences(MODE_PRIVATE)
        file = File(filesDir,myFile)

        if(file.exists()){
            try{
                val br = BufferedReader(FileReader(file))
                val text = StringBuilder()
                var line: String?
                while (br.readLine().also{line =it} != null){
                    text.append(line)
                    text.append('\n')
                }
                br.close()
                downloadComic(text.toString())
            }
            catch(e: IOException){
                e.printStackTrace()
            }
        }

        showButton.setOnClickListener {
            downloadComic(numberEditText.text.toString())
        }

        if (intent?.action == Intent.ACTION_VIEW){
            intent.data?.path?.run{
                downloadComic(split("/")[1])
            }
        }

        findViewById<Button>(R.id.button).setOnClickListener{
            val intent  = Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                Uri.parse("package:${packageName}"))
            startActivity(intent)
        }
    }

    private fun downloadComic (comicId: String) {
        val url = "https://xkcd.com/$comicId/info.0.json"
        requestQueue.add (
            JsonObjectRequest(url, {
                try{
                    val outputStream = FileOutputStream(file)
                    outputStream.write(numberEditText.text.toString().toByteArray())
                    outputStream.close()
                }catch(e: Exception){
                    e.printStackTrace()
                }
                showComic(it)}, {
            })
        )
    }

    private fun showComic (comicObject: JSONObject) {
        titleTextView.text = comicObject.getString("title")
        descriptionTextView.text = comicObject.getString("alt")
        Picasso.get().load(comicObject.getString("img")).into(comicImageView)
    }


}