package com.creativeinstitute.letschat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.creativeinstitute.letschat.data.models.TextMessages
import com.creativeinstitute.letschat.databinding.FragmentChatBinding
import com.creativeinstitute.letschat.nodes.DBNodes
import com.creativeinstitute.letschat.utils.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    lateinit var chatDB:DatabaseReference
    lateinit var userIDSelf :String
    lateinit var userIDRemote:String

    val chatList = mutableListOf<TextMessages>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentChatBinding.inflate(inflater, container, false)

        var layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.chatRecyclerView.layoutManager = layoutManager

        requireArguments().getString(USERID)?.let {
            userIDRemote = it
        }
        chatDB = FirebaseDatabase.getInstance().reference

        FirebaseAuth.getInstance().currentUser?.let {
            userIDSelf  = it.uid
        }
        chatDB.child(DBNodes.USER).child(userIDRemote).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    snapshot.getValue(User::class.java)?.let {
                        binding.usernameId.text = it.fullName
                        Glide.with(requireContext()).load(it.profileImage).placeholder(R.drawable.ic_profile)
                            .into(binding.profileImage)
                    }


                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )

        messageToShow()

        return binding.root
    }

    private fun messageToShow() {
        chatDB.child(DBNodes.CHAT).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                chatList.clear()
                snapshot.children.forEach {snp->
                    snp.getValue(TextMessages::class.java)?.let {

                        if (it.senderId==userIDSelf && it.receiverId==userIDRemote
                            || it.senderId == userIDRemote && it.receiverId==userIDSelf
                        ){

                            chatList.add(it)
                        }


                    }

                }
                val adapter = ChatAdapter(userIDSelf,chatList)
                binding.chatRecyclerView.adapter = adapter


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })





    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sendMessageButton.setOnClickListener {

            var textMessage = TextMessages(text = binding.chatBox.text.toString(), senderId = userIDSelf, receiverId = userIDRemote, messageId = "")

            sendTextMessage(textMessage)
        }

    }

    private fun sendTextMessage(textMessage: TextMessages) {
        var msgID = chatDB.push().key ?: UUID.randomUUID().toString()
        textMessage.messageId = msgID
        chatDB.child(DBNodes.CHAT).child(msgID).setValue(textMessage).addOnCompleteListener {

            if (it.isSuccessful){
                Toast.makeText(requireContext(),"Message Sent", Toast.LENGTH_SHORT).show()
                binding.chatBox.setText("")
            }else{
                Toast.makeText(requireContext(), it.exception?.message?: "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    companion object{
        const val USERID = "user_id_key"
    }


}