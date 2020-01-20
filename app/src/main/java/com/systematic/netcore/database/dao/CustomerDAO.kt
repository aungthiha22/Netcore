package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.Customer

@Dao
interface CustomerDAO {

    @Insert
    fun insert (customer: Customer)

    @Query("Select * From Customer")
    fun getCustomer() : List<Customer>

    @Query("Select * From Customer Where CustomerID =:id")
    fun getCustomerById(id : String) : List<Customer>

    @Query("Select * From Customer Where CustomerCode =:codeOrName Or CustomerNameEng = :codeOrName")
    fun getCusByCusNoAndName(codeOrName : String) : List<Customer>

    @Query("Select * From Customer Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<Customer>

    @Query("Delete from Customer where CustomerID = :id")
    fun deleteById(id: String)
}