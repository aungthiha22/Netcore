package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.Customer
import com.systematic.netcore.database.model.CustomerGroup

@Dao
interface CustomerGroupDAO {

    @Insert
    fun insert (customerGroup: CustomerGroup)

    @Query("Select * From CustomerGroup")
    fun getCustomerGroup() : List<CustomerGroup>

    @Query("Select * From CustomerGroup Where CustomerGroupID =:id")
    fun getCustomerGroupById(id : String) : List<CustomerGroup>

    @Query("Delete from CustomerGroup where CustomerGroupID = :id")
    fun deleteById(id: String)
}