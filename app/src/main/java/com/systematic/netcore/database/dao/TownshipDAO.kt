package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.City
import com.systematic.netcore.database.model.Township
import com.systematic.netcore.database.model.User

@Dao
interface TownshipDAO {

    @Insert
    fun insert (township: Township)

    @Query("Select * From Township")
    fun getTownship() : List<Township>

    @Query("Select * From Township Where TownshipID =:id")
    fun getTownshipById(id : String) : List<Township>

    @Query("Select * From Township Where CityID =:cityID")
    fun getTownshipByCityID(cityID : String) : List<Township>

    @Query("Select * From Township Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<Township>

    @Query("Delete from Township where TownshipID = :id")
    fun deleteById(id: String)
}