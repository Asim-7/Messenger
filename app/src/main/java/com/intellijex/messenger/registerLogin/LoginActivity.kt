package com.intellijex.messenger.registerLogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.intellijex.messenger.R
import com.intellijex.messenger.messages.LatestMessagesActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_Login.setOnClickListener {
            val logEmail = login_Email.text.toString()
            val logPass = login_Password.text.toString()

            //Exception handling
            //if (email.isEmpty() || password.isEmpty()) return@setOnClickListener
            if (logEmail.isEmpty() || logPass.isEmpty()){
                Toast.makeText(this, "Fill all fields",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this,"Email: "+logEmail+"\nPass: "+logPass, Toast.LENGTH_SHORT).show()
            //UX Formalities
            login_Email.text.clear()
            login_Password.text.clear()
            login_Email.requestFocus()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(logEmail,logPass)
                .addOnCompleteListener {
                    //user not logged in
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    //to clear all previous activity stacks, use this line:
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {  }
        }

        back_text.setOnClickListener {
            this.finish()
        }
    }
}
