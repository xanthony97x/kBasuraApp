package com.marcoescalera.kbasura

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest // Importación necesaria

class MainActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCapture = findViewById<Button>(R.id.btn_capture)
        btnCapture.setOnClickListener {
            checkCameraPermission()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicitarlo
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            // Si el permiso ya está concedido, abrir la actividad de captura
            openCaptureActivity()
        }
    }

    private fun openCaptureActivity() {
        val intent = Intent(this, CaptureActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, abrir la actividad de captura
                openCaptureActivity()
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(this, "Se necesita acceso a la cámara para usar esta función", Toast.LENGTH_SHORT).show()
            }
        }
    }
}