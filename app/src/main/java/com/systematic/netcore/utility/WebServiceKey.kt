package com.systematic.netcore.utility

class WebServiceKey {

    class KeyForResponse {
        companion object {
            var key_response_data = "ResponseData"
            var key_response_info = "ResponseInfo"

            var key_response_info_code = "Code"
            var key_response_info_message = "Message"
        }
    }

    class KeyForRequestData {
        companion object{
            var key_user_code = "usercode"
            var key_user_id = "userID"
            var key_user_password = "password"
            var key_user_modified_date = "ModifiedDate"
            var key_user_sign = "sign"
            var key_receipt_search_date = "search_date"
            var key_modified_date = "modifiedDate"
        }
    }

    class KeyForUserResponse{
        companion object{
            var key_user_id = "UserID"
            var key_user_name = "UserName"
            var key_org_id = "OrgID"
            var key_org_code = "OrgCode"
        }
    }

    class KeyForCityResponse{
        companion object{
            var key_city_id = "CityID"
            var key_city_code = "CityCode"
            var key_city_name_eng = "CityNameEng"
            var key_city_name_zg = "CityNameZawgyi"
            var key_city_name_uni = "CityNameUnicode"
            var key_activity = "Active"
            var key_modified_on = "ModifiedOn"
            var key_region_id = "RegionID"
        }
    }

    class UploadScoutData{
        companion object{
            val key_info = "info"
            val key_sign = "sign"

            val key_crm_scout = "Crm_Scout"
            val key_scout_id = "ScoutID"
            val key_township_id = "TownshipID"
            val key_scoutType_id = "ScoutTypeID"
            val key_contact_person = "ContactPerson"
            val key_contact_information = "ContactInformation"
            val key_contact_address = "ContactAddress"
            val key_company_name = "CompanyName"
            val key_scout_on = "ScoutOn"
            val key_scout_no = "ScoutNo"
            val key_uploaded_by = "UploadedBy"
            val key_uploaded_on = "UploadedOn"
            val key_remark = "Remark"
            val key_gps_lat = "GPS_Lat"
            val key_gps_lon = "GPS_Lon"
            val key_cell_lat = "Cell_Lat"
            val key_cell_lon = "Cell_Lon"
            val key_user_lat = "User_Lat"
            val key_user_lon = "User_Lon"
            val key_active = "Active"
            val key_created_by = "CreatedBy"
            val key_created_on = "CreatedOn"
            val key_modified_by = "ModifiedBy"
            val key_modified_on = "ModifiedOn"
            val key_last_action = "LastAction"
            val key_result = "SaveScoutInfoResult"
        }
    }

    class UploadSacoutImage {
        companion object ScoutImage {

            val key_info = "info"

            val key_id = "ScoutImageID"
            val key_name = "ScoutImageName"
            val key_scout_id = "ScoutId"
            val key_path = "ScoutImagePath"
            val key_data = "ScoutImageData"
        }
    }

}