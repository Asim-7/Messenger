package com.intellijex.messenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.intellijex.messenger.R
import com.intellijex.messenger.models.ChatMessage
import com.intellijex.messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {

    val adapater = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView_chatLog.adapter = adapater

        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

        //setupDummyData()
        listenForMessages()

        //send messages
        sendBtn_chatLog.setOnClickListener {
            Log.i("msg","btn clickedddddddd")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        //val reference = FirebaseDatabase.getInstance().getReference("/messages")
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage!=null){
                    Log.i("msg",chatMessage.text)

                    //From Messages from the user that's Logged In
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser
                        adapater.add(ChatFromItem(chatMessage.text , currentUser!!))
                    }else{
                        //val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                        adapater.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                recyclerView_chatLog.scrollToPosition(adapater.itemCount -1)
            }
        })
    }

    private fun performSendMessage() {
        //send msg to Firebase
        val text = editText_chatLog.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId==null) return

        //val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.i("msg","Saved our chat message: ${reference.key}")
                editText_chatLog.text.clear()
                recyclerView_chatLog.scrollToPosition(adapater.itemCount - 1)
            }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }

/*    private fun setupDummyData() {
        val adapater = GroupAdapter<ViewHolder>()
        adapater.add(ChatFromItem("From Message"))
        adapater.add(ChatToItem("To message \nTo Message"))
        adapater.add(ChatFromItem("From Message"))
        adapater.add(ChatToItem("To message \nTo Message"))
        adapater.add(ChatFromItem("From Message"))
        adapater.add(ChatToItem("To message \nTo Message"))

        recyclerView_chatLog.adapter = adapater
    }*/
}

class ChatFromItem(val text: String , val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text
        //load user Image
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_chat_FromRow
        Picasso.get().load(uri).into(targetImageView)
    }

}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_Row.text = text
        //load user Image
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_chatTo_row
        Picasso.get().load(uri).into(targetImageView)
    }

}