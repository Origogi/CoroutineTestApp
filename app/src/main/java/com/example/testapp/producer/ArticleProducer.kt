package com.example.testapp.producer

import com.example.testapp.model.Article
import com.example.testapp.model.Feed
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

object ArticleProducer {

    private val dispatcher = newFixedThreadPoolContext(2, "IO")
    private val factory = DocumentBuilderFactory.newInstance()
    private val feeds = listOf(
        Feed("npr1", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("npr2", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("npr3", "https://www.npr.org/rss/rss.php?id=1001"),
        )

    val producer = GlobalScope.produce(dispatcher) {
        feeds.forEach {
            send(fetchArticle(it))
        }
    }

    private fun fetchArticle(feed: Feed): List<Article> {

        val builder = factory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)
        return (0 until news.childNodes.length)
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
}