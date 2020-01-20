package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TowerType {
    @PrimaryKey
    @NonNull
    var TowerTypeID = ""

    var TowerTypeName = ""
    var TowerTypeCode = ""
    var Remark = ""
    var Active = ""
    var ModifiedOn = ""
}