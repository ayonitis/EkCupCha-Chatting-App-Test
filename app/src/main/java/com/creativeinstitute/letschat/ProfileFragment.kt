package com.creativeinstitute.letschat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.creativeinstitute.letschat.databinding.FragmentProfileBinding
import com.creativeinstitute.letschat.nodes.DBNodes
import com.creativeinstitute.letschat.utils.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var userDB: DatabaseReference
    private var userID = ""
    val bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        userDB = FirebaseDatabase.getInstance().reference
        requireArguments().getString(USERID)?.let {
            userID = it
            getUserByID(it)
        }

        FirebaseAuth.getInstance().currentUser?.let {
            if (userID == it.uid) {
                binding.chatOrEditBtn.text = "Edit Profile"
            } else {
                binding.chatOrEditBtn.text = "Let's Chat"
            }

            binding.chatOrEditBtn.setOnClickListener {
                bundle.putString(EditProfileFragment.USERID, userID)
                if (binding.chatOrEditBtn.text == "Edit Profile") {
                    findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment, bundle)
                }else{
                    findNavController().navigate(R.id.action_profileFragment_to_chatFragment, bundle)
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Re-fetch user data each time the ProfileFragment is visible
        getUserByID(userID)
    }

    companion object {
        const val USERID = "user_it_key"
    }

    private fun getUserByID(userID: String) {
        userDB.child(DBNodes.USER).child(userID).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let { user ->
                        binding.userEmail.text = user.email
                        binding.userBio.text = user.bio
                        binding.fullName.text = user.fullName

                        // Load profile image if it exists
                        if (!user.profileImage.isNullOrEmpty()) {
                            Glide.with(this@ProfileFragment)
                                .load(user.profileImage)
                                .into(binding.profileImage)
                        } else {
                            // Optionally set a placeholder or default image if profileImage is empty
                            binding.profileImage.setImageResource(R.drawable.ic_profile)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}
