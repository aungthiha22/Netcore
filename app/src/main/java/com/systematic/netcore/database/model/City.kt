package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class City {
    @PrimaryKey
    @NonNull
    var CityID = ""

    var CityNameEng = ""
    var CityNameZawgyi = ""
    var CityNameUnicode = ""
    var ModifiedOn = ""
    var Active = ""
}