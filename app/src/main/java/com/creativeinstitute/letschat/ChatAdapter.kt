package com.creativeinstitute.letschat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.creativeinstitute.letschat.data.models.TextMessages
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private var userIDSelf: String, private val chatList: MutableList<TextMessages>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val LEFT: Int = 1
    private val RIGHT: Int = 2

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTV: TextView = itemView.findViewById(R.id.chatTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == RIGHT) {
            R.layout.item_right_chat
        } else {
            R.layout.item_left_chat
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = chatList[position]
        holder.messageTV.text = message.text
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].senderId == userIDSelf) {
            RIGHT
        } else {
            LEFT
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    /**
     * Updates the chat list and refreshes the RecyclerView.
     */
    fun updateChatList(newMessages: List<TextMessages>) {
        chatList.clear()
        chatList.addAll(newMessages)
        notifyDataSetChanged()
    }
}
