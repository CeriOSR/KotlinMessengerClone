package com.example.reycerio.kotlinmessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.reycerio.kotlinmessenger.R
import com.example.reycerio.kotlinmessenger.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_newmessage.view.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        //layout manager has been set up inside the xml instead.

        fetchUsers()
    }

    //like making a static constant of this activity class
    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    //plugging the data into the user class we created
                    val user = it.getValue(User::class.java)
                    //using Groupie 3rd party lib to build an adapter instead
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                    recyclerview_newmessage.adapter = adapter
                }

                adapter.setOnItemClickListener { item, view ->

                    //cast item into a UserItem
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    //user companion object as key
//                    intent.putExtra(USER_KEY, userItem.user.username)
                    intent.putExtra(USER_KEY, userItem.user) //needs a specific class...lets make this Parcelable
                    startActivity(intent)

                    finish() //finish off the new message activity so we go back to latestMessageActivity on the stack.
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

//classes take parameters?!?!?!?!?! WTF kotlin is awesome!
class UserItem(val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.user_row_newmessage_text_view.text = user.username
        //using picasso to load images
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.user_row_newmessage_image_view)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_newmessage
    }
}
