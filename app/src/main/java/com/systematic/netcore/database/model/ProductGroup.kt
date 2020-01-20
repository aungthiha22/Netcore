package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ProductGroup {
    @PrimaryKey
    @NonNull
    var ProductGroupID   = ""

    var ProductGroupName   = ""
    var Protocal   = ""
    var Active   = ""
    var Remark   = ""
    var ModifiedOn   = ""
}