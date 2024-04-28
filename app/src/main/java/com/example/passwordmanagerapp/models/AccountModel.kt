package com.example.passwordmanagerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "acconttable")
data class AccountModel(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    var accountName:String,
    var userName: String,
    var password:String
)
