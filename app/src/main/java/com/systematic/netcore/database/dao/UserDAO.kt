package com.systematic.netcore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systematic.netcore.database.model.User

@Dao
interface UserDAO {

    @Insert
    fun insert (user: User)

    @Query("Select * From User Where Active=1")
    fun getUser() : List<User>

    @Query("Select * From User Where UserID =:id")
    fun getUserById(id : String) : List<User>

    @Query("Select * From User Order By ModifiedOn DESC")
    fun getLastModifiedOn() : List<User>

    @Query("Select * From User Where UserCode=:userCode And Password=:password")
    fun getUserLogin(userCode : String, password : String) : List<User>

    @Query("Delete from User where UserID = :id")
    fun deleteById(id: String)
}