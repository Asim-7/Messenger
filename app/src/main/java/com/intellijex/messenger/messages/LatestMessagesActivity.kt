package com.intellijex.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.intellijex.messenger.R
import com.intellijex.messenger.models.ChatMessage
import com.intellijex.messenger.models.User
import com.intellijex.messenger.registerLogin.RegisterActivity
import com.intellijex.messenger.views.LatestMessageRow
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    //declaring global variable
    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerView_latest_messages.adapter = adapter
        recyclerView_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        //Set item click listner on adapter
        adapter.setOnItemClickListener { item, view ->
            Log.i("msg","123456")
            val intent = Intent(this, ChatLogActivity::class.java)

            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

        //setupDummyRows()
        listenForLatestMessages()
        //Check who is the user
        fetchCurrentUser()
        //To Check whether user already signed or not
        verifyUserLoggedIn()
    }

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refereshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        //listen for new node
        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) { }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refereshRecyclerViewMessages()
            }
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refereshRecyclerViewMessages()
            }
        })
    }

    val adapter = GroupAdapter<ViewHolder>()

/*    private fun setupDummyRows() {
        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(LatestMessageRow())
        adapter.add(LatestMessageRow())
        adapter.add(LatestMessageRow())
        adapter.add(LatestMessageRow())

        recyclerView_latest_messages.adapter = adapter
    }*/

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.i("msgCurrent","Current User: ${currentUser?.profileImageUrl}")
            }
        })
    }

    private fun verifyUserLoggedIn() {
            val uid = FirebaseAuth.getInstance().uid
            if (uid==null){
                //user not logged in
                val intent = Intent(this, RegisterActivity::class.java)
                //to clear all previous activity stacks, use this line:
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //switch structure in Kotlin
        when(item?.itemId){
            R.id.newMsg -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_signOut -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                //to clear all previous activity stacks, use this line:
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }
}
