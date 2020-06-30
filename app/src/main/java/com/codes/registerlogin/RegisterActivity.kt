package com.codes.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codes.messages.HomeActivity
import com.codes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var etUsername: EditText
    lateinit var etEmail: EditText
    lateinit var etPassword: EditText
    lateinit var btnRegister: Button
    lateinit var twSignIn: TextView
    lateinit var btnImgUser: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etUsername = findViewById(R.id.etName) as EditText
        etEmail = findViewById(R.id.etEmail) as EditText
        etPassword = findViewById(R.id.etPassword) as EditText
        btnRegister = findViewById(R.id.btnRegister) as Button
        twSignIn = findViewById(R.id.twSignin) as TextView
        btnImgUser = findViewById(R.id.btnImgUser) as Button

        btnRegister.setOnClickListener {
            registeruser()
        }
        twSignIn.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }
        btnImgUser.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            // val bitmapDrawable = BitmapDrawable(bitmap)
            //  btnImgUser.setBackgroundDrawable(bitmapDrawable)
            circularImgView.setImageBitmap(bitmap)
            btnImgUser.alpha = 0f

        }

    }

    private fun registeruser() {
        val Email = etEmail.text.toString()
        val Username = etUsername.text.toString()
        val Password = etPassword.text.toString()
        if (Email.isEmpty() || Password.isEmpty() || Username.isEmpty()) {
            Toast.makeText(
                this@MainActivity,
                "Please Fill the fields !",
                Toast.LENGTH_SHORT
            ).show()
        } else {

            // Firebase   authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    uploadImageFirebase()
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    Toast.makeText(
                        this@MainActivity,
                        "Account created succesfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this@MainActivity,
                        "${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        }
    }

    private fun uploadImageFirebase() {
        if (selectedPhotoUri != null)
        {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("register", "success")
                    ref.downloadUrl.addOnSuccessListener {
                        // it.toString()

                        saveUserToFireBaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    //logs
                }
        }
        else{
            saveUserToFireBaseDatabase("default")
        }
    }

    private fun saveUserToFireBaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(
            uid,
            etUsername.text.toString(),
            profileImageUrl
        )
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("register", "saved user to database")
            }
    }
}
@Parcelize
class User(val uid: String, val username: String, val profileUrl: String):Parcelable{
    constructor():this("","","")
}