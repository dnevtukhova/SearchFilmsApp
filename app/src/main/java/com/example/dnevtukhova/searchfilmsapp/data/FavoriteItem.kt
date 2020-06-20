package com.example.dnevtukhova.searchfilmsapp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "favorite_table",
    indices = [
        Index(value = ["id"])]
)
data class FavoriteItem(
    @PrimaryKey
    var id: Int,
    var title: String,
    var description: String,
    var image: String?,
    var favorite: Boolean
) : Serializable