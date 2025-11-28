package com.ipn.escom.moviles.agenda

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment

class ConsultFragment : Fragment(R.layout.fragment_consult) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rgCriterio: RadioGroup = view.findViewById(R.id.rgCriterio)
        val spMes: Spinner = view.findViewById(R.id.spMes)
        val spTipo: Spinner = view.findViewById(R.id.spTipoEvento)
        val btnBuscar: Button = view.findViewById(R.id.btnBuscar)

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

        val tipos = listOf("Todos", "Cita", "Junta", "Entrega de proyecto", "Examen", "Otros")
        val adapterTipos = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tipos
        )
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipo.adapter = adapterTipos

        rgCriterio.check(R.id.rbFecha)

        btnBuscar.setOnClickListener {
            val criterioId = rgCriterio.checkedRadioButtonId
            val criterio = when (criterioId) {
                R.id.rbFecha -> "fecha"
                R.id.rbRango -> "rango"
                R.id.rbMes -> "mes"
                R.id.rbAnio -> "año"
                else -> "fecha"
            }

            val tipoSel = spTipo.selectedItem.toString()

            Toast.makeText(
                requireContext(),
                "Consulta demo por $criterio, tipo: $tipoSel",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
