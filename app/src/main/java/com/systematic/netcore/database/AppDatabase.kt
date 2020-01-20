package com.systematic.netcore.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.systematic.netcore.database.dao.*
import com.systematic.netcore.database.model.*

@Database(entities = [(Customer::class), (CustomerGroup::class), (Product::class), (ProductGroup::class), (TowerType::class),
    (SaleOrder::class), (Installation::class), (InstallationInfo::class), (User::class), (City::class), (Township::class),
    (ScoutType::class), (ImageScout::class), (Scout::class),(BillingInfo::class)], version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getCustomerDAO(): CustomerDAO
    abstract fun getCustomerGroupDAO(): CustomerGroupDAO
    abstract fun getProductDAO(): ProductDAO
    abstract fun getProductGroupDAO(): ProductGroupDAO
    abstract fun getTowerTypeDAO(): TowerTypeDAO
    abstract fun getUserDAO(): UserDAO
    abstract fun getCityDAO(): CityDAO
    abstract fun getTownshipDAO(): TownshipDAO
    abstract fun getScoutTypeDAO(): ScoutTypeDAO
    abstract fun getScoutDAO(): ScoutDAO
    abstract fun getImageScoutDAO(): ImageScoutDAO
    abstract fun getSaleOrderDAO(): SaleOrderDAO
    abstract fun getInstallationDAO(): InstallationDAO
    abstract fun getInstallationInfoDAO(): InstallationInfoDAO
    abstract fun  getBillingInfoDAO() : BillingDAO


    companion object {
        private var sInstance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (sInstance == null) {
                sInstance = Room
                    .databaseBuilder(context.applicationContext, AppDatabase::class.java, "Netcore")
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return sInstance!!
        }
    }
}