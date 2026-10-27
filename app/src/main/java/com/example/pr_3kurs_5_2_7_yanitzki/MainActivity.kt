package com.example.pr_3kurs_5_2_7_yanitzki

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etMovieTitle: EditText
    private lateinit var tvMovieInfo: TextView
    private lateinit var btnSaveMovie: Button
    private var currentMovie: Movie? = null
    private lateinit var db: AppDatabase

    private val apiToken = "G1ZZN5W-NE6MH7D-MXM3TC7-SY0BEXE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        // Связываем с ID кнопок и полей из XML вашего друга
        etMovieTitle = findViewById(R.id.etMovieTitle)
        tvMovieInfo = findViewById(R.id.tvMovieInfo)
        btnSaveMovie = findViewById(R.id.btnSaveMovie)
        val btnSearch = findViewById<Button>(R.id.btnSearchMovie)
        val btnGoToFavorites = findViewById<Button>(R.id.btnGoToFavorites)

        btnSearch.setOnClickListener {
            val title = etMovieTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                searchMovie(title)
            } else {
                Toast.makeText(this, "Введите название фильма", Toast.LENGTH_SHORT).show()
            }
        }

        btnSaveMovie.setOnClickListener {
            currentMovie?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    db.movieDao().insertMovie(it)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Фильм добавлен в избранное!", Toast.LENGTH_SHORT).show()
                        btnSaveMovie.visibility = View.GONE
                    }
                }
            }
        }

        btnGoToFavorites.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
    }

    private fun searchMovie(title: String) {
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
        val url = "https://kinopoisk.dev"

        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val rootJson = JSONObject(response)
                    val docsArray = rootJson.getJSONArray("docs")

                    if (docsArray.length() > 0) {
                        val movieJson = docsArray.getJSONObject(0)

                        val id = movieJson.getInt("id")
                        val name = movieJson.getString("name")
                        val year = movieJson.getInt("year")
                        val description = movieJson.optString("description", "Описание отсутствует.")
                        val kpRating = movieJson.getJSONObject("rating").optDouble("kp", 0.0)

                        currentMovie = Movie(id = id, name = name, year = year, rating = kpRating, description = description)

                        tvMovieInfo.text = """
                            Название: $name
                            Год выпуска: $year
                            Рейтинг КП: $kpRating
                            
                            Описание: $description
                        """.trimIndent()

                        btnSaveMovie.text = "Сохранить в избранное"
                        btnSaveMovie.visibility = View.VISIBLE
                    } else {
                        tvMovieInfo.text = "Фильм не найден."
                        btnSaveMovie.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("MyLog", "Ошибка парсинга: ${e.message}")
                    tvMovieInfo.text = "Ошибка обработки данных."
                }
            },
            { error ->
                Log.e("MyLog", "Volley error: $error")
                tvMovieInfo.text = "Ошибка сети. Проверьте подключение."
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["X-API-KEY"] = apiToken
                headers["accept"] = "application/json"
                return headers
            }
        }
        queue.add(stringRequest)
    }
}