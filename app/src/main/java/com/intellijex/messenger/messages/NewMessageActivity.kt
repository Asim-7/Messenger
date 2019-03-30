package com.intellijex.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.intellijex.messenger.R
import com.intellijex.messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_msg.view.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

       /* //Using Groupie library for handling complex recyclerviews easily
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(UserItem())
        adapter.add(UserItem())
        adapter.add(UserItem())

        recycler_newmsg.adapter = adapter
        //Declared in XML
        //recycler_newmsg.layoutManager = LinearLayoutManager(this)*/

        fetchUsers()
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    Log.i("New msg: ", it.toString())
                    val user = it.getValue(User::class.java)
                    adapter.add(UserItem(user!!))
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    //intent.putExtra(USER_KEY, userItem.user.username)
                    //pass whole user object
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    //close the last activity behind it
                    finish()
                }

                recycler_newmsg.adapter = adapter
            }

        })
    }
}
//Creating resource for new messages
class UserItem(val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.user_row_new_msg
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.userName_newMsg.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_newMsg)
    }

}
