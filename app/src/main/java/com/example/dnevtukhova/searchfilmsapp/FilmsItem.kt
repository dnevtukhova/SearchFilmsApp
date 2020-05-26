package com.example.dnevtukhova.searchfilmsapp

import java.io.Serializable

data class FilmsItem (
    var id: Int,
    var title: String,
    var description: String,
    var image: String?,
    var favorite: Boolean) : Serializable
