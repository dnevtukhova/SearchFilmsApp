package com.example.dnevtukhova.searchfilmsapp

import java.io.Serializable

data class FilmsItem (val id:Int, val title: Int, val description: Int, val image: Int, var favorite: Int) : Serializable
