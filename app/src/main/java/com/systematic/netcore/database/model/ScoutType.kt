package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ScoutType {
    @PrimaryKey
    @NonNull
    var ScoutTypeID=""

    var ScoutTypeNameEng=""
    var ScoutTypeNameZawGyi=""
    var ScoutTypeNameUnicode=""
    var ModifiedOn = ""
    var Active = ""
}