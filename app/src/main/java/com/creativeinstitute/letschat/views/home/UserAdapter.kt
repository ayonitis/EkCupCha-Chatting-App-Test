package com.creativeinstitute.letschat.views.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import coil3.load
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.creativeinstitute.letschat.R
import com.creativeinstitute.letschat.databinding.ItemUserBinding
import com.creativeinstitute.letschat.utils.User
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private val userListener: UserListener) : ListAdapter<User, UserViewHolder>(COMPARATOR) {

    interface UserListener {
        fun userItemClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        getItem(position)?.let { user ->
            holder.binding.apply {
                fullName.text = user.fullName
                email.text = user.email
                userBio.text = user.bio

                Glide.with(profileImage.context)
                    .load(user.profileImage)
                    .apply(RequestOptions.bitmapTransform(CircleCrop())) //CircleCrop for circular image
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(profileImage)

                holder.itemView.setOnClickListener { _ ->
                    userListener.userItemClick(user)
                }
            }
        }
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }
        }
    }
}
