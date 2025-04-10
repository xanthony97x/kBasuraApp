package com.marcoescalera.kbasura

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InformationActivity : AppCompatActivity() {

    private val items = listOf(
        WasteItem("PAPEL", R.drawable.icc_papel, "Cartón, sobres, periódicos, bolsas de papel."),
        WasteItem("METAL", R.drawable.icc_metal, "Latas, envases de conserva, aluminio, alambres."),
        WasteItem("VIDRIO", R.drawable.icc_vidrio, "Botellas, frascos, vasos, copas."),
        WasteItem("PLASTICO", R.drawable.icc_plastico, "Bolsas plásticas, botellas, envases plásticos."),
        WasteItem("ORGANICO", R.drawable.icc_organico, "Restos de frutas/verduras, restos de jardín."),
        WasteItem("DESECHOS", R.drawable.icc_desechos, "Colillas de cigarrillos, envolturas contaminadas, trapos."),
        WasteItem("INFECCIOSO_PELIGROSO", R.drawable.icc_infeccioso_peligroso, "Jeringas, gasas, barbijos, pilas/baterías.")
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