package com.example.progettoesame.ui.utils

import android.content.Context
import android.content.Intent

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