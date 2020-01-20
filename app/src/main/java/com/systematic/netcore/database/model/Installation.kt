package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Installation {
    @PrimaryKey
    @NonNull
    var InstallationID = ""
    var InstallationNo  = ""
    var CustomerID = ""
    var InstallationUserID  = ""
    var ProductID = ""
    var InternetDeveice = ""
    var FiberCableLength = ""
    var FiberSwitchQty = ""
    var Remark = ""
    var SNGPS = ""
    var LossRemark = ""
    var InstallationStartDate = ""
    var InstallationEndDate = ""
    var Duration = ""
    var Discount = ""
    var InstallationFee = ""
    var RouterFee = ""
    var ConfigFee = ""
    var RelocationFee = ""
    var InstallationStatus = ""
    var InstallationTotalAmount = ""
    var Active = ""
    var CreatedBy = ""
    var CreatedOn = ""
    var ModifiedBy = ""
    var ModifiedOn = ""
    var LastAction = ""
    var Speed = ""
    var InternetCode = ""
    var Tax = ""
    var ProductQty = ""
    var SaleOrderID = ""
}