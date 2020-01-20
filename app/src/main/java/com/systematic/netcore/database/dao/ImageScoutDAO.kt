package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.*

@Dao
interface ImageScoutDAO {

    @Insert
    fun insert(imageScout: ImageScout)

    @Query("Select * From ImageScout")
    fun getImageScout(): List<ImageScout>

    @Query("Select * From ImageScout Where Active = '1'")
    fun getImageScoutByActive(): List<ImageScout>

    @Query("Select * From ImageScout Where ScoutID = :id")
    fun getImageByScoutID(id: String): List<ImageScout>

    @Query("Select * From ImageScout Order By ModifiedOn DESC")
    fun getLastModifiedOn(): List<ImageScout>

    @Query("Select * From ImageScout Where ScoutID = :scoutId")
    fun getImageByFlagYes(scoutId :String): List<ImageScout>

    @Query("Update ImageScout Set Active = '1', ImageData = '' , TempSave = 'Yes' Where ImagePath = :path")
    fun changeStatusForDelete(path : String)

    @Query("Update ImageScout Set Active = '0'")
    fun changeStatusForUploadComplete()

    @Query("Select * From ImageScout Where ScoutID = :scoutId And TempSave = :flag")
    fun getImageByFlagNo(scoutId :String, flag :String): List<ImageScout>

    @Query("Delete from ImageScout where ScoutID = :id")
    fun deleteById(id: String)
}