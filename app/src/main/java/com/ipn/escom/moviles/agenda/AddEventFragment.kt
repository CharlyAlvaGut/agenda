package com.ipn.escom.moviles.agenda

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
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
import java.text.SimpleDateFormat
import java.util.*

class AddEventFragment : Fragment(R.layout.fragment_add_event) {

    // --- MODO EDICIÓN / ALTA ---
    // Si viene en arguments, es edición; si no, es alta
    private var idEvento: String? = null

    // --- 1. DECLARAMOS LAS VARIABLES AQUÍ ARRIBA PARA QUE SEAN GLOBALES ---
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etContacto: EditText
    private lateinit var etUbicacion: EditText
    private lateinit var spCategoria: Spinner
    private lateinit var spStatus: Spinner

    private lateinit var spNotificacion: Spinner

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

        // --- NUEVO: Pedir permiso de notificaciones en Android 13+ ---
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Usamos el mismo launcher de permisos que ya tenías o creamos uno genérico
                // Para rápido, pedimos directo al sistema:
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // --- 0. Leemos argumentos (por si venimos en modo edición) ---
        idEvento = arguments?.getString("id_evento")

        val fechaArg  = arguments?.getString("fecha")
        val horaArg   = arguments?.getString("hora")
        val descArg   = arguments?.getString("descripcion")
        val ubicArg   = arguments?.getString("ubicacion")
        val catIdxArg = arguments?.getInt("categoria_index")  // 0..4
        val estIdxArg = arguments?.getInt("estatus_index")    // 0..2

        // --- 2. INICIALIZAMOS VISTAS ---
        spCategoria = view.findViewById(R.id.spCategoria)
        spStatus    = view.findViewById(R.id.spStatus)
        spNotificacion = view.findViewById(R.id.spNotificacion)
        etFecha     = view.findViewById(R.id.etFecha)
        etHora      = view.findViewById(R.id.etHora)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        etUbicacion   = view.findViewById(R.id.etUbicacion)
        etContacto    = view.findViewById(R.id.etContacto)
        val btnGuardar: Button = view.findViewById(R.id.btnGuardar)

        // --- 3. Configuración de Spinners ---
        val categorias = listOf("Cita", "Junta", "Entrega de proyecto", "Examen", "Otros")
        val adapterCategoria = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategoria.adapter = adapterCategoria

        val estados = listOf("Pendiente", "Realizado", "Aplazado")
        val adapterStatus = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estados)
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStatus.adapter = adapterStatus
        spStatus.setSelection(0)

        // --- CONFIGURAR SPINNER NOTIFICACIÓN ---
        val opcionesNotif = listOf("Sin recordatorio", "A la hora exacta", "10 minutos antes", "1 día antes")
        val adapterNotif = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opcionesNotif)
        adapterNotif.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNotificacion.adapter = adapterNotif
        spNotificacion.setSelection(0) // Por defecto: Sin recordatorio

        // --- 4. Si estamos en modo edición, rellenamos los campos ---
        if (idEvento != null) {
            btnGuardar.text = "Actualizar evento"

            fechaArg?.let { etFecha.setText(it) }
            horaArg?.let { etHora.setText(it) }
            descArg?.let { etDescripcion.setText(it) }
            ubicArg?.let { etUbicacion.setText(it) }

            catIdxArg?.let { spCategoria.setSelection(it) }
            estIdxArg?.let { spStatus.setSelection(it) }
        }

        // --- 5. Lógica de Fecha ---
        etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val fechaFormateada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    etFecha.setText(fechaFormateada)
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // --- 6. Lógica de Hora ---
        etHora.setOnClickListener {
            val calendario = Calendar.getInstance()
            android.app.TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val horaFormateada = String.format("%02d:%02d:00", hourOfDay, minute)
                    etHora.setText(horaFormateada)
                },
                calendario.get(Calendar.HOUR_OF_DAY),
                calendario.get(Calendar.MINUTE),
                true
            ).show()
        }

        // --- 7. Lógica de Contactos ---
        etContacto.setOnClickListener {
            verificarPermisoYAbirContactos()
        }

        // --- 8. Lógica de Ubicación (Mapa) ---
        etUbicacion.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            mapPickerLauncher.launch(intent)
        }

        // --- 9. Botón Guardar (INSERT o UPDATE según idEvento) ---
        btnGuardar.setOnClickListener {
            val fecha = etFecha.text.toString()
            val hora  = etHora.text.toString()
            val desc  = etDescripcion.text.toString()

            if (fecha.isEmpty() || hora.isEmpty() || desc.isEmpty()) {
                Toast.makeText(requireContext(), "Llena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mapeo de IDs de catálogo (como ya lo tenías)
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
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            abrirAgendaContactos()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun abrirAgendaContactos() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        contactPickerLauncher.launch(intent)
    }

    private fun guardarEventoEnServidor(
        fecha: String,
        hora: String,
        evento: String,
        idCat: String,
        idStatus: String
    ) {
        val url = Config.BASE_URL+"add_evento.php"
        val queue = Volley.newRequestQueue(requireContext())

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                Log.d("API_SUCCESS", response)

                val mensaje = if (idEvento == null) {
                    "¡Guardado con éxito!"
                } else {
                    "¡Actualizado con éxito!"
                }
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()

                // --- NUEVO: PROGRAMAR LA NOTIFICACIÓN ---
                val opcionNotif = spNotificacion.selectedItemPosition
                val tituloEvento = if (etDescripcion.text.isNotEmpty()) etDescripcion.text.toString() else "Evento Agenda"

                // Solo programamos si es un ALTA (idEvento == null) o si quieres reprogramar al editar
                // Usamos System.currentTimeMillis().toInt() como ID único temporal para la alarma
                val idUnico = System.currentTimeMillis().toInt()
                programarAlarma(fecha, hora, tituloEvento, opcionNotif, idUnico)

                // Solo limpiamos si es ALTA; en edición normalmente regresas a la lista
                if (idEvento == null) {
                    etFecha.text.clear()
                    etHora.text.clear()
                    etDescripcion.text.clear()
                    etContacto.text.clear()
                    etUbicacion.text.clear()
                    spCategoria.setSelection(0)
                    spStatus.setSelection(0)
                    spNotificacion.setSelection(0)
                }
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
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()

                // Fecha/hora/otros datos
                params["fecha"]       = fecha
                params["hora"]        = hora
                params["evento"]      = evento
                params["id_categoria"] = idCat
                params["id_estatus"]   = idStatus

                // Ubicación: solo si viene algo válido
                val ubicacion = etUbicacion.text.toString().trim()
                if (ubicacion.isNotEmpty() && ubicacion.contains(",")) {
                    val partes = ubicacion.split(",")
                    if (partes.size >= 2) {
                        val latitud = partes[0].trim()
                        val longitud = partes[1].trim()
                        params["latitud"] = latitud
                        params["longitud"] = longitud
                    }
                }

                // Si estamos en modo edición, mandamos el id_evento
                idEvento?.let {
                    params["id_evento"] = it
                }

                // params["contacto"] = etContacto.text.toString() // Por si luego lo guardas en BD
                return params
            }
        }

        queue.add(stringRequest)
    }

    private fun programarAlarma(fecha: String, hora: String, titulo: String, opcion: Int, idRequest: Int) {
        if (opcion == 0) return // "Sin recordatorio"

        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            // Parsear la fecha y hora del evento
            c.time = sdf.parse("$fecha $hora")!!
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        // Calcular el momento del disparo según la opción
        when (opcion) {
            1 -> {} // A la hora exacta (no restamos nada)
            2 -> c.add(Calendar.MINUTE, -10) // 10 min antes
            3 -> c.add(Calendar.DAY_OF_YEAR, -1) // 1 día antes
        }

        // Si la hora calculada ya pasó, no programamos nada
        if (c.timeInMillis < System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "El recordatorio sería en el pasado, no se programó.", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear el Intent para el BroadcastReceiver
        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("titulo", "Recordatorio: $titulo")
            putExtra("mensaje", "Es hora de tu evento programado: $titulo")
            putExtra("id_evento", idRequest)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            idRequest,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            // Usamos setExact para precisión (requiere permiso en Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
            }
            Toast.makeText(requireContext(), "Recordatorio programado", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            // En caso de que falte el permiso SCHEDULE_EXACT_ALARM
            Toast.makeText(requireContext(), "No se pudo programar alarma (Falta permiso)", Toast.LENGTH_SHORT).show()
        }
    }
}
