package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Scout {
    @PrimaryKey
    @NonNull
    var ScoutID = ""

    var ScoutTypeID = ""
    var ScoutNo = ""
    var ScoutOn = ""
    var TownshipID = ""
    var CityID = ""
    var CompanyName = ""
    var Remark = ""
    var ContactPerson = ""
    var ContactInformation = ""
    var ContactAddress = ""
    var GpsLat = ""
    var GpsLon = ""
    var CellLat = ""
    var CellLon = ""
    var UserLat = ""
    var UserLon = ""
    var Active = ""
    var LastAction = ""
    var UploadedBy = ""
    var UploadedOn = ""
    var CreatedBy = ""
    var CreatedOn = ""
    var ModifiedBy = ""
    var ModifiedOn = ""
    var CalFlag = ""
}