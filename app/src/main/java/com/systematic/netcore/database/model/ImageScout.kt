package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ImageScout {
    @PrimaryKey
    @NonNull
    var ImageID = ""

    var ImageName = ""
    var ScoutID = ""
    var ImagePath = ""

    var ImageData = ""
    var TempSave = "No"
    var Active = ""

    var CreatedBy = ""
    var CreatedOn = ""
    var ModifiedBy = ""
    var ModifiedOn = ""
    var LastAction = ""
}