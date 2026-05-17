package com.example.progettoesame.ui.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
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
    if (sendIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(shareIntent)
    }
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

fun createImageUriInGallery(ctx: Context) : Uri? {
    val name =  "Ricetta_${System.currentTimeMillis()}"
    val values = ContentValues()
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    values.put(MediaStore.Images.Media.DISPLAY_NAME, name)

    return ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}
