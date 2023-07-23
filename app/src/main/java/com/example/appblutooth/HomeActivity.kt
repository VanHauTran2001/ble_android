package com.example.appblutooth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.appblutooth.client.MainActivity
import com.example.appblutooth.databinding.ActivityHomeBinding
import com.example.appblutooth.server.AdvertiserActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_home)
        binding.btnClient.setOnClickListener {
            startActivity(Intent(this@HomeActivity,MainActivity::class.java))
        }
        binding.btnServer.setOnClickListener {
            startActivity(Intent(this@HomeActivity,AdvertiserActivity::class.java))
        }

    }
}