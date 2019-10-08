package edu.gwu.androidtweetsfall2019

import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class TwitterManager {

    // We don't have to supply a value here, since we have an init block.
    private val okHttpClient: OkHttpClient

    // An init block is similar to having a longer constructor in Java - it allows us to run
    // extra code during initialization. All variables must be set by the end of the init block.
    init {
        // Turn on console logging for our network traffic, useful during development
        val builder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        builder.addInterceptor(logging)
        okHttpClient = builder.build()
    }

    fun retrieveTweets(latLng: LatLng): List<Tweet> {
        // Data setup
        val searchTerm = "Android"
        val latitude = latLng.latitude
        val longitude = latLng.longitude
        val radius = "30mi"

        // Build our request to turn - for now, using a hardcoded OAuth token
        val request = Request.Builder()
            .url("https://api.twitter.com/1.1/search/tweets.json?q=$searchTerm&geocode=$latitude,$longitude,$radius")
            .header("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAAJ6N8QAAAAAABppHnTpssd0Hrsdpsi6vYN%2BTfks%3DFY1iVemJdKF5HWRZhQnHRbGpwXJevg3sYyvYC3R53sHCfOJvFk")
            .build()

        // Calling .execute actually makes the network request, blocking the thread until the server
        // responds, or the request times out.
        //
        // If there are any connection or network errors, .execute will throw an Exception.
        val response = okHttpClient.newCall(request).execute()
        val responseString: String? = response.body?.string()

        val tweets = mutableListOf<Tweet>()

        // Confirm that we retrieved a successful (e.g. 200) response with some body content
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse the JSON response that was sent back by the server
            val jsonObject = JSONObject(responseString)
            val statuses = jsonObject.getJSONArray("statuses")

            for (i in 0 until statuses.length()) {
                val tweetJson = statuses.getJSONObject(i)

                // Get the Tweet's content
                val content = tweetJson.getString("text")

                // Get the inner user object
                val userJson = tweetJson.getJSONObject("user")

                // Get user-specific fields
                val name = userJson.getString("name")
                val handle = userJson.getString("screen_name")
                val profilePicUrl = userJson.getString("profile_image_url")

                val tweet = Tweet(
                    name = name,
                    handle = handle,
                    iconUrl = profilePicUrl,
                    content = content
                )

                tweets.add(tweet)
            }
        }

        return tweets
    }
}