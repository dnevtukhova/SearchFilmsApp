package com.example.dnevtukhova.searchfilmsapp.data

import androidx.room.*
import io.reactivex.Flowable

@Dao
interface FilmsDao {
    @Query("SELECT * FROM films_table")
    fun getFilms(): Flowable<List<FilmsItem>>

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(films: List<FilmsItem>)

    @Query("DELETE FROM films_table")
    fun removeAllFilms()

    @Query("SELECT * FROM favorite_table")
    fun getAllFavorite(): Flowable<List<FavoriteItem>>

    @Query("SELECT * FROM favorite_table WHERE id= :id")
    fun getItemFavorite(id: Int): FavoriteItem

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInFavorite(film: FavoriteItem)

    @Query("DELETE FROM favorite_table")
    fun removeAllFavorite()

    @Delete
    fun deleteItemFavorite(favoriteItem: FavoriteItem)

    @Query("UPDATE favorite_table SET favorite = :isFavorite where id = :id")
    fun updateIsFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE films_table SET favorite = :isFavorite where id = :id")
    fun setFilms(id: Int, isFavorite: Boolean)

    @Query("UPDATE films_table SET watchLater = :isWatchLater where id = :id")
    fun setWatchLater(id: Int, isWatchLater: Boolean)

    @Update
    fun updateFilms(filmsItem: FilmsItem)

    @Update
    fun updateFavorite(favoriteItem: FavoriteItem)

    //WatchLater_table
    @Query("SELECT * FROM watchLater_table")
    fun getAllWatchLater(): Flowable<List<WatchLaterItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInWatchLater(film: WatchLaterItem)

    @Delete
    fun deleteItemWatchLater(watchLaterItem: WatchLaterItem)

    @Query("SELECT * FROM watchLater_table WHERE id= :id")
    fun getItemWatchLater(id: Int): WatchLaterItem

    @Query ("UPDATE watchLater_table SET dateToWatch = :dateToWatch where id = :id")
    fun updateTimeToWatch(id: Int, dateToWatch: Long)
}