package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.City
import com.systematic.netcore.database.model.Installation
import com.systematic.netcore.database.model.User

@Dao
interface InstallationDAO {

    @Insert
    fun insert (installation: Installation)

    @Query("Select * From Installation")
    fun getInstallation() : List<Installation>

    @Query("Select * From Installation Where InstallationID =:id")
    fun getInstallationById(id : String) : List<Installation>

    @Query("Delete from Installation where InstallationID = :id")
    fun deleteById(id: String)
}