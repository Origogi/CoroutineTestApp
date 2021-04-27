package com.example.testapp

import com.example.testapp.model.Article
import com.example.testapp.model.Feed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class Searcher {
    fun search(query : String) : ReceiveChannel<Article> {
        val channel = Channel<Article>(150)

        feeds.forEach { feed ->
            GlobalScope.launch(dispatcher) {
                search(feed, channel, query)
            }
        }
        return channel

    }

    private val dispatcher = newFixedThreadPoolContext(3, "IO")
    private val factory = DocumentBuilderFactory.newInstance()
    private val feeds = listOf(
        Feed("npr1", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("npr2", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("npr3", "https://www.npr.org/rss/rss.php?id=1001"),
    )

    private suspend fun search(feed: Feed, channel : SendChannel<Article>, query: String) {

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

                if (title.toLowerCase().contains(query.toLowerCase()) || summary.toLowerCase().contains(query.toLowerCase()) ) {
                    channel.send(Article(feed.name, title, summary))
                }
            }
    }

}

