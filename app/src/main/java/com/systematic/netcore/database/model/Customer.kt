package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Customer {
    @PrimaryKey
    @NonNull
    var CustomerID =""

    var CustomerNameEng=""
    var CustomerNameUnicode =""
    var CustomerNameZawgyi  =""
    var PhoneNo =""
    var Address =""
    var NRC  = ""
    var Email =""
    var Remark = ""
    var CustomerCode =""
    var Active =""
    var ModifiedOn =""
    var ContactDate  =""
    var GPS_Lat =""
    var GPS_Lon =""
    var Cell_Lat =""
    var Cell_Lon =""
    var User_Lat =""
    var User_Lon =""
    var CustomerGroupID =""
    var CompanyID =""
}