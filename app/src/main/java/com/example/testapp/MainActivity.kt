package com.example.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private val defDsp = newFixedThreadPoolContext(2, "IO")
    private val factory = DocumentBuilderFactory.newInstance()
    private val feeds = listOf("https://www.npr.org/rss/rss.php?id=1001",
            "https://www.npr.org/rss/rss.php?id=1001",
            "invalid url")

    private fun asyncFetchHeadlines(feed: String, dispatcher: CoroutineDispatcher) = GlobalScope.async(dispatcher) {
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse(feed)
        val news = xml.getElementsByTagName("channel").item(0)
        (0 until news.childNodes.length)
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    it.getElementsByTagName("title").item(0).textContent
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        asyncLoadNews()
    }

    private fun asyncLoadNews(dispatcher: CoroutineDispatcher = defDsp) {
        GlobalScope.launch {
            val requests = mutableListOf<Deferred<List<String>>>()

            feeds.mapTo(requests) {

                asyncFetchHeadlines(it, defDsp)
            }

            requests.forEach {
                it.join()
            }

            val headlines = requests.filter { !it.isCancelled }.flatMap { it.getCompleted() }
            findViewById<TextView>(R.id.newsCount).let {
                GlobalScope.launch(Dispatchers.Main) {
                    it.text = "Found ${headlines.size} news in ${requests.size} feeds"
                }
            }
        }
    }
}