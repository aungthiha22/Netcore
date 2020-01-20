package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.Customer
import com.systematic.netcore.database.model.Product
import com.systematic.netcore.database.model.TowerType

@Dao
interface TowerTypeDAO {

    @Insert
    fun insert (towerType: TowerType)

    @Query("Select * From TowerType")
    fun getTowerType() : List<TowerType>

    @Query("Select * From TowerType Where TowerTypeID =:id")
    fun getTowerTypeIDById(id : String) : List<TowerType>

    @Query("Select * From TowerType Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<TowerType>

    @Query("Delete from TowerType where TowerTypeID = :id")
    fun deleteById(id: String)
}