package com.example.passwordmanagerapp

import java.security.SecureRandom

class PasswordStrengthService {

    //calculate password strength of user input password
    // return strength type as per input
    fun calculatePasswordStrength(password: String): String {
        var score = 0
        if (password.length >= 6) {
            score += 1
        }
        if (password.any { it.isUpperCase() }) {
            score += 1
        }
        if (password.any { it.isLowerCase() }) {
            score += 1
        }
        if (password.any { char -> char in "!@#$%^&*()_+-=[]{}|;':<>,.?/" }) {
            score += 1
        }
        // Translate score to strength level (Example)
        var strength: String = ""
        if(score<=2 && score>=0){
            strength="Weak"
        }else if( score<=3) {
            strength= "Medium"
        }else{
            strength="Strong"
        }

        return strength

    }

    //generate a strong password and return as String
    fun generatePassword():String{
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;':<>,.?/"
        val random = SecureRandom()
        val password = StringBuilder()
        val length = 12 // Adjust as needed
        val charLength = chars.length

        for (i in 0 until length) {
            val randomIndex = random.nextInt(charLength)
            password.append(chars[randomIndex])
        }

        return password.toString()
    }
}