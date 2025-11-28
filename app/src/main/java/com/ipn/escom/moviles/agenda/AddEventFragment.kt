package com.ipn.escom.moviles.agenda

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment

class AddEventFragment : Fragment(R.layout.fragment_add_event) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(requireContext(), "AddEventFragment cargado", Toast.LENGTH_SHORT).show()

        val spCategoria: Spinner = view.findViewById(R.id.spCategoria)
        val spStatus: Spinner = view.findViewById(R.id.spStatus)
        val etFecha: EditText = view.findViewById(R.id.etFecha)
        val etHora: EditText = view.findViewById(R.id.etHora)
        val etDescripcion: EditText = view.findViewById(R.id.etDescripcion)
        val etUbicacion: EditText = view.findViewById(R.id.etUbicacion)
        val etContacto: EditText = view.findViewById(R.id.etContacto)
        val btnGuardar: Button = view.findViewById(R.id.btnGuardar)

        val categorias = listOf(
            "Cita",
            "Junta",
            "Entrega de proyecto",
            "Examen",
            "Otros"
        )

        val adapterCategoria = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categorias
        )
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategoria.adapter = adapterCategoria
        spCategoria.setSelection(0)

        val estados = listOf(
            "Pendiente",
            "Realizado",
            "Aplazado"
        )

        val adapterStatus = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            estados
        )
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStatus.adapter = adapterStatus
        spStatus.setSelection(0)

        btnGuardar.setOnClickListener {
            val categoriaSel = spCategoria.selectedItem.toString()
            val fecha = etFecha.text.toString()
            val hora = etHora.text.toString()
            val desc = etDescripcion.text.toString()

            Toast.makeText(
                requireContext(),
                "Evento guardado (demo): $categoriaSel - $fecha $hora\n$desc",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
