package com.example.dnevtukhova.searchfilmsapp.data.entity

import com.google.gson.annotations.SerializedName

data class FilmModel (
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val description: String,
    @SerializedName("poster_path") val image: String
)
