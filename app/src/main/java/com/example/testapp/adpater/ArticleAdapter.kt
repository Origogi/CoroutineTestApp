package com.example.testapp.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.R
import com.example.testapp.model.Article
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


interface ArticleLoader {
    suspend fun loadMore()
}

class ArticleAdapter(private val loader: ArticleLoader) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    private val articles = mutableListOf<Article>()
    private var loading = false

    class ViewHolder(val layout: LinearLayout, val feed: TextView, val title: TextView, val summary: TextView) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.article, parent, false) as LinearLayout

        val feed = layout.findViewById<TextView>(R.id.feed)
        val title = layout.findViewById<TextView>(R.id.title)
        val summary = layout.findViewById<TextView>(R.id.summary)

        return ViewHolder(layout = layout, feed = feed, title = title, summary = summary)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]

//        if (!loading && position >= articles.size - 2) {
//            loading = true
//
//            GlobalScope.launch {
//                loader.loadMore()
//                loading = false
//            }
//        }

        holder.feed.text = article.feed
        holder.summary.text = article.summary
        holder.title.text = article.title
    }

    override fun getItemCount() = articles.size

    fun add (article: Article) {
        this.articles.add(article)
        notifyDataSetChanged()
    }

    fun clear() {
        this.articles.clear()
        notifyDataSetChanged()
    }

    fun add (articles : List<Article>) {
        this.articles.addAll(articles)
        notifyDataSetChanged()
    }
}