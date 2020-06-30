package com.codes.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.DividerItemDecoration
import com.codes.R
import com.codes.messages.NewMessageActivity.Companion.USER_KEY
import com.codes.models.ChatMessage
import com.codes.registerlogin.LoginActivity
import com.codes.registerlogin.User
import com.codes.views.RecentMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.recent_message_row.view.*


class HomeActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
    }

    lateinit var tv_no_data: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tv_no_data = findViewById(R.id.tv_no_data)
        recyclerview_recentmsg.adapter = adapter
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as RecentMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.ChatToUser)
            startActivity(intent)
        }

        fetchcurrentUser()

        listenForRecentMessages()

    }


    val adapter = GroupAdapter<GroupieViewHolder>()


    val recentMessagesmap = HashMap<String, ChatMessage>()
    private fun refreshRecyclerviewMessages() {

        adapter.clear()
        recentMessagesmap.values.forEach {
            adapter.add(RecentMessageRow(it))
           /* val v =adapter.getItemCount().toString()
            Log.d("Bossey",v)
            if(adapter != null && layoutInflater!= null) {
                if (adapter.getItemCount() == 0) {
                    tv_no_data.visibility = View.VISIBLE
                }
                else{
                    tv_no_data.visibility = View.INVISIBLE
                }
            }

            */

        }
    }

    private fun listenForRecentMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/recent-messages/$fromId")


        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                recentMessagesmap[snapshot.key!!] = chatMessage
                recyclerview_recentmsg.addItemDecoration(
                    DividerItemDecoration(
                        recyclerview_recentmsg.getContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
                refreshRecyclerviewMessages()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                recentMessagesmap[snapshot.key!!] = chatMessage

                refreshRecyclerviewMessages()




            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }


        })
    }

    private fun fetchcurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this@HomeActivity, NewMessageActivity::class.java)
                startActivity(intent)

            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
