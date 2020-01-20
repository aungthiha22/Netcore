package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.City
import com.systematic.netcore.database.model.Installation
import com.systematic.netcore.database.model.InstallationInfo
import com.systematic.netcore.database.model.User

@Dao
interface InstallationInfoDAO {

    @Insert
    fun insert (installationInfo: InstallationInfo)

    @Query("Select * From InstallationInfo")
    fun getInstallationInfo() : List<InstallationInfo>

    @Query("Select * From InstallationInfo Where InstallationInfoID =:id")
    fun getInstallationInfoById(id : String) : List<InstallationInfo>

    @Query("Delete from InstallationInfo where InstallationInfoID = :id")
    fun deleteById(id: String)
}