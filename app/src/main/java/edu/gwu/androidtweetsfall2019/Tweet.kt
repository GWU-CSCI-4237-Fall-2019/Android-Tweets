package edu.gwu.androidtweetsfall2019

import java.io.Serializable

data class Tweet(
    val name: String,
    val handle: String,
    val content: String,
    val iconUrl: String
) : Serializable {
    // Firebase DB requires custom classes to declare an empty constructor
    constructor() : this("", "" ,"", "")
}