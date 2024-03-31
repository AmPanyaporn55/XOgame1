package com.example.xogame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputBinding
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gridSizeEditText: EditText
    private lateinit var createGridButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridSizeEditText = findViewById(R.id.etGridSize)
        createGridButton = findViewById(R.id.btnCreateGrid)

        createGridButton.setOnClickListener {
            val size = gridSizeEditText.text.toString().toIntOrNull()
            if (size != null && size in 3..10) {
                val intent = Intent(this, BoardActivity::class.java)
                intent.putExtra("GRID_SIZE", size)
                startActivity(intent)
            } else {
                gridSizeEditText.error = "Enter a number between 3 and 10"
            }
        }
    }
}

