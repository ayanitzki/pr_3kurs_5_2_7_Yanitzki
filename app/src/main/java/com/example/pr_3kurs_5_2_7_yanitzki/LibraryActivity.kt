package com.example.pr_3kurs_5_2_7_yanitzki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        db = AppDatabase.getDatabase(this)

        val btnBackToMain = findViewById<Button>(R.id.btnBackToMain)
        btnBackToMain.setOnClickListener {
            finish() // Закрывает этот экран и возвращает на предыдущий (MainActivity)
        }

        val rvMovies = findViewById<RecyclerView>(R.id.rvMovies) // Оставляем ID разметки друга
        rvMovies.layoutManager = LinearLayoutManager(this)
        adapter = MovieAdapter()
        rvMovies.adapter = adapter

        loadMovies()
    }

    private fun loadMovies() {
        CoroutineScope(Dispatchers.IO).launch {
            val movies = db.movieDao().getAllMovies()
            runOnUiThread {
                adapter.setMovies(movies)
            }
        }
    }

    inner class MovieAdapter : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {
        private var movies: List<Movie> = emptyList()

        fun setMovies(newMovies: List<Movie>) {
            movies = newMovies
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return MovieViewHolder(view)
        }

        override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
            val movie = movies[position]
            holder.text1.text = movie.name
            holder.text2.text = "Год: ${movie.year}, Рейтинг КП: ${movie.rating}"

            // Удаление по долгому нажатию
            holder.itemView.setOnLongClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    db.movieDao().deleteMovie(movie)
                    loadMovies()
                }
                true
            }
        }

        override fun getItemCount() = movies.size

        inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text1: TextView = view.findViewById(android.R.id.text1)
            val text2: TextView = view.findViewById(android.R.id.text2)
        }
    }
}