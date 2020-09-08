package com.example.dnevtukhova.searchfilmsapp.data.db

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class FilmsDbTest {
    @Rule
    @JvmField
    val countingTaskExecutorRule = CountingTaskExecutorRule()

    private lateinit var _db: FilmsDb
    val db: FilmsDb
        get() = _db

    @Before
    fun initDb() {
        _db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FilmsDb::class.java
        ).build()
    }

    @After
    fun closeDb() {
        _db.close()
    }
}