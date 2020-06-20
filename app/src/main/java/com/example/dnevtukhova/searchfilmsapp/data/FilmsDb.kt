package com.example.dnevtukhova.searchfilmsapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FilmsItem::class, FavoriteItem::class], version = 1)
abstract class FilmsDb : RoomDatabase() {
    abstract fun getFilmsDao(): FilmsDao
}