package com.codes.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.codes.R
import com.codes.models.ChatMessage
import com.codes.registerlogin.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_from_row.view.twMessage
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        val TAG = "chatlog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerChatLog.adapter = adapter
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username


        listenForMessages()
        btnSend.setOnClickListener {
            Log.d(TAG, "sending message")
            performSendMessage()
        }
    }


    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue((ChatMessage::class.java))
                if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val FromUser = HomeActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, FromUser!!))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }

                }
                recyclerChatLog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {


            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {


            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {
        val text = etChatLog.text.toString()
        if(text!="")
        {
            val fromId = FirebaseAuth.getInstance().uid
            val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
            val toId = user.uid

            if (fromId == null) return

            // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
            val reference =
                FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
            val toReference =
                FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
            val chatMessage =
                ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "saved chat message")
                    etChatLog.text.clear()
                    recyclerChatLog.scrollToPosition(adapter.itemCount - 1)
                }
            toReference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "saved chat message")
                }


            val recentMessageRef =
                FirebaseDatabase.getInstance().getReference("/recent-messages/$fromId/$toId")
            recentMessageRef.setValue(chatMessage)
            val recentMessageToRef =
                FirebaseDatabase.getInstance().getReference("/recent-messages/$toId/$fromId")
            recentMessageToRef.setValue(chatMessage)
        }

    }
}

class ChatFromItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.twMessage.text = text
        val targetImageView = viewHolder.itemView.circularImgViewFrom
        Picasso.get().load(user.profileUrl).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.twMessage.text = text
        val uri = user.profileUrl
        val targetImageView = viewHolder.itemView.circularImgViewTo
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}