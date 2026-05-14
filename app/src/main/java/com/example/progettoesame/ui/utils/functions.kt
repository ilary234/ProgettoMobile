package com.example.progettoesame.ui.utils

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun shareRecipe(context: Context, title: String, previewImageUrl: String) {
    val link = "link della release di GitHub" //TODO

    val message = """
        $previewImageUrl
        Prova questa ricetta: $title
        Scarica l'app per vedere i dettagli: $link
    """.trimIndent()

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Condividi con:")
    context.startActivity(shareIntent)
}

fun formatTime(time: Int) : String {
    val hours = time / 60
    val minutes = time % 60

    val formattedTime = when {
        time < 60 -> "$time min"
        minutes == 0 -> "${hours} h"
        else -> "${hours} h ${minutes} min"
    }
    return formattedTime
}

fun getFormattedTimeStamp() : String {
    val zoneId = ZoneId.of("Europe/Rome")
    val italianTime = ZonedDateTime.now(zoneId)

    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    return  italianTime.format(formatter)
}

object AuthState { //per utilizzare il valore nelle varie classi bisogna scrivere AuthState.isLoggedIn.value
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _userId = mutableStateOf<String?>(null)
    val userId: State<String?> = _userId

    private val _userEmail = mutableStateOf<String?>(null)
    val userEmail: State<String?> = _userEmail

    fun setLoggedIn(id: String, email: String) {
        _userId.value = id
        _userEmail.value = email
        _isLoggedIn.value = true
    }

    fun setLoggedOut() {
        _userId.value = null
        _userEmail.value = null
        _isLoggedIn.value = false
    }
}