package com.marcoescalera.kbasura

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import android.Manifest
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaptureActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private val STORAGE_PERMISSION_REQUEST_CODE = 101

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            // Permiso ya concedido
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                Toast.makeText(this, "Se necesita acceso al almacenamiento para guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        previewView = findViewById(R.id.previewView)
        val btnTakePhoto = findViewById<Button>(R.id.btn_take_photo)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara no concedido", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnTakePhoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Crear un archivo temporal para la imagen
        val photoFile = createImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Guardar la imagen en la galería
                    saveImageToGallery(photoFile)

                    // Pasar la ruta de la imagen a ResultActivity
                    val intent = Intent(this@CaptureActivity, ResultActivity::class.java)
                    intent.putExtra("photo_path", photoFile.absolutePath)
                    startActivity(intent)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                }
            })
    }

    private fun createImageFile(): File {
        // Crear un nombre único para la imagen
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", // Prefijo
            ".jpg", // Sufijo
            storageDir // Directorio
        )
    }

    private fun saveImageToGallery(imageFile: File) {
        // Crear un ContentValues para la imagen
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        // Insertar la imagen en la galería
        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                // Copiar la imagen al archivo de la galería
                val outputStream = resolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    val inputStream = FileInputStream(imageFile)
                    inputStream.copyTo(stream)
                    inputStream.close()
                }
                Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Log.e("CaptureActivity", "Error al guardar la imagen en la galería", e)
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}