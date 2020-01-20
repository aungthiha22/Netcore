package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.systematic.netcore.database.model.City
import com.systematic.netcore.database.model.Scout
import com.systematic.netcore.database.model.ScoutType
import com.systematic.netcore.database.model.Township

@Dao
interface ScoutDAO {

    @Insert
    fun insert (scout: Scout)

    @Query("Select * From Scout")
    fun getScout() : List<Scout>

    @Query ("Select * From Scout Where ScoutID = :id")
    fun getAllByID(id : String) : List<Scout>

    @Query ("Select * From Scout Where UploadedOn = '' And CreatedBy = :userId And ScoutID = :id")
    fun getAllForUploadByScoutId(userId : String,id : String) : List<Scout>

     @Query ("Select * From Scout Where CreatedBy = :id And UploadedOn = ''")
    fun getAllByUserID(id : String) : List<Scout>

    @Query ("Select * From Scout Where ModifiedBy = :userId")
    fun getScoutByUserId(userId : String) : List<Scout>

    @Query("Select * From Scout Where ScoutOn Like :date And CreatedBy = :userId Order By CreatedOn DESC ")
    fun getAllWithFilter(date : String,userId : String) : List<Scout>

    @Query("Select * From Scout Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<Scout>

    @RawQuery
    abstract fun update(query: SupportSQLiteQuery): Boolean

    @Query("Delete from Scout where ScoutID = :id")
    fun deleteById(id: String)
}