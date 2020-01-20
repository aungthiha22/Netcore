package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class BillingInfo {
    @PrimaryKey
    @NonNull

    var BillingTitle = ""
    var BillingNo = ""
    var BillingFor = ""
    var Status = ""
    var BillingDate = ""
    var BillType = ""
    var RepeatType = ""
    var EndDate = ""
    var SaleOrder = ""
    var ContactInfo = ""
    var aboutBilling = ""
    var Amount = ""
    var Currency =""
    var RemainderOne =""
    var RemainderTwo = ""
    var NextBillDate =""
    var NextTargetBillDate = ""
    var ImportantLevel = ""
    var RemainderEmail =""
    var LatePolicy =""
    var NotifyUser1 = ""
    var NotifyUser2 =""


}