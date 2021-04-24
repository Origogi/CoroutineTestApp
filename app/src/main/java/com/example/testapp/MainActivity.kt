package com.example.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.adpater.ArticleAdapter
import com.example.testapp.model.Article
import com.example.testapp.model.Feed
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private val defDsp = newFixedThreadPoolContext(2, "IO")
    private val factory = DocumentBuilderFactory.newInstance()
    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("inv", "invalid url")
    )

    private lateinit var articles: RecyclerView
    private lateinit var viewAdapter: ArticleAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private fun asyncFetchArticles(feed: Feed, dispatcher: CoroutineDispatcher) =
        GlobalScope.async(dispatcher) {
            delay(1000)
            val builder = factory.newDocumentBuilder()
            val xml = builder.parse(feed.url)
            val news = xml.getElementsByTagName("channel").item(0)
            (0 until news.childNodes.length)
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    val title = it.getElementsByTagName("title").item(0).textContent
                    val summary =
                        it.getElementsByTagName("description").item(0).textContent.let { text ->
                            if (!text.startsWith("div") && text.contains("<div")) {
                                text.substring(0, text.indexOf(",div"))
                            } else {
                                text
                            }
                        }


                    Article(feed.name, title, summary)
                }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ArticleAdapter()
        articles = this.findViewById<RecyclerView>(R.id.articles).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        asyncLoadNews()
    }

    private fun asyncLoadNews(dispatcher: CoroutineDispatcher = defDsp) {
        GlobalScope.launch {
            val requests = mutableListOf<Deferred<List<Article>>>()

            feeds.mapTo(requests) {
                asyncFetchArticles(it, defDsp)
            }

            requests.forEach {
                it.join()
            }

            val articles = requests.filter { !it.isCancelled }.flatMap { it.getCompleted() }
            val failed = requests.filter { it.isCancelled }

            val obtained = requests.size - failed.size

            launch(Dispatchers.Main) {
                findViewById<View>(R.id.progressBar).visibility = View.GONE
                viewAdapter.add(articles)
            }


        }
    }
}