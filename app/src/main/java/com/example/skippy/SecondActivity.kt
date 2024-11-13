package com.example.skippy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.skippy.databinding.ActivitySecondBinding
import android.util.Log

class SecondActivity : Activity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura o clique do botão "Inicio"
        binding.buttonInicio.setOnClickListener {
            Log.d("SecondActivity", "Botão Inicio")
        }

        // Configura o clique do botão "Final"
        binding.buttonFinal.setOnClickListener {
            Log.d("SecondActivity", "Botão Final")
        }

        binding.buttonVoltar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}