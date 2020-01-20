package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.City
import com.systematic.netcore.database.model.User

@Dao
interface CityDAO {

    @Insert
    fun insert (city: City)

    @Query("Select * From City")
    fun getCity() : List<City>

    @Query("Select * From City Where CityID =:id")
    fun getCityById(id : String) : List<City>

    @Query("Select * From City Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<City>

    @Query("Delete from City where CityID = :id")
    fun deleteById(id: String)
}