package com.podium.jokescloud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.podium.jokescloud.model.Joke

class JokeAdapter(private val jokeList: ArrayList<Joke>) :
    RecyclerView.Adapter<JokeAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentText: TextView = view.findViewById(R.id.content)
        val emailText: TextView = view.findViewById(R.id.email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_card_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = jokeList[position]
        holder.contentText.text = item.content
        holder.emailText.text = item.email
    }

    override fun getItemCount(): Int {
        return jokeList.size
    }
}