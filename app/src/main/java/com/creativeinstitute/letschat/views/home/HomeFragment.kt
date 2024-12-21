package com.creativeinstitute.letschat.views.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.creativeinstitute.letschat.ProfileFragment
import com.creativeinstitute.letschat.R
import com.creativeinstitute.letschat.databinding.FragmentHomeBinding
import com.creativeinstitute.letschat.nodes.DBNodes
import com.creativeinstitute.letschat.utils.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment(), UserAdapter.UserListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userDB: DatabaseReference
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var adapter: UserAdapter

    //
    val userList: MutableList<User> = mutableListOf()

    private val jAuth = FirebaseAuth.getInstance()

    private lateinit var firebaseUser: FirebaseUser

    private var currentUser : User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        //Firebase Auth if current user ->
        FirebaseAuth.getInstance().currentUser?.let {
            firebaseUser = it
        }



        // Initialize the drawer layout
        drawerLayout = binding.homeDrawerLayout

        // Set up the FloatingActionButton to open the navigation drawer
        binding.openDrawerFab.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


        // Inside setNavigationItemSelectedListener
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    if (currentUser != null) {
                        val bundle = Bundle().apply {
                            putString(ProfileFragment.USERID, currentUser!!.userId)
                        }
                        findNavController().navigate(R.id.action_homeFragment_to_profileFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "User data is loading, please try again", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                R.id.nav_settings -> {
                    // Navigate to Settings Fragment
                    true
                }
                R.id.logout_btn -> {
                    // Sign out and navigate to Login Fragment
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                    true
                }
                else -> false
            }
        }

        //ADAPTER
        adapter = UserAdapter(this@HomeFragment)
        binding.userRecyclerView.adapter = adapter

        getAvailableUser()


        return binding.root
    }

    private fun getAvailableUser() {
        userDB.child(DBNodes.USER).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    userList.clear()
                    snapshot.children.forEach{
                        val user: User = it.getValue(User::class.java)!!

                        if(firebaseUser.uid!= user.userId){
                            userList.add(user)
                        }else{
                            currentUser = user
                        }

                    }

                    adapter.submitList(userList)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Close the drawer if it’s open when the fragment is destroyed
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun userItemClick(user: User) {

        val bundle = Bundle()
        bundle.putString(ProfileFragment.USERID, user.userId)

        findNavController().navigate(R.id.action_homeFragment_to_profileFragment, bundle)

    }
}
