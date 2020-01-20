package com.systematic.netcore.utility

import java.text.SimpleDateFormat
import java.util.*

class Constants {
    companion object {
        val key_web_service_url = "webServiceUrl"
        //val asmx_name ="WebServiceNetCore.asmx"
        val asmx_name ="WebService_NetCore_Connector.asmx"

        val key_login_state = "login_state"
        val keyUserId = "UserID"
        val keyUserName = "UserName"

        val key_go_to = "go_to"
        val key_new_sacout = "new_sacout"
        val key_installation = "installation"
        val key_installation_list = "installation_list"
        val key_customer_list = "customer_list"
        val key_sale_order_list = "sale_order_list"
        val key_sale_order_details = "sale_order_details"
        val key_installation_details = "sale_install_details"

        //key for scout
        val key_filter_state = "filter_state"
        val key_is_downloaded = "is_downloaded"
        val key_object = "object"
        val key_is_edit = "is_edit"
        val key_last_city = "last_city"
        val key_last_township = "last_township"
        val key_selected_date = "selected_date"
        val key_is_saved = "is_saved"
        val key_imageLayout_Height = "imageLayoutHeight"
        val key_latitude = "latitude"
        val key_longitude = "longitude"

        //key for netcore
        val keyCustomerID = "customerId"
        val keyEngineerID = "engineerId"
        val keyProductGroupID = "productGroupId"
        val keyProductID = "productId"
        val keyTowerTypeID = "towerTypeId"
        val keyInstallationID = "installationID"
        val keyInstallationNo = "installationNo"

        val keySaleOrderID = "saleOrderId"
        val keyCustomerName = "customerName"

        val defaultDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss")
        val defaultDateFormatWithoutTime = SimpleDateFormat("yyyy-MM-dd")

        val dateTimeFormatForDataShow = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val dateTimeFormatForUpload = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss", Locale.US)
    }
}