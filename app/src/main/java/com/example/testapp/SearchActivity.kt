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
                ResultsCounter.reset()
                search()
            }
        }

        launch() {
            updateResult()
        }
    }

    override suspend fun loadMore() {

        val producer = ArticleProducer.producer

        if (!producer.isClosedForReceive) {
            val articles = producer.receive()
            viewAdapter.add(articles)
        }
    }

    private suspend fun updateResult() {
        println("hello")

        val notifications = ResultsCounter.getNotification()

        while (!notifications.isClosedForReceive) {
            val count = notifications.receive()
            println(count)
            val newMount = "Result : $count"
            withContext(Main) {
                findViewById<TextView>(R.id.results).text = newMount
            }
        }
    }

    private suspend fun search() {
        val query = findViewById<EditText>(R.id.searchText).text.toString()
        val channel = searcher.search(query)

        while (!channel.isClosedForReceive) {
            val article = channel.receive()
            withContext(Main) {
                viewAdapter.add(article)
            }
        }
    }
}