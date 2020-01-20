package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class CustomerGroup {
    @PrimaryKey
    @NonNull
    var CustomerGroupID = ""

    var CustomerGroupCode = ""
    var GroupName = ""
    var GroupDescription = ""
    var Remark = ""
    var Active = ""
}