package com.intellijex.messenger.registerLogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.intellijex.messenger.R
import com.intellijex.messenger.messages.LatestMessagesActivity
import com.intellijex.messenger.models.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_register.setOnClickListener {
            performRegister()
        }

        account_text.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btn_pic.setOnClickListener {
            //Select pic from phone
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var user: String=""
    var email: String=""
    var password: String=""
    var selectedPhotoUri: Uri? = null

    //to check which image is selected
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //if (requestCode==0 && requestCode == Activity.RESULT_OK && data!= null)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0 && data != null) {
                //check what selected image was
                Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()

                selectedPhotoUri = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
                //setting image on circle image
                circle_imgReg.setImageBitmap(bitmap)
                btn_pic.alpha = 0f

                //setting image on button
                /*val bitmapDrawable = BitmapDrawable(bitmap)
                btn_pic.setBackgroundDrawable(bitmapDrawable)*/
            }
        }
    }

    private fun performRegister() {
        user = userNameReg.text.toString()
        email = emailReg.text.toString()
        password = passReg.text.toString()

        //Exception handling
        //if (email.isEmpty() || password.isEmpty()) return@setOnClickListener
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Fill all fields",Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "User is: "+user+"\nEmail is: "+email+"\nPassword is: "+password,
            Toast.LENGTH_SHORT).show()
        //UX Formalities
        userNameReg.text.clear()
        emailReg.text.clear()
        passReg.text.clear()
        userNameReg.requestFocus()

        //Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if successful
                Toast.makeText(this,"Success, uid: "+ it.result!!.user.uid, Toast.LENGTH_SHORT).show()

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Failure, uid: ${it.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Toast.makeText(this,"Registered Successfully: ${it.metadata?.path}!!", Toast.LENGTH_SHORT).show()

                //To access file location on Firebase
                ref.downloadUrl.addOnSuccessListener {
                    Toast.makeText(this,"Registered Successfully: ${it}!!", Toast.LENGTH_SHORT).show()
                    //saving data to firebase database
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{

            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, user, profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this,"Saved Successfully!!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LatestMessagesActivity::class.java)
                //to clear all previous activity stacks, use this line:
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {

            }
    }
}
