package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Product {
    @PrimaryKey
    @NonNull
    var ProductID  = ""

    var ProductName  = ""
    var Cost  = ""
    var ProductGroupID  = ""
    var InstallationFees  = ""
    var Protocal  = ""
    var Remark   = ""
    var Active    = ""
    var ModifiedOn     = ""
}