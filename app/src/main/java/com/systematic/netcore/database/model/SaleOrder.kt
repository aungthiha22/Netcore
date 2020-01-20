package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SaleOrder {
    @PrimaryKey
    @NonNull
    var SaleOrderID = ""

    var CustomerID = ""
    var SaleManID = ""
    var ProductID = ""
    var OrderStatus = ""
    var Tax = ""
    var TotalAmount = ""
    var SN_GPS = ""
    var SN_Losses = ""
    var SN_Condition = ""
    //var ServayDate = ""
    var InternetCode = ""
    var Remark = ""
    //var OrderDate = ""
    var Active = ""
    var ModifiedOn = ""
    var OrderNo = ""
}