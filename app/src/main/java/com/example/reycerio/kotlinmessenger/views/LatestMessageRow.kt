package com.example.reycerio.kotlinmessenger.views

import com.example.reycerio.kotlinmessenger.R
import com.example.reycerio.kotlinmessenger.models.ChatMessage
import com.example.reycerio.kotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage) : Item<ViewHolder>() {

    var chatPartnerUser: User? = null
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_message_latestmessages.text = chatMessage.text

        var chatPartnerId: String

        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        }else {
            chatPartnerId = chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java) ?: return
                viewHolder.itemView.textview_username_latestmessages.text = chatPartnerUser?.username

                //load to user image
                val imageView = viewHolder.itemView.imageview_latestmessage
                val uriString = chatPartnerUser?.profileImageUrl
                Picasso.get().load(uriString).into(imageView)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })





    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    private fun fetchRecipientUser( chatPartnerId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}