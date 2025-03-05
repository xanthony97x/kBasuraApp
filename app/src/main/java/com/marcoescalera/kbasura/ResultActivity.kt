package com.marcoescalera.kbasura

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        // Cargar el modelo TensorFlow Lite
        tflite = Interpreter(loadModelFile())

        imageView = findViewById(R.id.imageView)
        tvResult = findViewById(R.id.tv_result)

        // Recuperar la ruta de la imagen desde el Intent
        val photoPath = intent.getStringExtra("photo_path")

        if (photoPath != null) {
            // Cargar la imagen desde la ruta y mostrarla en el ImageView
            val bitmap = BitmapFactory.decodeFile(photoPath)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)

                // Clasificar la imagen y mostrar el resultado
                val result = classifyImage(bitmap)
                when (result) {
                    "vidrio" -> {
                        tvResult.text = "Resultado: Vidrio ♻️"
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    "plástico" -> {
                        tvResult.text = "Resultado: Plástico ♻️"
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    "papel" -> {
                        tvResult.text = "Resultado: Papel ♻️"
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    "orgánico" -> {
                        tvResult.text = "Resultado: Orgánico 🍂"
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    "metal" -> {
                        tvResult.text = "Resultado: Metal ♻️"
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    "infeccioso_peligroso" -> {
                        tvResult.text = "Resultado: Infeccioso/Peligroso ☣️"
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    "desechos" -> {
                        tvResult.text = "Resultado: Desechos 🗑️"
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    else -> {
                        tvResult.text = "Resultado: Desconocido ❓"
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                }
            } else {
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ruta de la imagen no encontrada", Toast.LENGTH_SHORT).show()
        }
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
        val output = Array(1) { FloatArray(8) } // Ajusta el tamaño según tu modelo
        tflite.run(byteBuffer, output)

        // Obtener la categoría con la probabilidad más alta
        val categories = listOf("vidrio", "plastico", "papel", "organico", "metal", "infeccioso_peligroso", "desechos")
        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1

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