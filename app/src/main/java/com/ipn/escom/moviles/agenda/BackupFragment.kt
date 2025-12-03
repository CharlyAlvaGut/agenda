package com.ipn.escom.moviles.agenda

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class BackupFragment : Fragment(R.layout.fragment_backup) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardDrive = view.findViewById<MaterialCardView>(R.id.cardBackupDrive)
        val cardDropbox = view.findViewById<MaterialCardView>(R.id.cardBackupDropbox)

        cardDrive.setOnClickListener {
            Toast.makeText(requireContext(), "Iniciando respaldo en Google Drive...", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica de conexión con la API de Drive
        }

        cardDropbox.setOnClickListener {
            Toast.makeText(requireContext(), "Iniciando respaldo en Dropbox...", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica de conexión con la API de Dropbox
        }
    }
}