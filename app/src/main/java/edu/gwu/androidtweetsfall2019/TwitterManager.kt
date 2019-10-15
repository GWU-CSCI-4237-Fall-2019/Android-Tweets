package edu.gwu.androidtweetsfall2019

import android.util.Base64
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder

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

    // Twitter's APIs are protected by OAuth. APIs from other companies (like Yelp) do not require OAuth.
    fun retrieveOAuthToken(
        apiKey: String,
        apiSecret: String
    ): String {

        // Twitter requires us to format our OAuth API call in a certain way, so the code below
        // is deriving from the steps in Twitter's documentation:
        // https://developer.twitter.com/en/docs/basics/authentication/overview/application-only

        // Encoding for a URL -- converts things like spaces into %20
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val encodedSecret = URLEncoder.encode(apiSecret, "UTF-8")

        // Concatenate the two together, with a colon inbetween
        val combinedEncoded = "$encodedKey:$encodedSecret"

        // Base-64 encode the combined string
        // https://en.wikipedia.org/wiki/Base64
        val base64Combined = Base64.encodeToString(
            combinedEncoded.toByteArray(), Base64.NO_WRAP)

        // OAuth is a POST API call, so we need to create the actual "body" of the request (e.g.
        // the data we want to send). This request body is specific to an OAuth call.
        val requestBody = "grant_type=client_credentials"
            .toRequestBody(
                contentType = "application/x-www-form-urlencoded".toMediaType()
            )

        val request = Request.Builder()
            .url("https://api.twitter.com/oauth2/token")
            .header("Authorization", "Basic $base64Combined")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseString = response.body?.string()

        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse JSON
            val json = JSONObject(responseString)
            val token = json.getString("access_token")

            return token
        } else {
            // API request failed - ideally we should think about returning null or an Exception
            return ""
        }

        // One improvement we can make is also to cache the OAuth token, since it can be reused for all Twitter API
        // requests in this session.
    }

    fun retrieveTweets(
        oAuthToken: String,
        latLng: LatLng
    ): List<Tweet> {
        // Data setup
        val searchTerm = "Android"
        val latitude = latLng.latitude
        val longitude = latLng.longitude
        val radius = "30mi"

        // Build our request to turn - for now, using a hardcoded OAuth token
        val request = Request.Builder()
            .url("https://api.twitter.com/1.1/search/tweets.json?q=$searchTerm&geocode=$latitude,$longitude,$radius")
            .header("Authorization", "Bearer $oAuthToken")
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