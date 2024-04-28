package com.example.passwordmanagerapp.roomdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.passwordmanagerapp.models.AccountModel

@Database(entities = [AccountModel::class], version = 1)
abstract class AccountDatabase: RoomDatabase() {
    abstract fun accountDao():AccountDao

    companion object{
        @Volatile
        private var INSTANCE: AccountDatabase? = null
        fun getDatabase(context: Context): AccountDatabase{
            if(INSTANCE== null){
                synchronized(this){
                    INSTANCE= Room.databaseBuilder(context.applicationContext,
                        AccountDatabase::class.java,"accountDB").build()
                }
            }
            return INSTANCE!!
        }
    }
}