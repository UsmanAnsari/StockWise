package com.uansari.stockwise.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.uansari.stockwise.data.local.database.StockWiseDatabase
import org.junit.After
import org.junit.Before

abstract class BaseDaoTest {
    protected lateinit var database: StockWiseDatabase

    @Before
    open fun setUp() {
        // Create in-memory database (destroyed when process killed)
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), StockWiseDatabase::class.java
        ).allowMainThreadQueries()  // OK for testing only!
            .build()
    }

    @After
    open fun tearDown() {
        database.close()
    }
}