package com.systematic.netcore.objects

import java.io.Serializable

class ScoutObject : Serializable{
    var scoutID = ""
    var scoutTypeId = ""
    var scoutNo = ""
    //var scoutOn: Calendar = GregorianCalendar()
    var scoutOn = ""
    var townshipID = ""
    var townshipName = ""
    var companyName = ""
    var remark = ""

    var cityID = ""

    var contactPerson = ""
    var contactInformation = ""
    var contactAddress = ""

    var gpsLat = ""
    var gpsLng = ""
    var cellLat = ""
    var cellLng = ""
    var userLat = ""
    var userLng = ""

    var active:Int = 0
    var lastAction = ""
    var uploadBy = ""
    var uploadOn = ""
    var createBy = ""
    var createOn = ""
    var modifiedBy = ""
    var modifiedOn = ""

    var timeAgo = ""
    var scoutType = ""
}