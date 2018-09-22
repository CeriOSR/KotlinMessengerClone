package com.example.reycerio.kotlinmessenger.messages

import android.support.v7.app.AppCompatActivity
import com.example.reycerio.kotlinmessenger.R
import android.os.Bundle
import android.util.Log
import com.example.reycerio.kotlinmessenger.models.ChatMessage
import com.example.reycerio.kotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }
    var toUser: User? = null
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username


        listenForMessages()

        send_button_chat_log.setOnClickListener{
            Log.d(TAG, "Attempt to send message to print on click")
            performSendMessage()
            //clears the text box
            message_edit_text_chat_log.text.clear()
            //scrolls to bottom
            recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
        }

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        reference.addChildEventListener(object : ChildEventListener {


            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chattMessage = p0.getValue(ChatMessage::class.java)
                Log.d(TAG, chattMessage?.text.toString())

                if (chattMessage != null) {

                    //this will display all your messages...need to put in if statements to filter
                    if (chattMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chattMessage.text, currentUser))

                    }else {
                        if (toUser != null) {
                            adapter.add(ChatToItem(chattMessage.text, toUser!!))
                        }
                    }


                }
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }



    private fun performSendMessage() {
        val text = message_edit_text_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = toUser.uid

        //safe unwrapping
        if (fromId == null) return

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "saved our chat message: ${reference.key}")
        }
        toReference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG,"saved message chat to To node: ${reference.key}")
        }
    }
}

class ChatFromItem(val text: String, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text

        val imageView = viewHolder.itemView.imageview_from_row
        val uriString = user.profileImageUrl
        Picasso.get().load(uriString).into(imageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text = text

        val imageView = viewHolder.itemView.imageview_to_row
        val uriString = user.profileImageUrl
        Picasso.get().load(uriString).into(imageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}


