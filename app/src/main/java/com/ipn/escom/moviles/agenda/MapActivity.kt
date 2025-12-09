package com.ipn.escom.moviles.agenda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private var ubicacionSeleccionada: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. IMPORTANTE: Configurar osmdroid antes de inflar el layout
        // Esto carga la configuración de caché para que el mapa se vea bien
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        // Truco para evitar bloqueo de user-agent (necesario en versiones nuevas)
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map)

        // 2. Inicializar Mapa
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK) // Estilo de mapa estándar (gratis)
        map.setMultiTouchControls(true) // Permitir zoom con dos dedos

        // 3. Centrar mapa (Ej: CDMX - ESCOM)
        val mapController = map.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(19.5045, -99.1469)
        mapController.setCenter(startPoint)

        // 4. Detectar toques en el mapa
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    colocarMarcador(it)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // Opcional: También poner marcador con clic largo
                p?.let { colocarMarcador(it) }
                return true
            }
        }
        val overlayEvents = MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(overlayEvents)

        // 5. Botón confirmar
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarUbicacion)
        btnConfirmar.setOnClickListener {
            if (ubicacionSeleccionada != null) {
                confirmarSeleccion()
            } else {
                Toast.makeText(this, "Toca el mapa para seleccionar un lugar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun colocarMarcador(punto: GeoPoint) {
        ubicacionSeleccionada = punto

        // Limpiar marcadores viejos (los overlays > 1 suelen ser marcadores, el 0 es el detector de eventos)
        if (map.overlays.size > 1) {
            map.overlays.removeAt(1)
        }

        val marcador = Marker(map)
        marcador.position = punto
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title = "Ubicación seleccionada"
        map.overlays.add(marcador)
        map.invalidate() // Refrescar mapa
    }

    private fun confirmarSeleccion() {
        val lat = ubicacionSeleccionada!!.latitude
        val lng = ubicacionSeleccionada!!.longitude

        // Formato simple de coordenadas
        val textoUbicacion = String.format("%.5f, %.5f", lat, lng)

        val intent = Intent()
        intent.putExtra("ubicacion", textoUbicacion)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}