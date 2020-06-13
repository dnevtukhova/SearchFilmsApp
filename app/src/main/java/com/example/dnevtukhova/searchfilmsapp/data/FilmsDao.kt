package com.example.dnevtukhova.searchfilmsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface FilmsDao {
    @Query("select * from films_table")
    fun getFilms(): LiveData<List<FilmsItem>>

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(films: List<FilmsItem>)

    @Query("DELETE FROM films_table")
    fun removeAllFilms()

    @Query("select * from favorite_table")
    fun getAllFavorite(): LiveData<List<FavoriteItem>>

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

    @Update
    fun updateFilms(filmsItem: FilmsItem)

    @Update
    fun updateFavorite(favoriteItem: FavoriteItem)
}