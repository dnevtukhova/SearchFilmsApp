package com.example.dnevtukhova.searchfilmsapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem

@Database(entities = [FilmsItem::class], version = 3)
abstract class FilmsDb : RoomDatabase() {
    abstract fun getFilmsDao(): FilmsDao
}