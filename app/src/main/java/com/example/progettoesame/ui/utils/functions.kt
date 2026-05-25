package com.example.progettoesame.ui.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

fun shareRecipe(ctx: Context, title: String, previewImageUrl: Uri) {
    val link = "https://github.com/ilary234/ProgettoMobile/releases/latest"

    val message = """
        Prova questa ricetta: $title
        
        Scarica l'app per vedere i dettagli: $link
    """.trimIndent()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val loader =ImageLoader(ctx)
            val request = ImageRequest.Builder(ctx)
                .data(previewImageUrl)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap

            if (bitmap != null) {
                val cachePath = File(ctx.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "recipe_preview.jpg")
                val stream = FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
                stream.close()

                val contentUri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TITLE, title)
                    type = "image/jpeg"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    val shareIntent = Intent.createChooser(sendIntent, "Condividi con:")
                    shareIntent.clipData = android.content.ClipData.newRawUri("", contentUri)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (sendIntent.resolveActivity(ctx.packageManager) != null) {
                        ctx.startActivity(shareIntent)
                    }
                }
            } else {
                withContext(Dispatchers.Main) { shareTextOnly(ctx, message) }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { shareTextOnly(ctx, message) }
        }
    }
}

private fun shareTextOnly(ctx: Context, message: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Condividi con:")
    if (sendIntent.resolveActivity(ctx.packageManager) != null) {
        ctx.startActivity(shareIntent)
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

fun getRoundedRating(rating: Float): Double {
    return (rating * 10).roundToInt() / 10.0
}
fun formatRating(roundedRating: Double): String {
    return String.format(Locale.US, "%.1f", roundedRating)
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


object AuthState {
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _userId = mutableStateOf<String?>(null)
    val userId: State<String?> = _userId

    private val _userEmail = mutableStateOf<String?>(null)
    val userEmail: State<String?> = _userEmail

    private val _username = mutableStateOf<String?>(null)
    val username: State<String?> = _username

    private val _isResetPasswordMode = mutableStateOf(false)
    val isResetPasswordMode: State<Boolean> = _isResetPasswordMode

    fun setLoggedIn(id: String, email: String, username: String, isResetMode: Boolean = false) {
        _userId.value = id
        _userEmail.value = email
        _username.value = username
        _isResetPasswordMode.value = isResetMode
        _isLoggedIn.value = true
    }

    fun enableResetModeOnly() {
        _isResetPasswordMode.value = true
        _isLoggedIn.value = true
    }

    fun disableResetModeOnly() {
        _isResetPasswordMode.value = false
    }

    fun updateUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun setLoggedOut() {
        _userId.value = null
        _userEmail.value = null
        _username.value
        _isResetPasswordMode.value = false
        _isLoggedIn.value = false
    }
}