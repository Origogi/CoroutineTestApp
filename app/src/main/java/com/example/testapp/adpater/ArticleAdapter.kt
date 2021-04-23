package com.example.testapp.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.R
import com.example.testapp.model.Article

class ArticleAdapter : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    val articles = mutableListOf<Article>()

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

        holder.feed.text = article.feed
        holder.summary.text = article.summary
        holder.title.text = article.title
    }

    override fun getItemCount() = articles.size

    fun add (articles : List<Article>) {
        this.articles.addAll(articles)
        notifyDataSetChanged()
    }
}