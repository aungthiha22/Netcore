package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.Customer
import com.systematic.netcore.database.model.Product

@Dao
interface ProductDAO {

    @Insert
    fun insert (product: Product)

    @Query("Select * From Product")
    fun getProduct() : List<Product>

    @Query("Select * From Product Where ProductID =:id")
    fun getProductById(id : String) : List<Product>

    @Query("Select * From Product Where ProductGroupID =:groupId")
    fun getProductByGroupId(groupId : String) : List<Product>

    @Query("Select * From Product Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<Product>

    @Query("Delete from Product where ProductID = :id")
    fun deleteById(id: String)
}