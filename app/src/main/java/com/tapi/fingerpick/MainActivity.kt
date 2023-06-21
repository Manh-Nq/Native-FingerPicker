package com.tapi.fingerpick

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {
    var fingerView: FingerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        fingerView = findViewById(R.id.finger)

        findViewById<AppCompatButton>(R.id.btn).setOnClickListener {
            fingerView?.start()
        }
    }
}