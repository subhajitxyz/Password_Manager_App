package com.example.passwordmanagerapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerapp.adapters.AccountAdapter
import com.example.passwordmanagerapp.models.AccountModel
import com.example.passwordmanagerapp.roomdatabase.AccountDatabase
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var recView:RecyclerView
    private lateinit var adapter:AccountAdapter

    private lateinit var addButton: FloatingActionButton
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private lateinit var database:AccountDatabase
    private lateinit var accountList:ArrayList<AccountModel>

    private lateinit var accountName_input:EditText
    private lateinit var username_input:EditText
    private lateinit var password_input:EditText
    private lateinit var password_strength:TextView
    private lateinit var suggest_btn:TextView

    private lateinit var enHelper :EncryptionHelper
    private lateinit var passwordStrengthService:PasswordStrengthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recView= findViewById(R.id.main_rec_view)
        addButton=findViewById(R.id.add_button)

        database= AccountDatabase.getDatabase(this)
        accountList= ArrayList()
        //Retrieve the data from room database and store into Arraylist
        getData()

        //set up recyclerview
        recView.layoutManager= LinearLayoutManager(this)
        adapter= AccountAdapter(accountList)
        recView.adapter= adapter

        //EncrytionHelper class object and PasswordStrengthService class object is created
        enHelper = EncryptionHelper()
        passwordStrengthService= PasswordStrengthService()


        // when Add button is clicked , it shows the bottom sheet dialog for taking user input
        addButton.setOnClickListener{
            showAddAccountDialog()
        }


    }


    private fun getData() {
        database.accountDao().getAllAccountDetails().observe(this, Observer {
            accountList.clear()
            accountList.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun showAddAccountDialog(){

        //initialized a bottom sheet dialog
        bottomSheetDialog= BottomSheetDialog(this@MainActivity,R.style.BottomSheetTheme)
        var sheetView  = LayoutInflater.from(applicationContext).inflate(R.layout.add_account_layout,null)

        username_input =sheetView.findViewById<EditText>(R.id.username_edt_text)
        accountName_input=sheetView.findViewById<EditText>(R.id.account_name_edt_text)
        password_input =sheetView.findViewById<EditText>(R.id.password_edt_text)
        password_strength = sheetView.findViewById<TextView>(R.id.password_strength)
        suggest_btn = sheetView.findViewById<TextView>(R.id.suggest_btn)

        //shows a strong password when password suggest button is clicked
        suggest_btn.setOnClickListener{
            password_input.setText(passwordStrengthService.generatePassword())
            password_strength.setText("Strong")
            password_strength.setTextColor(Color.RED)
        }


        password_input.addTextChangedListener { text ->
            if( text.toString().trim().isNotEmpty()){
                password_strength.setText(passwordStrengthService.calculatePasswordStrength(text.toString().trim()))
                if(password_strength.text=="Strong"){
                   password_strength.setTextColor(Color.RED)
                }else if(password_strength.text=="Weak"){
                    password_strength.setTextColor(Color.GREEN)
                }else{
                    password_strength.setTextColor(Color.BLUE)
                }
            }
        }


        // Add Account Button
        //when button is clicked all details save to room database and handle all edge cases.
        sheetView.findViewById<Button>(R.id.add_account_btn).setOnClickListener{
            val username = username_input.text.toString().trim()
            val accountName = accountName_input.text.toString().trim()
            val password = password_input.text.toString().trim()

            if(username.isNotEmpty() && accountName.isNotEmpty() && password.isNotEmpty()){
                var encryptedData= enHelper.encrypt(password)
                GlobalScope.launch{
                    database.accountDao().insertAccountDetails(AccountModel(0, accountName, username,encryptedData))
                }
                getData()
                Toast.makeText(this, "Your account details are saved", Toast.LENGTH_LONG).show()
                bottomSheetDialog.dismiss()
            } else {
                if(username.isEmpty()){
                    username_input.error = "Username required"
                }
                if(accountName.isEmpty()){
                    accountName_input.error="Account name required"
                }
                if(password.isEmpty()){
                    password_input.error="Password required"
                }
            }

        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }
}