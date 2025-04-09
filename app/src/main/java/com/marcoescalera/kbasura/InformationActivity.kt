package com.marcoescalera.kbasura

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InformationActivity : AppCompatActivity() {

    private val items = listOf(
        WasteItem("PAPEL", R.drawable.icc_papel, "Cajas de cartón, sobres, papel reciclado."),
        WasteItem("METAL", R.drawable.icc_metal, "Latas de conserva, aluminio, alambres."),
        WasteItem("VIDRIO", R.drawable.icc_vidrio, "Botellas, frascos, vasos, restos de ventanas."),
        WasteItem("PLASTICO", R.drawable.icc_plastico, "Bolsas plásticas, envoltorios, tapas."),
        WasteItem("ORGANICO", R.drawable.icc_organico, "Restos de fruta/verduras, restos de jardín."),
        WasteItem("DESECHOS", R.drawable.icc_desechos, "Colillas de cigarros, llantas, trapos viejos."),
        WasteItem("INFECCIOSO_PELIGROSO", R.drawable.icc_infeccioso_peligroso, "Jeringas, gasas, guantes médicos, barbijos.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WasteAdapter(items)

        val nextButton: Button = findViewById(R.id.btn_next)
        nextButton.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            startActivity(intent)
        }
    }
}
