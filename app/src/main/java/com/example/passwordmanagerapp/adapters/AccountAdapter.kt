package com.example.passwordmanagerapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerapp.EncryptionHelper
import com.example.passwordmanagerapp.PasswordStrengthService
import com.example.passwordmanagerapp.R
import com.example.passwordmanagerapp.models.AccountModel
import com.example.passwordmanagerapp.roomdatabase.AccountDatabase
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AccountAdapter(private val accountList: ArrayList<AccountModel>) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {
    private lateinit var bottomSheetDialog: BottomSheetDialog


    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountNameTextView: TextView = itemView.findViewById(R.id.account_name)
        val accountPassTextView: TextView = itemView.findViewById(R.id.account_pass)
        val imageButton: ImageButton = itemView.findViewById(R.id.img_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.all_details_recycler_row, parent, false)
        return AccountViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val currentItem = accountList[position]
        if(currentItem.accountName.length>10){
            holder.accountNameTextView.text = currentItem.accountName.substring(0,10)+".."
        }else{
            holder.accountNameTextView.text = currentItem.accountName
        }
        holder.accountPassTextView.text = "******"
        var database = AccountDatabase.getDatabase(holder.itemView.context)
        var passwordStrengthService:PasswordStrengthService= PasswordStrengthService()

        // when we clicked image button of itemview
        //it shows bottom sheet dialog of accunt details
        holder.imageButton.setOnClickListener {
            showBottomSheetDialog(holder.itemView.context, currentItem,database,passwordStrengthService)

        }
    }
    override fun getItemCount() = accountList.size

    private fun showBottomSheetDialog(context: Context, currentItem: AccountModel,
                                      database:AccountDatabase, passwordStrengthService:PasswordStrengthService) {
        bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetTheme)
        var sheetView = LayoutInflater.from(context)
            .inflate(R.layout.account_details_layout, null)

        var edit_btn = sheetView.findViewById<Button>(R.id.edit_btn)
        var delete_btn = sheetView.findViewById<Button>(R.id.delete_btn)
        var username_text = sheetView.findViewById<TextView>(R.id.username_text_view)
        var accountName_text = sheetView.findViewById<TextView>(R.id.account_type_text_view)
        var password_text = sheetView.findViewById<TextView>(R.id.password_txt_view)
        var toggleBtn = sheetView.findViewById<ImageButton>(R.id.pass_toggle_img_btn)


        username_text.setText(currentItem.userName)
        accountName_text.setText(currentItem.accountName)
        toggleBtn.setBackgroundResource(R.drawable.eye_crossed_icon)

        var flag: Boolean = false

        toggleBtn.setOnClickListener {
            flag = !flag
            if (flag == true) {
                var x_pass = EncryptionHelper().decrypt(currentItem.password)
                toggleBtn.setBackgroundResource(R.drawable.eye_icon)
                password_text.setText(x_pass)
            } else {
                toggleBtn.setBackgroundResource(R.drawable.eye_crossed_icon)
                password_text.setText("******")
            }
        }

        delete_btn.setOnClickListener {
            Toast.makeText(context,"delete",Toast.LENGTH_LONG).show()
            GlobalScope.launch {
                database.accountDao().deleteAccountDetails(AccountModel(currentItem.id,"","",""))
            }
            bottomSheetDialog.dismiss()

        }

        // when we clicked edit button
        //it shows bottom sheet dialog for editing details of account
        edit_btn.setOnClickListener {
            bottomSheetDialog.dismiss()
            showBottomSheetDialogEdit(context, currentItem,database,passwordStrengthService)

        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()

    }

    private fun showBottomSheetDialogEdit(context: Context, currentItem: AccountModel, database: AccountDatabase, passwordStrengthService: PasswordStrengthService) {

        var bottomSheetDialogEdit =
            BottomSheetDialog(context, R.style.BottomSheetTheme)
        var sheetViewEdit = LayoutInflater.from(context)
            .inflate(R.layout.add_account_layout, null)

        var final_edit_btn = sheetViewEdit.findViewById<Button>(R.id.edit_account_btn)
        var final_add_btn = sheetViewEdit.findViewById<Button>(R.id.add_account_btn)
        final_add_btn.isVisible = false
        final_edit_btn.isVisible= true


        var username_input = sheetViewEdit.findViewById<EditText>(R.id.username_edt_text)
        var accountName_input =
            sheetViewEdit.findViewById<EditText>(R.id.account_name_edt_text)
        var password_input = sheetViewEdit.findViewById<EditText>(R.id.password_edt_text)
        var suggest_btn = sheetViewEdit.findViewById<TextView>(R.id.suggest_btn)
        var password_strength = sheetViewEdit.findViewById<TextView>(R.id.password_strength)


        username_input.setText(currentItem.userName)
        accountName_input.setText(currentItem.accountName)
        password_input.setText(EncryptionHelper().decrypt(currentItem.password))

        suggest_btn.setOnClickListener{
            password_input.setText(passwordStrengthService.generatePassword())
            password_strength.setText("Strong")
            password_strength.setTextColor(Color.RED)
        }

        password_input.addTextChangedListener { text ->
            // Do something with the new text
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

        //Finally edit
        // it updates the existing details in room database and handle all edge cases
        final_edit_btn.setOnClickListener {
            val username = username_input.text.toString().trim()
            val accountName = accountName_input.text.toString().trim()
            val password = password_input.text.toString().trim()

            if (username.isNotEmpty() && accountName.isNotEmpty() && password.isNotEmpty()) {

                GlobalScope.launch {
                    database.accountDao().updateAccountDetails(
                        AccountModel(
                            currentItem.id,
                            accountName,
                            username,
                            EncryptionHelper().encrypt(password)
                        )
                    )

                }

                Toast.makeText(context, "Existing Details are updated", Toast.LENGTH_LONG)
                    .show()
                bottomSheetDialogEdit.dismiss()
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

        bottomSheetDialogEdit.setContentView(sheetViewEdit)
        bottomSheetDialogEdit.show()

    }


}