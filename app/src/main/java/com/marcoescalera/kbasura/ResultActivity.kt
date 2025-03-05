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
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ResultActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var tvResult: TextView
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Inicializar las vistas
        imageView = findViewById(R.id.imageView)
        tvResult = findViewById(R.id.tv_result)

        val btnRetry = findViewById<Button>(R.id.btn_retry)
        btnRetry.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java) // Asegúrate de que CaptureActivity es la pantalla de captura
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Opcional: cierra ResultActivity al volver
            startActivity(intent)
            finish() // Cierra ResultActivity para evitar acumulación en la pila de actividades
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
                // Abrir la imagen desde la URI
                val inputStream = contentResolver.openInputStream(android.net.Uri.parse(photoUri))
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap != null) {
                    Log.d("ResultActivity", "Imagen cargada correctamente.")

                    // Mostrar la imagen en el ImageView
                    imageView.setImageBitmap(bitmap)

                    // Clasificar la imagen y mostrar el resultado
                    val result = classifyImage(bitmap)
                    tvResult.text = "$result"
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

    private fun exitApp() {
        finishAffinity() // Cierra todas las actividades de la app
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
        // Preprocesar la imagen (redimensionar, normalizar, etc.)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Ejecutar el modelo
        val output = Array(1) { FloatArray(7) } // Salida del modelo con 7 categorías
        try {
            tflite.run(byteBuffer, output)
            Log.d("ResultActivity", "Modelo ejecutado correctamente.")
        } catch (e: Exception) {
            Log.e("ResultActivity", "Error al ejecutar el modelo", e)
            Toast.makeText(this, "Error al ejecutar el modelo", Toast.LENGTH_SHORT).show()
            return "Error en la clasificación"
        }

        // Mostrar la salida del modelo para depuración
        Log.d("ResultActivity", "Resultado del modelo: ${output[0].contentToString()}")

        // Obtener la categoría con la probabilidad más alta
        val categories = listOf("DESECHOS", "INFECCIOSO - PELIGROSO", "METAL", "ORGANICO", "PAPEL", "PLASTICO", "VIDRIO")
        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        // Verificar que la clasificación esté bien
        Log.d("ResultActivity", "Índice con mayor probabilidad: $maxIndex")

        return if (maxIndex in categories.indices) {
            categories[maxIndex] // Devuelve la categoría correspondiente
        } else {
            "Desconocido" // En caso de que el índice no sea válido
        }
    }



    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3) // Ajusta según tu modelo
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)

        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        for (pixelValue in intValues) {
            // Normalizar los valores de píxeles (ajusta según tu modelo)
            byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f) // Canal R
            byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)  // Canal G
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)          // Canal B
        }
        return byteBuffer
    }
}
