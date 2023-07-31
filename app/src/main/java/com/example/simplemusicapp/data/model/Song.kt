package com.example.simplemusicapp.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long = 0,
    val path: String = "",
    var title: String? = "",
    var artist: String? = "",
    val duration: Int = 0,
    val date: Long = 0,
    val image: Uri? = null,
) : Parcelable