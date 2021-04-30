package com.example.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.adpater.ArticleAdapter
import com.example.testapp.adpater.ArticleLoader
import com.example.testapp.producer.ArticleProducer
import com.example.testapp.searcher.ResultsCounter
import com.example.testapp.searcher.Searcher
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.CoroutineContext

class SearchActivity() : AppCompatActivity(), ArticleLoader, CoroutineScope {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ArticleAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val searcher = Searcher()
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        job = Job()

        viewManager = LinearLayoutManager(this)
        viewAdapter = ArticleAdapter(this)
        recyclerView = this.findViewById<RecyclerView>(R.id.articles).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        findViewById<Button>(R.id.searchButton).setOnClickListener {
            viewAdapter.clear()
            launch(IO) {
                search()
            }
        }
    }

    override suspend fun loadMore() {

        val producer = ArticleProducer.producer

        if (!producer.isClosedForReceive) {
            findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
            recyclerView.alpha = 0.3f

            val articles = producer.receive()
            recyclerView.alpha = 1.0f
            findViewById<View>(R.id.progressBar).visibility = View.GONE
            viewAdapter.add(articles)

        }
    }

    private suspend fun search() {
        val query = findViewById<EditText>(R.id.searchText).text.toString()

        val channel = searcher.search(query)

        val notifications = ResultsCounter.getNotification()

        while (!channel.isClosedForReceive) {
            val article = channel.receive()

            val newMount = "Result : ${notifications.receive()}"

            withContext(Main) {
                findViewById<TextView>(R.id.results).text = newMount
                viewAdapter.add(article)
            }
        }
    }
}