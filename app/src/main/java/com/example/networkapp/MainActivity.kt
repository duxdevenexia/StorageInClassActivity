package com.example.networkapp

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

// TODO (1: Fix any bugs)
// TODO (2: Add function saveComic(...) to save comic info when downloaded
// TODO (3: Automatically load previously saved comic when app starts)

class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    lateinit var titleTextView: TextView
    lateinit var descriptionTextView: TextView
    lateinit var numberEditText: EditText
    lateinit var showButton: Button
    lateinit var comicImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)

        titleTextView = findViewById<TextView>(R.id.comicTitleTextView)
        descriptionTextView = findViewById<TextView>(R.id.comicDescriptionTextView)
        numberEditText = findViewById<EditText>(R.id.comicNumberEditText)
        showButton = findViewById<Button>(R.id.showComicButton)
        comicImageView = findViewById<ImageView>(R.id.comicImageView)

        // Check if there is a saved comic and load it
        loadSavedComic()


        showButton.setOnClickListener {
            val comicId = numberEditText.text.toString()
            if (comicId.isNotEmpty()) {
                downloadComic(comicId)
            } else {
                Toast.makeText(this, "Please enter a comic number", Toast.LENGTH_SHORT).show()
            }
        }


    }

    // Fetches comic from web as JSONObject
    private fun downloadComic(comicId: String) {
        val url = "https://xkcd.com/$comicId/info.0.json"

        val comicRequest = JsonObjectRequest(
            url,
            { response ->
                showComic(response)
                saveComic(response)  // Save the downloaded comic
            },
            { error -> showError(error) }
        )

        requestQueue.add(comicRequest)
    }



    // Display a comic for a given comic JSON object
    private fun showComic(comicObject: JSONObject) {
        val title = comicObject.optString("title", "No Title")
        val description = comicObject.optString("alt", "No Description")
        val imageUrl = comicObject.optString("img", "")

        titleTextView.text = title
        descriptionTextView.text = description

        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).into(comicImageView)
        } else {
            comicImageView.setImageResource(R.drawable.ic_launcher_background)  // Use a placeholder if the image URL is empty
        }
        Picasso.get().load(comicObject.getString("img")).into(comicImageView)
    }

    // Show error if the network request fails
    private fun showError(error: Throwable) {
        val errorMessage = when (error) {
            is com.android.volley.NoConnectionError -> "No internet connection. Please check your connection."
            is com.android.volley.ServerError -> "Server error occurred. Please try again later."
            else -> "Error fetching comic: ${error.localizedMessage}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

    }

    // Implement this function
    private fun saveComic(comicObject: JSONObject) {
        // Get SharedPreferences instance
        val sharedPreferences: SharedPreferences = getSharedPreferences("ComicPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert the comic JSON data to a string
        val comicJsonString = comicObject.toString()

        // Save the comic data under a key (e.g., "last_comic")
        editor.putString("last_comic", comicJsonString)
        editor.apply()  // Apply the changes asynchronously


    }

    // Function to load the previously saved comic
    private fun loadSavedComic() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("ComicPrefs", Context.MODE_PRIVATE)
        val savedComicJson = sharedPreferences.getString("last_comic", null)

        // If there's a saved comic, display it
        savedComicJson?.let {
            try {
                val comicObject = JSONObject(it)
                showComic(comicObject)
            } catch (e: Exception) {
                e.printStackTrace() // Handle any errors in case the saved data is corrupted
            }
        }
    }


}