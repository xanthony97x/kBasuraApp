package com.marcoescalera.kbasura

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ResultActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var tvResult: TextView
    private lateinit var iconView: ImageView
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)

        imageView = findViewById(R.id.imageView)
        tvResult = findViewById(R.id.tv_result)
        iconView = findViewById(R.id.iconView)

        val btnRetry = findViewById<Button>(R.id.btn_retry)
        btnRetry.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        val btnExit = findViewById<Button>(R.id.btn_exit)
        btnExit.setOnClickListener {
            exitApp()
        }

        try {
            tflite = Interpreter(loadModelFile())
            Log.d("ResultActivity", "Modelo cargado correctamente.")
        } catch (e: Exception) {
            Log.e("ResultActivity", "Error al cargar el modelo", e)
            return
        }

        val photoUri = intent.getStringExtra("photo_uri")
        Log.d("ResultActivity", "Ruta de la imagen recibida: $photoUri")

        if (photoUri != null) {
            try {
                val inputStream = contentResolver.openInputStream(android.net.Uri.parse(photoUri))
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                if (originalBitmap != null) {
                    Log.d("ResultActivity", "Imagen cargada correctamente.")

                    // Mostrar la imagen original en el ImageView
                    imageView.setImageBitmap(originalBitmap)

                    // Procesar la imagen para el modelo: recorte central + redimensionado
                    val processedBitmap = prepareImageForModel(originalBitmap)
                    //imageView.setImageBitmap(processedBitmap)
                    val result = classifyImage(processedBitmap)
                    updateUI(result)
                } else {
                    Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ResultActivity", "Error al cargar la imagen", e)
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ruta de la imagen no encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prepareImageForModel(bitmap: Bitmap): Bitmap {
        // 1. Recortar cuadrado central
        val croppedBitmap = cropToSquare(bitmap)
        // 2. Redimensionar a 224x224
        return Bitmap.createScaledBitmap(croppedBitmap, 224, 224, true)
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height) // Tamaño del lado más corto
        val x = (bitmap.width - size) / 2 // Punto de inicio X (centrado)
        val y = (bitmap.height - size) / 2 // Punto de inicio Y (centrado)

        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    private fun updateUI(result: String) {
        val (color, icon, message) = when (result) {
            "VIDRIO", "PLASTICO", "PAPEL", "METAL" -> {
                Triple(R.color.white, R.drawable.ic_reciclable, "$result")
            }
            "ORGANICO" -> {
                Triple(R.color.white, R.drawable.ic_organico, "$result")
            }
            "DESECHOS", "INFECCIOSO_PELIGROSO" -> {
                Triple(R.color.white, R.drawable.ic_no_reciclable, "$result")
            }
            else -> {
                Triple(R.color.white, R.drawable.ic_error, "DESCONOCIDO")
            }
        }

        tvResult.text = message
        tvResult.setTextColor(ContextCompat.getColor(this, color))
        iconView.setImageResource(icon)
    }

    private fun exitApp() {
        finishAffinity()
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("MNV2.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun classifyImage(processedBitmap: Bitmap): String {
        val byteBuffer = convertBitmapToByteBuffer(processedBitmap)

        val output = Array(1) { FloatArray(7) }
        try {
            tflite.run(byteBuffer, output)
            Log.d("ResultActivity", "Modelo ejecutado correctamente.")
        } catch (e: Exception) {
            Log.e("ResultActivity", "Error al ejecutar el modelo", e)
            return "Error en la clasificación"
        }

        val categories = listOf("DESECHOS", "INFECCIOSO_PELIGROSO", "METAL", "ORGANICO", "PAPEL", "PLASTICO", "VIDRIO")
        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        Log.d("ResultActivity", "Índice con mayor probabilidad: $maxIndex")

        return if (maxIndex in categories.indices) {
            categories[maxIndex]
        } else {
            "DESCONOCIDO"
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)

        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }
        return byteBuffer
    }
}