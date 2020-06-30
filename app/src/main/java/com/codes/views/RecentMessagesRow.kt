package com.codes.views

import com.codes.R
import com.codes.models.ChatMessage
import com.codes.registerlogin.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.recent_message_row.view.*

class RecentMessageRow(val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
    var ChatToUser : User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tw_message_recent_row.text = chatMessage.text
        val chatToUserId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatToUserId = chatMessage.toId
        }
        else {
            chatToUserId = chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatToUserId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ChatToUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.tw_Username_recent_row.text =ChatToUser?.username
                val targetImageView = viewHolder.itemView.circularImgView_recent_row
                Picasso.get().load(ChatToUser?.profileUrl).into(targetImageView )
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    override fun getLayout(): Int {
        return R.layout.recent_message_row
    }
}