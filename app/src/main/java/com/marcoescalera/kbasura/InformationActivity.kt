package com.marcoescalera.kbasura

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InformationActivity : AppCompatActivity() {

    private val items = listOf(
        WasteItem("PAPEL", R.drawable.ic_papel),
        WasteItem("METAL", R.drawable.ic_manico),
        WasteItem("VIRIO", R.drawable.ic_jallo),
        WasteItem("PLASTICO", R.drawable.ic_vidio),
        WasteItem("ORGANICO", R.drawable.ic_detal),
        WasteItem("DESECHOS", R.drawable.ic_odesechos),
        WasteItem("VACIO", R.drawable.ic_inliodchos),
        WasteItem("INFECCIOSO_PELIGROSO", R.drawable.ic_infeccioso),
        WasteItem("VACIO", R.drawable.ic_peligroso)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = WasteAdapter(items)

        val nextButton: Button = findViewById(R.id.btn_next)
        nextButton.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            startActivity(intent)
        }
    }
}
