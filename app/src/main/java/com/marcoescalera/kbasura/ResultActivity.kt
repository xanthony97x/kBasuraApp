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

        // Inicializar las vistas
        imageView = findViewById(R.id.imageView)
        tvResult = findViewById(R.id.tv_result)
        iconView = findViewById(R.id.iconView) // Nuevo ImageView agregado en el XML

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

        // Cargar el modelo TensorFlow Lite
        try {
            tflite = Interpreter(loadModelFile())
            Log.d("ResultActivity", "Modelo cargado correctamente.")
        } catch (e: Exception) {
            Log.e("ResultActivity", "Error al cargar el modelo", e)
            Toast.makeText(this, "Error al cargar el modelo", Toast.LENGTH_SHORT).show()
            return
        }

        // Recuperar la URI de la imagen desde el Intent
        val photoUri = intent.getStringExtra("photo_uri")
        Log.d("ResultActivity", "Ruta de la imagen recibida: $photoUri")

        if (photoUri != null) {
            try {
                val inputStream = contentResolver.openInputStream(android.net.Uri.parse(photoUri))
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap != null) {
                    Log.d("ResultActivity", "Imagen cargada correctamente.")
                    imageView.setImageBitmap(bitmap)

                    // Clasificar la imagen y actualizar UI
                    val result = classifyImage(bitmap)
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

    private fun classifyImage(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

        val output = Array(1) { FloatArray(7) }
        try {
            tflite.run(byteBuffer, output)
            Log.d("ResultActivity", "Modelo ejecutado correctamente.")
        } catch (e: Exception) {
            Log.e("ResultActivity", "Error al ejecutar el modelo", e)
            Toast.makeText(this, "Error al ejecutar el modelo", Toast.LENGTH_SHORT).show()
            return "Error en la clasificación"
        }

        val categories = listOf("DESECHOS", "INFECCIOSO - PELIGROSO", "METAL", "ORGANICO", "PAPEL", "PLASTICO", "VIDRIO")
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
