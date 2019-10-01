package edu.gwu.androidtweetsfall2019

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock.sleep
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar

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

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tells Android which XML layout file to use for this Activity
        // The "R" is short for "Resources" (e.g. accessing a layout resource in this case)
        setContentView(R.layout.activity_main)

        val preferences: SharedPreferences = getSharedPreferences("android-tweets", Context.MODE_PRIVATE)

        // The "id" used here is what we had set in XML in the "id" field
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login)
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
            // Save the inputted username to file
            preferences
                .edit()
                .putString("SAVED_USERNAME", username.text.toString())
                .apply()

            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("LOCATION", "Washington D.C.")
            startActivity(intent)
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
        }
    }
}
