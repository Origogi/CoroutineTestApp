package com.example.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private val netDispatcher = newSingleThreadContext(name = "ServiceCall")
    private val factory = DocumentBuilderFactory.newInstance()

    private fun fetchHeadlines(): List<String> {
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
        val news = xml.getElementsByTagName("channel").item(0)
        return (0 until news.childNodes.length)
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

    private fun asyncLoadNews() {
        GlobalScope.launch(netDispatcher) {
            val headlines = fetchHeadlines()
            findViewById<TextView>(R.id.newsCount).let {
                GlobalScope.launch(Dispatchers.Main) {
                    it.text = "Found ${headlines.size} news"

                }
            }
        }


    }
}