package com.example.reycerio.kotlinmessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.reycerio.kotlinmessenger.R
import com.example.reycerio.kotlinmessenger.models.ChatMessage
import com.example.reycerio.kotlinmessenger.models.User
import com.example.reycerio.kotlinmessenger.registration.MainActivity
import com.example.reycerio.kotlinmessenger.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        val TAG = "LatestMessages"
    }

    //moved adapter here so its class wide
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        verifyIfUserIsLoggedIn()
        fetchCurrentUser()
        //moved this in here so it can access the adapter
        recyclerview_latest_messages.adapter = adapter

        //get a divider to show up
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        listenForLatestMessages()
        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG, "1234556")
            val intent = Intent(this, ChatLogActivity::class.java)

            //accessing the chatpartner user variable in the cell
            //cast the cell to the class (LatestMessageRow in this case)
            val row = item as LatestMessageRow


            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser )
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {   //creating the menu
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {  //when options selected

        when (item?.itemId) {
            (R.id.menu_new_messages) -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            (R.id.menu_sign_out) -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }



    private fun refreshRecyclerView() {

        this.runOnUiThread {
            adapter.clear()
            latestMessagesHashMap.values.forEach {
                adapter.add(LatestMessageRow(it))

            }
        }
    }


    //using hashmap/dictionary because we dont want to get rid of the uuid
    val latestMessagesHashMap = HashMap<String, ChatMessage>()

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        latestMessageRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
//                val toUser = fetchRecipientUser(chatMessage.toId)
//                Log.d("ChatLog", toUser.username)
                //hash map, like a dictionary in swift
                //another way of unwrapping
                p0.let {
                    latestMessagesHashMap["${it.key}"] = chatMessage
                }

                refreshRecyclerView()

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
//                val toUser = fetchRecipientUser(chatMessage.toId)
//                Log.d("ChatLog", toUser.username)
                //hash map, like a dictionary in swift
                //another way of unwrapping
                p0.let {
                    latestMessagesHashMap["${it.key}"] = chatMessage
                }
                refreshRecyclerView()
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun fetchCurrentUser() {
        var uid = FirebaseAuth.getInstance().uid.toString()
        var ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("Chatlog", "$currentUser?.username")
            }
        })
    }

    private fun verifyIfUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
