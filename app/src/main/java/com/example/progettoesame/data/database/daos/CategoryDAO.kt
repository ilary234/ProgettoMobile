package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.progettoesame.data.database.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDAO {

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Query("SELECT * FROM categories")
    fun getAll(): Flow<List<Category>>

    @Insert
    suspend fun insertAll(categories : List<Category>)
}