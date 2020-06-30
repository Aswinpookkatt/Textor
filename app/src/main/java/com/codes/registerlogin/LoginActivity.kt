package com.codes.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.codes.messages.HomeActivity
import com.codes.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    lateinit var etEmail: EditText
    lateinit var etPassword: EditText
    lateinit var btnLogin: Button
    lateinit var twSignUp :TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        isloggedin()

        etEmail = findViewById(R.id.etEmail) as EditText
        etPassword = findViewById(R.id.etPassword) as EditText
        btnLogin = findViewById(R.id.btnLogin) as Button
        twSignUp = findViewById(R.id.twSignUp) as TextView
        btnLogin.setOnClickListener {
            login()
        }
        twSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)

        }
    }
    private fun login(){

            val Email = etEmail.text.toString()
            val Password = etPassword.text.toString()
            if (Email.isEmpty() || Password.isEmpty())
            {
                Toast.makeText(
                    this@LoginActivity,
                    "Please Fill the fields !",
                    Toast.LENGTH_SHORT
                ).show()
            }else{

                // Firebase   authentication
                FirebaseAuth.getInstance().signInWithEmailAndPassword(Email,Password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) return@addOnCompleteListener
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()

                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this@LoginActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            }

    }

    private fun isloggedin(){
        val uid =FirebaseAuth.getInstance().uid
        if(uid!=null){
            val intent =Intent(this, HomeActivity::class.java)
          //  intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
