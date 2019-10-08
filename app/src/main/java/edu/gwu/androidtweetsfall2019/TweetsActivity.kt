package edu.gwu.androidtweetsfall2019

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.doAsync

class TweetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweets)

        // Retrieve data out of the intent, as supplied by the MapsActivity
        val address: String = intent.getStringExtra("address")
        val latitude: Double = intent.getDoubleExtra("latitude", 0.0)
        val longitude: Double = intent.getDoubleExtra("longitude", 0.0)

        // Kotlin shorthand for setTitle(...)
        // Calling getString will retrieve a string from our strings.xml (based on the ID we pass)
        // the 2nd param instructs Android to substitute the location into the string where we specified %1$s
        title = getString(R.string.tweets_title, address)

        recyclerView = findViewById(R.id.recyclerView)

        // Set the direction of our list to be vertical
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Networking must be done on a background thread
        doAsync {
            val twitterManager = TwitterManager()

            // retrieveTweets can throw an Exception on connection / network errors
            try {
                val tweets = twitterManager.retrieveTweets(
                    LatLng(latitude, longitude)
                )

                // The UI can only be updated from the UI Thread
                runOnUiThread {
                    // Create the adapter and assign it to the RecyclerView
                    recyclerView.adapter = TweetsAdapter(tweets)
                }
            } catch(e: Exception) {
                // The UI can only be updated from the UI Thread
                runOnUiThread {
                    Toast.makeText(
                        this@TweetsActivity,
                        "Error retrieving Tweets",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun getFakeTweets(): List<Tweet> {
        return listOf(
            Tweet(
                name = "Nick Capurso",
                handle = "@nickcapurso",
                content = "We're learning lists!",
                iconUrl = "https://...."
            ),
            Tweet(
                name = "Android Central",
                handle = "@androidcentral",
                content = "NVIDIA Shield TV vs. Shield TV Pro: Which should I buy?",
                iconUrl = "https://...."
            ),
            Tweet(
                name = "DC Android",
                handle = "@DCAndroid",
                content = "FYI - another great integration for the @Firebase platform",
                iconUrl = "https://...."
            ),
            Tweet(
                name = "KotlinConf",
                handle = "@kotlinconf",
                content = "Can't make it to KotlinConf this year? We have a surprise for you. We'll be live streaming the keynotes, closing panel and an entire track over the 2 main conference days. Sign-up to get notified once we go live!",
                iconUrl = "https://...."
            ),
            Tweet(
                name = "Android Summit",
                handle = "@androidsummit",
                content = "What a #Keynote! @SlatteryClaire is the Director of Performance at Speechless, and that's exactly how she left us after her amazing (and interactive!) #keynote at #androidsummit. #DCTech #AndroidDev #Android",
                iconUrl = "https://...."
            ),
            Tweet(
                name = "Fragmented Podcast",
                handle = "@FragmentedCast",
                content = ".... annnnnnnnnd we're back!\n\nThis week @donnfelker talks about how it's Ok to not know everything and how to set yourself up mentally for JIT (Just In Time [learning]). Listen in here: \nhttp://fragmentedpodcast.com/episodes/135/ ",
                iconUrl = "https://...."
            ),
            Tweet(
                name = "Jake Wharton",
                handle = "@JakeWharton",
                content = "Free idea: location-aware physical password list inside a password manager. Mostly for garage door codes and the like. I want to open my password app, switch to the non-URL password section, and see a list of things sorted by physical distance to me.",
                iconUrl = "https://...."
            ),
            Tweet(
                name = "Droidcon Boston",
                handle = "@droidconbos",
                content = "#DroidconBos will be back in Boston next year on April 8-9!",
                iconUrl = "https://...."
            ),
            Tweet(
                name = "AndroidWeekly",
                handle = "@androidweekly",
                content = "Latest Android Weekly Issue 327 is out!\nhttp://androidweekly.net/ #latest-issue  #AndroidDev",
                iconUrl = "https://...."
            ),
            Tweet(
                name = ".droidconSF",
                handle = "@droidconSF",
                content = "Drum roll please.. Announcing droidcon SF 2018! November 19-20 @ Mission Bay Conference Center. Content and programming by @tsmith & @joenrv.",
                iconUrl = "https://...."
            )
        )
    }
}