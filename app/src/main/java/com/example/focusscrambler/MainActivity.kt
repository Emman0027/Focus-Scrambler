package com.example.focusscrambler

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This loads the UI from app/src/main/res/layout/activity_login_page.xml
        val intent = Intent(this, loading_page::class.java)
        startActivity(intent)

        // Finish MainActivity so the user cannot navigate back to it
        finish()
    }
}
