package com.example.appwithlibmodule

import com.example.lib.NOP
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NOP.printMessage("AMESSAGE")
    }
}
