package com.intellijex.messenger.views

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.intellijex.messenger.R
import com.intellijex.messenger.models.ChatMessage
import com.intellijex.messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){

    var chatPartnerUser: User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_textView_latestMessage.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }else{
            chatPartnerId = chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_textView_latestMessage.text = chatPartnerUser?.username

                val targetImageView = viewHolder.itemView.imageView_latestMessage
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }
        })
    }
}
