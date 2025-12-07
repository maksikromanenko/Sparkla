package com.example.sparkla

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.sparkla.ui.fragments.HistoryFragment
import com.example.sparkla.ui.fragments.HomeFragment
import com.example.sparkla.ui.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var cartButton: ImageView

    private val homeFragment = HomeFragment()
    private val historyFragment = HistoryFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        cartButton = findViewById(R.id.cart_button)
        cartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        if (savedInstanceState == null) {
            loadFragment(homeFragment, "Главное меню")
            bottomNavigationView.selectedItemId = R.id.navigation_orders
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_orders -> {
                    loadFragment(homeFragment, "Главное меню")
                    true
                }
                R.id.navigation_history -> {
                    loadFragment(historyFragment, "История заказов")
                    true
                }
                R.id.navigation_profile -> {
                    loadFragment(profileFragment, "Профиль")
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        supportActionBar?.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()

        if (fragment is HomeFragment) {
            cartButton.visibility = View.VISIBLE
        } else {
            cartButton.visibility = View.GONE
        }
    }
}
