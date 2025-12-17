package com.ipn.escom.moviles.agenda

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var containerHoy: LinearLayout
    private lateinit var containerSemana: LinearLayout
    private lateinit var containerOtras: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        containerHoy = view.findViewById(R.id.containerHoy)
        containerSemana = view.findViewById(R.id.containerSemana)
        containerOtras = view.findViewById(R.id.containerOtras)

        cargarEventosHome()
    }

    private fun cargarEventosHome() {
        // Usa tu IP correcta
        val url = Config.BASE_URL+"get_agenda_home.php"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                if (response.optBoolean("ok")) {
                    val jsonArray = response.getJSONArray("data")

                    // 1. OBTENER LA FECHA DE HOY COMO TEXTO (YYYY-MM-DD)
                    // Esto asegura que la comparación sea exacta, caracter por caracter.
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val hoyTexto = sdf.format(Date())

                    // Calendarios para calcular semanas
                    val calHoy = Calendar.getInstance()
                    val calEvento = Calendar.getInstance()

                    containerHoy.removeAllViews()
                    containerSemana.removeAllViews()
                    containerOtras.removeAllViews()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val fechaStr = obj.getString("fecha") // La fecha del evento (YYYY-MM-DD)
                        val titulo = obj.getString("titulo")
                        val hora = obj.getString("hora")
                        val estatus = obj.getString("estatus")
                        val lat = obj.optDouble("latitud", 0.0)
                        val lng = obj.optDouble("longitud", 0.0)

                        // Parsear fecha para cálculos de semana
                        val dateEvento = sdf.parse(fechaStr)
                        if (dateEvento != null) calEvento.time = dateEvento

                        // Inflar tarjeta
                        val card = layoutInflater.inflate(R.layout.item_evento_home, containerHoy, false) as MaterialCardView

                        card.findViewById<TextView>(R.id.tvTituloHome).text = titulo
                        card.findViewById<TextView>(R.id.tvFechaHome).text = fechaStr
                        card.findViewById<TextView>(R.id.tvHoraHome).text = hora

                        // Ubicación
                        val layoutUbicacion = card.findViewById<LinearLayout>(R.id.layoutUbicacion)
                        val tvUbicacion = card.findViewById<TextView>(R.id.tvUbicacionHome)
                        if (lat != 0.0 && lng != 0.0) {
                            layoutUbicacion.visibility = View.VISIBLE
                            obtenerDireccion(lat, lng, tvUbicacion)
                        } else {
                            layoutUbicacion.visibility = View.GONE
                        }

                        // Color Estatus
                        val tvStatus = card.findViewById<TextView>(R.id.tvStatusHome)
                        tvStatus.text = estatus
                        when (estatus.uppercase()) {
                            "PENDIENTE" -> tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                            "REALIZADO" -> tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                            "APLAZADO" -> tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
                        }

                        // --- LÓGICA DE CLASIFICACIÓN CORREGIDA ---

                        // 1. ¿Es Hoy? (Comparación de TEXTO directa)
                        // Si fechaStr es "2025-12-04" y hoyTexto es "2025-12-04", entra aquí sí o sí.
                        if (fechaStr == hoyTexto) {
                            containerHoy.addView(card)
                        }
                        // 2. ¿Es Esta Semana? (Mismo año, misma semana y NO es hoy)
                        else if (calHoy.get(Calendar.YEAR) == calEvento.get(Calendar.YEAR) &&
                            calHoy.get(Calendar.WEEK_OF_YEAR) == calEvento.get(Calendar.WEEK_OF_YEAR)) {
                            containerSemana.addView(card)
                        }
                        // 3. Todo lo demás (Futuro)
                        else {
                            containerOtras.addView(card)
                        }

                        val params = card.layoutParams as LinearLayout.LayoutParams
                        params.marginEnd = 24
                        card.layoutParams = params
                    }
                }
            },
            { error -> Toast.makeText(context, "Error cargando home", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun obtenerDireccion(lat: Double, lng: Double, textView: TextView) {
        Thread {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val direcciones = geocoder.getFromLocation(lat, lng, 1)
                if (direcciones != null && direcciones.isNotEmpty()) {
                    val dir = direcciones[0]
                    val texto = "${dir.thoroughfare ?: ""} ${dir.subThoroughfare ?: ""}, ${dir.subLocality ?: ""}".trim()
                    val textoFinal = if (texto.length > 3) texto else String.format("%.4f, %.4f", lat, lng)
                    activity?.runOnUiThread { textView.text = textoFinal }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread { textView.text = String.format("%.4f, %.4f", lat, lng) }
            }
        }.start()
    }
}