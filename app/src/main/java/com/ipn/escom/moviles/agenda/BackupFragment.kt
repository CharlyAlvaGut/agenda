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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupFragment : Fragment(R.layout.fragment_backup) {

    private var jsonRespaldo: String = ""

    // Launcher para "Crear Documento"
    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // Escribir el JSON en el archivo seleccionado
                    requireContext().contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(jsonRespaldo.toByteArray())
                    }
                    Toast.makeText(context, "¡Respaldo guardado exitosamente!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar archivo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UN SOLO LISTENER
        view.findViewById<MaterialCardView>(R.id.cardBackupAction).setOnClickListener {
            iniciarRespaldo()
        }
    }

    private fun iniciarRespaldo() {
        // CAMBIA LA IP POR LA TUYA SI USAS CELULAR FÍSICO
        val url = Config.BASE_URL+"backup_data.php"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                jsonRespaldo = response // Guardamos el JSON que llegó
                if (jsonRespaldo.length > 5) { // Validación simple de que llegó algo
                    abrirSelectorDeGuardado()
                } else {
                    Toast.makeText(context, "No hay datos para respaldar", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun abrirSelectorDeGuardado() {
        val fecha = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val nombreArchivo = "agenda_respaldo_$fecha.json"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json" // Tipo de archivo
            putExtra(Intent.EXTRA_TITLE, nombreArchivo)
        }
        saveFileLauncher.launch(intent)
    }
}