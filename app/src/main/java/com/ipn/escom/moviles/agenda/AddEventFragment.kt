package com.ipn.escom.moviles.agenda

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.Calendar

class AddEventFragment : Fragment(R.layout.fragment_add_event) {

    // --- 1. DECLARAMOS LAS VARIABLES AQUÍ ARRIBA PARA QUE SEAN GLOBALES ---
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etContacto: EditText
    private lateinit var etUbicacion: EditText
    private lateinit var spCategoria: Spinner
    private lateinit var spStatus: Spinner

    // Lanzadores de permisos y resultados
    private val contactPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val contactUri = data?.data ?: return@registerForActivityResult
            val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            val cursor = requireContext().contentResolver.query(contactUri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val name = it.getString(0)
                    etContacto.setText(name)
                }
            }
        }
    }

    private val mapPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val ubicacion = result.data?.getStringExtra("ubicacion")
            etUbicacion.setText(ubicacion)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            abrirAgendaContactos()
        } else {
            Toast.makeText(requireContext(), "Se requiere permiso para contactos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 2. AQUÍ SOLO LAS INICIALIZAMOS (quitamos el 'val' del principio) ---
        spCategoria = view.findViewById(R.id.spCategoria)
        spStatus = view.findViewById(R.id.spStatus)
        etFecha = view.findViewById(R.id.etFecha)
        etHora = view.findViewById(R.id.etHora)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        etUbicacion = view.findViewById(R.id.etUbicacion)
        etContacto = view.findViewById(R.id.etContacto)
        val btnGuardar: Button = view.findViewById(R.id.btnGuardar)

        // Configuración de Spinners
        val categorias = listOf("Cita", "Junta", "Entrega de proyecto", "Examen", "Otros")
        val adapterCategoria = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategoria.adapter = adapterCategoria

        val estados = listOf("Pendiente", "Realizado", "Aplazado")
        val adapterStatus = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estados)
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStatus.adapter = adapterStatus
        spStatus.setSelection(0)

        // Lógica de Fecha
        etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val fechaFormateada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    etFecha.setText(fechaFormateada)
                },
                calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Lógica de Hora
        etHora.setOnClickListener {
            val calendario = Calendar.getInstance()
            android.app.TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val horaFormateada = String.format("%02d:%02d:00", hourOfDay, minute)
                    etHora.setText(horaFormateada)
                },
                calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true
            ).show()
        }

        // Lógica de Contactos
        etContacto.setOnClickListener {
            verificarPermisoYAbirContactos()
        }

        // Lógica de Ubicación (Mapa)
        etUbicacion.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            mapPickerLauncher.launch(intent)
        }

        // Botón Guardar
        btnGuardar.setOnClickListener {
            val fecha = etFecha.text.toString()
            val hora = etHora.text.toString()
            val desc = etDescripcion.text.toString()

            if (fecha.isEmpty() || hora.isEmpty() || desc.isEmpty()) {
                Toast.makeText(requireContext(), "Llena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mapeo de IDs
            val categoriaId = when (spCategoria.selectedItemPosition) {
                0 -> "1"
                1 -> "2"
                2 -> "3"
                3 -> "4"
                4 -> "5"
                else -> "1"
            }
            val statusId = when (spStatus.selectedItemPosition) {
                0 -> "2" // Pendiente
                1 -> "1" // Realizado
                2 -> "3" // Aplazado
                else -> "2"
            }

            guardarEventoEnServidor(fecha, hora, desc, categoriaId, statusId)
        }
    }

    private fun verificarPermisoYAbirContactos() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            abrirAgendaContactos()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun abrirAgendaContactos() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        contactPickerLauncher.launch(intent)
    }

    private fun guardarEventoEnServidor(fecha: String, hora: String, evento: String, idCat: String, idStatus: String) {
        val url = "http://10.0.2.2/agenda_api/add_evento.php"
        val queue = Volley.newRequestQueue(requireContext())

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                Log.d("API_SUCCESS", response)
                Toast.makeText(requireContext(), "¡Guardado con éxito!", Toast.LENGTH_LONG).show()

                etFecha.text.clear()
                etHora.text.clear()
                etDescripcion.text.clear()
                etContacto.text.clear()
                spCategoria.setSelection(0)
                spStatus.setSelection(0)
            },
            { error ->
                Log.e("API_ERROR", "Detalle: $error") // Imprime el error crudo en Logcat

                val mensajeError = when (error) {
                    is com.android.volley.TimeoutError -> "Tiempo de espera agotado"
                    is com.android.volley.NoConnectionError -> "No hay conexión con el servidor"
                    is com.android.volley.AuthFailureError -> "Error de autenticación"
                    is com.android.volley.ServerError -> "Error en el servidor (Revisa PHP)"
                    is com.android.volley.NetworkError -> "Error de red"
                    is com.android.volley.ParseError -> "Error al leer respuesta"
                    else -> "Error desconocido: ${error.message}"
                }

                Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                val ubicacion = etUbicacion.text.toString().trim()
                val partes = ubicacion.split(",")
                val latitud = partes[0].trim()
                val longitud = partes[1].trim()

                params["fecha"] = fecha
                params["hora"] = hora

                params["evento"] = evento
                params["id_categoria"] = idCat
                params["id_estatus"] = idStatus
                params["latitud"] = latitud
                params["longitud"] = longitud

                // params["contacto"] = etContacto.text.toString() // Si decides enviarlo después
                return params
            }
        }
        queue.add(stringRequest)
    }
}