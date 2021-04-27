package com.example.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.adpater.ArticleAdapter
import com.example.testapp.adpater.ArticleLoader
import com.example.testapp.producer.ArticleProducer
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

class SearchActivity : AppCompatActivity(), ArticleLoader {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ArticleAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val searcher = Searcher()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ArticleAdapter(this)
        recyclerView = this.findViewById<RecyclerView>(R.id.articles).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        findViewById<Button>(R.id.searchButton).setOnClickListener {
            viewAdapter.clear()
            GlobalScope.launch {
                search()
            }
        }
    }

    override suspend fun loadMore() {

        val producer = ArticleProducer.producer

        if (!producer.isClosedForReceive) {

            withContext(Main) {
                findViewById<View>(R.id.progressBar).visibility = View.VISIBLE

                recyclerView.alpha = 0.3f
            }
            delay(2000)

            val articles = producer.receive()
            withContext(Main) {
                recyclerView.alpha = 1.0f

                findViewById<View>(R.id.progressBar).visibility = View.GONE
                viewAdapter.add(articles)
            }
        }
    }

    private suspend fun search() {
        val query = findViewById<EditText>(R.id.searchText).text.toString()

        val channel = searcher.search(query)

        while (!channel.isClosedForReceive) {
            val article = channel.receive()

            GlobalScope.launch(Main) {
                viewAdapter.add(article)
            }
        }
    }
}