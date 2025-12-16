package com.ipn.escom.moviles.agenda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.card.MaterialCardView

class RestoreFragment : Fragment(R.layout.fragment_restore) {

    // Launcher para "Abrir Documento"
    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // Leer el contenido del archivo
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val jsonString = inputStream?.bufferedReader().use { it?.readText() }

                    if (!jsonString.isNullOrEmpty()) {
                        enviarDatosAlServidor(jsonString)
                    } else {
                        Toast.makeText(context, "El archivo está vacío", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al leer archivo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UN SOLO LISTENER
        view.findViewById<MaterialCardView>(R.id.cardRestoreAction).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            openFileLauncher.launch(intent)
        }
    }

    private fun enviarDatosAlServidor(json: String) {
        // CAMBIA LA IP POR LA TUYA SI USAS CELULAR FÍSICO
        val url = "http://10.0.2.2/agenda_api/restore_data.php"

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                Toast.makeText(context, "Restauración completada.", Toast.LENGTH_LONG).show()
            },
            { error ->
                Toast.makeText(context, "Error en restauración: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            // Enviamos el JSON como cuerpo de la petición (Body)
            override fun getBody(): ByteArray = json.toByteArray()
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }
}