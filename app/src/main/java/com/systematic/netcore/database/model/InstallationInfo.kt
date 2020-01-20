package com.systematic.netcore.database.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class InstallationInfo {
    @PrimaryKey
    @NonNull
    var InstallationInfoID = ""
    var CustomerID  = ""
    var PPOEName = ""
    var PPOEPassword  = ""
    var WifiName = ""
    var WifiPassword = ""
    var VLAN = ""
    var ManagementMAC = ""
    var UserMAC = ""
    var SearialNo = ""
    var PortNo = ""
    var Node = ""
    var TowerTypeID = ""
    var Active = ""
    var CreatedBy = ""
    var CreatedOn = ""
    var ModifiedBy = ""
    var ModifiedOn = ""
    var LastAction = ""
    var CustomerNo = ""
    var InstallationID = ""

}