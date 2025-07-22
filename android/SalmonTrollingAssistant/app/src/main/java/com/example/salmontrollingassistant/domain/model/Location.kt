package com.example.salmontrollingassistant.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isSaved: Boolean = false,
    val notes: String? = null
) : Parcelable