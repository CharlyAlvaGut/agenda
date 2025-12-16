package com.ipn.escom.moviles.agenda

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class ConsultFragment : Fragment(R.layout.fragment_consult) {

    private lateinit var rgCriterio: RadioGroup

    private lateinit var layoutFecha: View
    private lateinit var layoutRango: View
    private lateinit var layoutMes: View
    private lateinit var layoutAnio: View

    // Campos de fecha
    private lateinit var etFechaConsulta: TextInputEditText
    private lateinit var etFechaInicio: TextInputEditText
    private lateinit var etFechaFin: TextInputEditText
    private lateinit var etAnio: TextInputEditText

    // Contenedor donde se agregan las tarjetas de resultados
    private lateinit var containerResultados: LinearLayout

    // Spinners
    private lateinit var spMes: Spinner
    private lateinit var spTipo: Spinner

    // URLs base de tu API
    private val BASE_URL_CONSULTA = "http://10.0.2.2/agenda_api/consult_eventos.php"
    private val URL_DELETE = "http://10.0.2.2/agenda_api/delete_evento.php"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ====== Referencias a layouts ======
        rgCriterio = view.findViewById(R.id.rgCriterio)

        layoutFecha = view.findViewById(R.id.layoutFecha)
        layoutRango = view.findViewById(R.id.layoutRango)
        layoutMes   = view.findViewById(R.id.layoutMes)
        layoutAnio  = view.findViewById(R.id.layoutAnio)

        // ====== Referencias a inputs ======
        etFechaConsulta = view.findViewById(R.id.etFechaConsulta)
        etFechaInicio   = view.findViewById(R.id.etFechaInicio)
        etFechaFin      = view.findViewById(R.id.etFechaFin)
        etAnio          = view.findViewById(R.id.etAnio)

        spMes  = view.findViewById(R.id.spMes)
        spTipo = view.findViewById(R.id.spTipoEvento)
        val btnBuscar: Button = view.findViewById(R.id.btnBuscar)

        // Contenedor de resultados
        containerResultados = view.findViewById(R.id.containerResultados)

        // ====== DatePickers ======
        etFechaConsulta.setOnClickListener { mostrarDatePicker(etFechaConsulta) }
        etFechaInicio.setOnClickListener { mostrarDatePicker(etFechaInicio) }
        etFechaFin.setOnClickListener { mostrarDatePicker(etFechaFin) }

        // ====== Spinner de meses ======
        val meses = listOf(
            "Enero", "Febrero", "Marzo", "Abril",
            "Mayo", "Junio", "Julio", "Agosto",
            "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        val adapterMeses = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            meses
        )
        adapterMeses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMes.adapter = adapterMeses

        // ====== Spinner de tipos de evento ======
        val tipos = listOf("Todos", "Cita", "Junta", "Entrega de proyecto", "Examen", "Otros")

        val adapterTipos = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tipos
        )
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipo.adapter = adapterTipos

        // ====== Estado inicial: FECHA ======
        rgCriterio.check(R.id.rbFecha)
        mostrarSoloFecha()

        // ====== Cambio de criterio ======
        rgCriterio.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbFecha -> mostrarSoloFecha()
                R.id.rbRango -> mostrarSoloRango()
                R.id.rbMes   -> mostrarSoloMes()
                R.id.rbAnio  -> mostrarSoloAnio()
            }
        }

        // ====== Botón Buscar ======
        btnBuscar.setOnClickListener {
            val criterioId = rgCriterio.checkedRadioButtonId
            val criterioApi = when (criterioId) {
                R.id.rbFecha -> "fecha"
                R.id.rbRango -> "rango"
                R.id.rbMes   -> "mes"
                R.id.rbAnio  -> "anio"
                else -> "fecha"
            }

            val idCategoria = obtenerIdCategoriaDesdeSpinner(spTipo.selectedItemPosition)
            val url = construirUrlConsulta(criterioApi, idCategoria) ?: return@setOnClickListener

            consultarEventos(url)
        }
    }

    // ====== Construir URL según criterio y validar campos ======
    private fun construirUrlConsulta(criterio: String, idCategoria: Int?): String? {
        val builder = Uri.parse(BASE_URL_CONSULTA).buildUpon()
        builder.appendQueryParameter("criterio", criterio)

        when (criterio) {
            "fecha" -> {
                val fecha = etFechaConsulta.text.toString().trim()
                if (fecha.isEmpty()) {
                    Toast.makeText(requireContext(), "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                    return null
                }
                builder.appendQueryParameter("fecha", fecha)
            }

            "rango" -> {
                val inicio = etFechaInicio.text.toString().trim()
                val fin    = etFechaFin.text.toString().trim()
                if (inicio.isEmpty() || fin.isEmpty()) {
                    Toast.makeText(requireContext(), "Completa fecha inicio y fecha fin", Toast.LENGTH_SHORT).show()
                    return null
                }
                builder.appendQueryParameter("fecha_inicio", inicio)
                builder.appendQueryParameter("fecha_fin", fin)
            }

            "mes" -> {
                val mesSeleccionado = spMes.selectedItemPosition + 1
                builder.appendQueryParameter("mes", mesSeleccionado.toString())

                // El año es opcional, si el usuario escribió algo, lo mandamos. Si no, no pasa nada.
                val anioTexto = etAnio.text.toString().trim()
                if (anioTexto.isNotEmpty()) {
                    builder.appendQueryParameter("anio", anioTexto)
                }
            }

            "anio" -> {
                val anioTexto = etAnio.text.toString().trim()
                if (anioTexto.isEmpty()) {
                    Toast.makeText(requireContext(), "Captura el año", Toast.LENGTH_SHORT).show()
                    return null
                }
                builder.appendQueryParameter("anio", anioTexto)
            }
        }

        if (idCategoria != null && idCategoria > 0) {
            builder.appendQueryParameter("id_categoria", idCategoria.toString())
        }

        return builder.build().toString()
    }

    // ====== Llamada a la API con Volley ======
    private fun consultarEventos(url: String) {
        containerResultados.removeAllViews()

        val queue = Volley.newRequestQueue(requireContext())

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val ok = response.optBoolean("ok", false)
                    if (!ok) {
                        val errorMsg = response.optString("error", "Error en la respuesta del servidor")
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        return@JsonObjectRequest
                    }

                    val eventosJson = response.optJSONArray("eventos")
                    val lista = mutableListOf<EventoResultado>()

                    if (eventosJson != null) {
                        for (i in 0 until eventosJson.length()) {
                            val ev = eventosJson.getJSONObject(i)


                            val idEvento  = ev.optInt("id_evento", 0)
                            val fecha     = ev.optString("fecha", "")
                            val categoria = ev.optString("categoria", "")
                            val evento    = ev.optString("evento", "")

                            lista.add(
                                EventoResultado(
                                    idEvento = idEvento,
                                    fecha    = fecha,
                                    tipo     = categoria,
                                    contacto = evento
                                )
                            )
                        }
                    }

                    if (lista.isEmpty()) {
                        Toast.makeText(requireContext(), "No se encontraron eventos", Toast.LENGTH_SHORT).show()
                    }

                    mostrarResultados(lista)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error al procesar la respuesta", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Error de red: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        queue.add(request)
    }

    // ====== Mapea posición del Spinner a id_categoria (1..5) ======
    private fun obtenerIdCategoriaDesdeSpinner(pos: Int): Int? {
        return when (pos) {
            1 -> 1 // Cita
            2 -> 2 // Junta
            3 -> 3 // Entrega de proyecto
            4 -> 4 // Examen
            5 -> 5 // Otros
            else -> null // "Todos"
        }
    }

    // ====== Funcion para DatePicker ======
    private fun mostrarDatePicker(target: TextInputEditText) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val mesReal = m + 1
                val fechaFormateada = String.format("%04d-%02d-%02d", y, mesReal, d)
                target.setText(fechaFormateada)
            },
            year,
            month,
            day
        )

        dpd.show()
    }

    // ====== Helpers de visibilidad ======
    private fun ocultarTodos() {
        layoutFecha.visibility = View.GONE
        layoutRango.visibility = View.GONE
        layoutMes.visibility = View.GONE
        layoutAnio.visibility = View.GONE
    }

    private fun mostrarSoloFecha() {
        ocultarTodos()
        layoutFecha.visibility = View.VISIBLE
    }

    private fun mostrarSoloRango() {
        ocultarTodos()
        layoutRango.visibility = View.VISIBLE
    }

    private fun mostrarSoloMes() {
        ocultarTodos()
        layoutMes.visibility = View.VISIBLE
    }

    private fun mostrarSoloAnio() {
        ocultarTodos()
        layoutAnio.visibility = View.VISIBLE
    }

    // ====== Pintar tarjetas de resultado ======
    private fun mostrarResultados(lista: List<EventoResultado>) {
        containerResultados.removeAllViews()

        val inflater = layoutInflater

        for (evento in lista) {
            val cardView = inflater.inflate(R.layout.item_evento, containerResultados, false)

            val tvFecha: TextView = cardView.findViewById(R.id.tvFecha)
            val tvTipo: TextView = cardView.findViewById(R.id.tvTipo)
            val tvContacto: TextView = cardView.findViewById(R.id.tvContacto)
            val btnModificar: Button = cardView.findViewById(R.id.btnModificar)
            val btnEliminar: Button = cardView.findViewById(R.id.btnEliminar)

            tvFecha.text = evento.fecha
            tvTipo.text = evento.tipo
            tvContacto.text = evento.contacto

            btnModificar.setOnClickListener {
                // Listas iguales a las de AddEventFragment
                val categorias = listOf("Cita", "Junta", "Entrega de proyecto", "Examen", "Otros")

                // Sacamos el índice de la categoría según el texto que regresó la API
                val catIndex = categorias.indexOfFirst {
                    it.equals(evento.tipo, ignoreCase = true)
                }.let { idx ->
                    if (idx >= 0) idx else 0
                }

                // Por ahora no traes estatus desde la API, lo dejamos en 0 (Pendiente)
                val estatusIndex = 0

                // Armamos los argumentos para AddEventFragment
                val args = Bundle().apply {
                    putString("id_evento", evento.idEvento.toString())
                    putString("fecha", evento.fecha)
                    putString("hora", "")                // cuando tu API de consulta traiga hora, la pones aquí
                    putString("descripcion", evento.contacto)
                    putString("ubicacion", "")           // cuando tengas lat,long desde la API, lo pones aquí

                    putInt("categoria_index", catIndex)
                    putInt("estatus_index", estatusIndex)
                }

                val fragment = AddEventFragment().apply {
                    arguments = args
                }

                // ⚠️ Cambia R.id.fragment_container por el ID real de tu contenedor de fragments en la Activity
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }


            btnEliminar.setOnClickListener {
                eliminarEventoEnServidor(evento.idEvento)
            }

            containerResultados.addView(cardView)
        }
    }

    // ====== ELIMINAR EN EL SERVIDOR ======
    private fun eliminarEventoEnServidor(idEvento: Int) {
        val queue = Volley.newRequestQueue(requireContext())

        val request = object : StringRequest(
            Method.POST,
            URL_DELETE,
            { response ->
                Toast.makeText(requireContext(), "Evento eliminado", Toast.LENGTH_SHORT).show()
                // Opcional: volver a ejecutar la búsqueda actual
                containerResultados.removeAllViews()
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Error al eliminar: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_evento" to idEvento.toString())
            }
        }

        queue.add(request)
    }
}

// Modelo con idEvento incluido
data class EventoResultado(
    val idEvento: Int,
    val fecha: String,
    val tipo: String,      // nombre de la categoría
    val contacto: String   // descripción del evento
)
