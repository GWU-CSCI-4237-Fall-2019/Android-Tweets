package edu.gwu.androidtweetsfall2019

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TweetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweets)

        // getStringExtra retrieves the value associated with the key "LOCATION", casting it to a String
        // If the key does not exist in the Intent, null will be returned, so you may also want to use
        // hasExtra(...) to check if your key is in the intent (if it's possible for it to be missing)
        val location: String = intent.getStringExtra("LOCATION")

        // This is a combination of two Kotlin shorthands:
        //   1. setTitle("...")
        //   2. "Android Tweets near " + location
        title = "Android Tweets near $location"
    }
}