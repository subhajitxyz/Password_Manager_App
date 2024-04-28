package com.example.passwordmanagerapp.roomdatabase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.passwordmanagerapp.models.AccountModel

@Dao
interface AccountDao {
    @Insert
    suspend fun insertAccountDetails(model: AccountModel)
    @Update
    suspend fun updateAccountDetails(model:AccountModel)
    @Delete
    suspend fun deleteAccountDetails(model: AccountModel)
    @Query("SELECT * FROM acconttable")
    fun getAllAccountDetails():LiveData<List<AccountModel>>
}