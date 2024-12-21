package com.creativeinstitute.letschat

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.creativeinstitute.letschat.databinding.FragmentEditProfileBinding
import com.creativeinstitute.letschat.nodes.DBNodes
import com.creativeinstitute.letschat.utils.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditProfileFragment : Fragment() {

    lateinit var binding: FragmentEditProfileBinding
    lateinit var userDB: DatabaseReference
    private val currentUser = FirebaseAuth.getInstance().currentUser
    lateinit var userStorage: StorageReference
    private var isProfileClicked = false
    private lateinit var userProfileUri: Uri
    private var imageLink: String = "no_data"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        userDB = FirebaseDatabase.getInstance().reference
        userStorage = FirebaseStorage.getInstance().reference

        // Display current email/gmail-address
        currentUser?.let {
            binding.userEmail.text = it.email
        }

        // Fetch and display current user data (user & bio)
        loadUserData()

        // Save updated user data
        binding.saveProfileBtn.setOnClickListener {
            if (isProfileClicked && userProfileUri != null) {
                uploadImage(userProfileUri)
            } else {
                saveUserData()
            }
        }

        binding.profileImage.setOnClickListener {
            pickProfileImage()
            isProfileClicked = true
        }

        return binding.root
    }

    private fun uploadImage(userProfileUri: Uri) {
        // Reference to upload profile image under "Profile-Images/upload/userId"
        val profileStorage: StorageReference = userStorage.child("Profile-Images").child("upload").child(currentUser!!.uid)

        profileStorage.putFile(userProfileUri).addOnCompleteListener { uploadTask ->
            if (uploadTask.isSuccessful) {
                profileStorage.downloadUrl.addOnSuccessListener { uri ->
                    imageLink = uri.toString()
                    // After getting the download URL, update the database
                    saveUserData()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickProfileImage() {
        ImagePicker.with(this)
            .crop() // Crop image (Optional), check customization for more option
            .compress(512) // Final image size will be less than 1 MB (Optional)
            .maxResultSize(512, 512) // Final image resolution will be less than 1080 x 1080 (Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    companion object {
        const val USERID = "user_it_key"
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    // Image Uri will not be null for RESULT_OK
                    data?.data?.let {
                        userProfileUri = it
                        binding.profileImage.setImageURI(it)
                    }
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun loadUserData() {
        currentUser?.let {
            val userId = it.uid
            userDB.child(DBNodes.USER).child(userId).get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding.fullName.setText(it.fullName)
                    binding.userBio.setText(it.bio)
                    // If profile image exists, load it
                    it.profileImage?.let { imageUrl ->
                        if (imageUrl.isNotEmpty()) {
                            // Load the image using your preferred image loading library, e.g., Glide or Picasso
                            // Glide.with(this).load(imageUrl).into(binding.profileImage)
                        }
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUserData() {
        val updatedName = binding.fullName.text.toString().trim()
        val updatedBio = binding.userBio.text.toString().trim()

        // Exception if user doesn't fill up the fields
        if (updatedName.isEmpty() || updatedBio.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_LONG).show()
            return
        }

        currentUser?.let {
            val userId = it.uid
            val userUpdates = mapOf(
                "fullName" to updatedName,
                "bio" to updatedBio,
                "profileImage" to imageLink // Save the image link in the database
            )

            userDB.child("User").child(userId).updateChildren(userUpdates).addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_LONG).show()
            }
        }
    }
}
