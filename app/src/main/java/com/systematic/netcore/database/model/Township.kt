package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Township {
    @PrimaryKey
    @NonNull
    var TownshipID = ""

    var TownshipNameEng = ""
    var TownshipNameZawgyi = ""
    var TownshipNameUnicode = ""
    var CityID = ""
    var Active = ""
    var ModifiedOn = ""
}