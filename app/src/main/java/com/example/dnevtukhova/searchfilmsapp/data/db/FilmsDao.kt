package com.example.dnevtukhova.searchfilmsapp.data.db

import androidx.room.*
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import io.reactivex.Flowable

@Dao
interface FilmsDao {
    @Query("SELECT * FROM films_table")
    fun getFilms(): Flowable<List<FilmsItem>>

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(films: List<FilmsItem>)

    @Query("SELECT * FROM films_table WHERE favorite = 0")
    fun getAllFavorite(): Flowable<List<FilmsItem>>

    @Query("UPDATE films_table SET favorite = :isFavorite where id = :id")
    fun setFilms(id: Int, isFavorite: Boolean)

    @Query("UPDATE films_table SET watchLater = :isWatchLater where id = :id")
    fun setWatchLater(id: Int, isWatchLater: Boolean)

    @Update
    fun updateFilms(filmsItem: FilmsItem)

    @Query("SELECT * FROM films_table WHERE watchLater = 0")
    fun getAllWatchLater(): Flowable<List<FilmsItem>>

    @Query ("UPDATE films_table SET dateToWatch = :dateToWatch where id = :id")
    fun updateTimeToWatch(id: Int, dateToWatch: Long)
}