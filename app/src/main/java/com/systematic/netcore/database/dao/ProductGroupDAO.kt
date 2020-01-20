package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.Customer
import com.systematic.netcore.database.model.Product
import com.systematic.netcore.database.model.ProductGroup

@Dao
interface ProductGroupDAO {

    @Insert
    fun insert (productGroup: ProductGroup)

    @Query("Select * From ProductGroup")
    fun getProductGroup() : List<ProductGroup>

    @Query("Select * From ProductGroup Where ProductGroupID =:id")
    fun getProductGroupById(id : String) : List<ProductGroup>

    @Query("Select * From ProductGroup Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<ProductGroup>

    @Query("Delete from ProductGroup where ProductGroupID = :id")
    fun deleteById(id: String)
}