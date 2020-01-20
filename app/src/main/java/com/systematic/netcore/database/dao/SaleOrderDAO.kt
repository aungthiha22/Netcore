package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.SaleOrder

@Dao
interface SaleOrderDAO {

    @Insert
    fun insert (saleOrder: SaleOrder)

    @Query("Select * From SaleOrder")
    fun getSaleOrder() : List<SaleOrder>

    @Query("Select * From SaleOrder Where SaleOrderID =:id")
    fun getSaleOrderById(id : String) : List<SaleOrder>

    @Query("Select * From SaleOrder Where CustomerID =:customerId")
    fun getSaleOrderByCustomerID(customerId : String) : List<SaleOrder>

    @Query("Select * From SaleOrder Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<SaleOrder>

    @Query("Delete from SaleOrder where SaleOrderID = :id")
    fun deleteById(id: String)

}