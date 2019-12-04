package edu.gwu.androidtweetsfall2019

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    /*
      We cannot assign our view-based variables during initialization, since we do not set up our
      UI until setContentView(...) in onCreate (otherwise, findViewById(...) will return null.

      Kotlin requires variables be given an initial value, so we have two options:
        1. Declare these variables as nullable (e.g. private var username: EditText? = null)
            - This is annoying, since you need to do a null check every time you access
        2. Declare these variables as lateinit var (and non-null)
            - lateinit is a "promise" to the compiler that they the variable will be set, just not right now
              and, after being set, will never be null in this case.
     */

    private lateinit var username: EditText

    private lateinit var password: EditText

    private lateinit var login: Button

    private lateinit var signUp: Button

    private lateinit var progressBar: ProgressBar

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tells Android which XML layout file to use for this Activity
        // The "R" is short for "Resources" (e.g. accessing a layout resource in this case)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val preferences: SharedPreferences = getSharedPreferences("android-tweets", Context.MODE_PRIVATE)

        // The "id" used here is what we had set in XML in the "id" field
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login)
        signUp = findViewById(R.id.signUp)
        progressBar = findViewById(R.id.progressBar)

        // Kotlin shorthand for login.setEnabled(false)
        login.isEnabled = false

        username.setText(preferences.getString("SAVED_USERNAME", ""))

        username.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        // An OnClickListener is an interface with a single function, so you can use lambda-shorthand
        // The lambda is called when the user pressed the button
        // https://developer.android.com/reference/android/view/View.OnClickListener
        login.setOnClickListener {
            firebaseAnalytics.logEvent("login_clicked", null)

            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()

            firebaseAuth
                .signInWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        firebaseAnalytics.logEvent("login_success", null)

                        val currentUser: FirebaseUser? = firebaseAuth.currentUser
                        val email = currentUser?.email
                        Toast.makeText(this, "Logged in as $email", Toast.LENGTH_SHORT).show()

                        // Save the inputted username to file
                        preferences
                            .edit()
                            .putString("SAVED_USERNAME", username.text.toString())
                            .apply()

                        val intent = Intent(this, MapsActivity::class.java)
                        intent.putExtra("LOCATION", "Washington D.C.")
                        startActivity(intent)
                    } else {
                        val exception = task.exception

                        // Example of logging some extra metadata (the error reason) with our analytic
                        val reason = if (exception is FirebaseAuthInvalidCredentialsException) "invalid_credentials" else "connection_failure"
                        val bundle = Bundle()
                        bundle.putString("error_type", reason)

                        firebaseAnalytics.logEvent("login_failed", bundle)

                        Toast.makeText(this, "Registration failed: $exception", Toast.LENGTH_SHORT).show()

                    }
                }
        }

        signUp.setOnClickListener {
            firebaseAnalytics.logEvent("signup_clicked", null)

            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()
            firebaseAuth
                .createUserWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAnalytics.logEvent("signup_success", null)
                        val currentUser: FirebaseUser? = firebaseAuth.currentUser
                        val email = currentUser?.email
                        Toast.makeText(this, "Registered as $email", Toast.LENGTH_SHORT).show()

                        showNotification()
                    } else {
                        firebaseAnalytics.logEvent("signup_failed", null)
                        val exception = task.exception
                        Toast.makeText(this, "Registration failed: $exception", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun showNotification() {
        val tweetsIntent = Intent(this, TweetsActivity::class.java)
        tweetsIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        tweetsIntent.putExtra("address", "2121 I St NW")
        tweetsIntent.putExtra("state", "Virginia")
        tweetsIntent.putExtra("latitude", 38.8997145)
        tweetsIntent.putExtra("longitude", -77.0485992)

        // val pendingIntent = PendingIntent.getActivity(this, 0, tweetsIntent, 0)
        val pendingIntentBuilder = TaskStackBuilder.create(this)
        pendingIntentBuilder.addNextIntentWithParentStack(tweetsIntent)

        val pendingIntent = pendingIntentBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_check_white)
            .setContentTitle("Android Tweets")
            .setContentText("Welcome to Android Tweets!")
            .addAction(0, "Go To Virginia", pendingIntent)

        NotificationManagerCompat.from(this).notify(0, builder.build())
    }

    private fun createNotificationChannel() {
        // Only needed for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Notifications"
            val descriptionText = "The app's default notification set"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel("default", name, importance)
            channel.description = descriptionText

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    // A TextWatcher is an interface with three functions, so we cannot use lambda-shorthand
    // The functions are called accordingly as the user types in the EditText
    // https://developer.android.com/reference/android/text/TextWatcher
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(newString: CharSequence, start: Int, before: Int, count: Int) {
            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()
            val enabled: Boolean = inputtedUsername.isNotEmpty() && inputtedPassword.isNotEmpty()

            // Kotlin shorthand for login.setEnabled(enabled)
            login.isEnabled = enabled
            signUp.isEnabled = enabled
        }
    }
}
