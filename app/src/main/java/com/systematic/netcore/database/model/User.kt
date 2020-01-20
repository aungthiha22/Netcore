package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User {
    @PrimaryKey
    @NonNull
    var UserID=""
    var UserCode=""
    var UserName=""
    var UserDeviceID=""
    var Password=""
    var Ref_Type=""
    var OrgID=""
    var OrgCode=""
    var OrgType=""
    var LastLogin = ""
    var DepartmentID=""
    var PositionID=""
    var Active=1
    var CreatedOn=""
    var ModifiedOn=""
}