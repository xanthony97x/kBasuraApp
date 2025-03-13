package com.marcoescalera.kbasura

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Verificar permisos de la cámara antes de iniciar la actividad de captura
        Handler(Looper.getMainLooper()).postDelayed({
            checkCameraPermission()
        }, 3000) // Espera 3 segundos antes de continuar
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            openCaptureActivity()
        }
    }

    private fun openCaptureActivity() {
        val intent = Intent(this, CaptureActivity::class.java)
        startActivity(intent)
        finish() // Cierra MainActivity para que no vuelva atrás
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCaptureActivity()
            } else {
                Toast.makeText(this, "Se necesita acceso a la cámara para usar esta función", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
