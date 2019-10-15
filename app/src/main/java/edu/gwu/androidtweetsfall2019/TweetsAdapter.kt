package edu.gwu.androidtweetsfall2019

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class TweetsAdapter(val tweets: List<Tweet>) : RecyclerView.Adapter<TweetsAdapter.TweetsViewHolder>() {

    // onCreateViewHolder is called when the RecyclerView needs a new XML layout to be loaded for a row
    // Open & parse our XML file for our row and return the ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetsViewHolder {
        // Open & parse our XML file
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_tweet, parent, false)

        // Create a new ViewHolder
        return TweetsViewHolder(view)
    }

    // Returns the number of rows to render
    override fun getItemCount(): Int {
        return tweets.size
    }

    // onBindViewHolder is called when the RecyclerView is ready to display a new row at [position]
    // and needs you to fill that row with the necessary data.
    //
    // It passes you a ViewHolder, either from what you returned from onCreateViewHolder *or*
    // it's passing you an existing ViewHolder as a part of the "recycling" mechanism.
    override fun onBindViewHolder(holder: TweetsViewHolder, position: Int) {
        val currentTweet = tweets[position]

        holder.name.text = currentTweet.name
        holder.handle.text = currentTweet.handle
        holder.content.text = currentTweet.content

        // Uncomment to turn on debug indicators
        // Picasso
        //     .get()
        //     .setIndicatorsEnabled(true)

        // Load the profile picture into our icon ImageView
        Picasso
            .get()
            .load(currentTweet.iconUrl)
            .into(holder.icon)
    }

    // A ViewHolder is a class which *holds* references to *views* that we care about in each
    // individual row. The findViewById function is somewhat inefficient, so the idea is to the lookup
    // for each view once and then reuse the object.
    class TweetsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)

        val name: TextView = view.findViewById(R.id.username)

        val handle: TextView = view.findViewById(R.id.handle)

        val content: TextView = view.findViewById(R.id.tweet_content)
    }
}