package com.example.dnevtukhova.searchfilmsapp.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity (tableName = "films_table",
        indices = [
            Index(value = ["id"])]
    )
    data class FilmsItem (
    var id: Int,
    var title: String,
    var description: String,
    var image: String?,
    var favorite: Boolean,
    var watchLater: Boolean,
    var dateToWatch: Long?): Parcelable {
    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)
    var idFilms = 0
}
