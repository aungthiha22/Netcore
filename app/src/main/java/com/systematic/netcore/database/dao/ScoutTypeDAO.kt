package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.City
import com.systematic.netcore.database.model.ScoutType
import com.systematic.netcore.database.model.Township

@Dao
interface ScoutTypeDAO {

    @Insert
    fun insert (scoutType: ScoutType)

    @Query("Select * From ScoutType")
    fun getScoutType() : List<ScoutType>

    @Query("Select * From ScoutType Where ScoutTypeID =:id")
    fun getScoutTypeById(id : String) : List<ScoutType>

    @Query("Select * From ScoutType Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<ScoutType>

    @Query("Delete from ScoutType where ScoutTypeID = :id")
    fun deleteById(id: String)
}