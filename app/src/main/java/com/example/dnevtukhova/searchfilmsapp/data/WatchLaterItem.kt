package com.example.dnevtukhova.searchfilmsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchLater_table")
data class WatchLaterItem(
    @PrimaryKey
    var id: Int,
    var title: String,
    var description: String,
    var image: String?,
    var favorite: Boolean,
    var watchLater: Boolean,
    var dateToWatch: Long
)