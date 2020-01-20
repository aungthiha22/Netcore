package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.BillingInfo
import com.systematic.netcore.database.model.Customer

@Dao
interface BillingDAO {

    @Insert
    fun insert (billingInfo : BillingInfo)

    @Query("Select * From BillingInfo")
    fun getBillingInfo() : List<BillingInfo>

    /*@Query("Select * From Customer Where CustomerID =:id")
    fun getBillingInfoById(id : String) : List<BillingInfo>

    *//*@Query("Select * From BillingInfo Where CustomerCode =:codeOrName Or CustomerNameEng = :codeOrName")
    fun getCusByCusNoAndName(codeOrName : String) : List<Customer>*//*

    @Query("Select * From Customer Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<Customer>

    @Query("Delete from Customer where CustomerID = :id")
    fun deleteById(id: String)*/

}