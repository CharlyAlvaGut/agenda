package com.ipn.escom.moviles.agenda

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class RestoreFragment : Fragment(R.layout.fragment_restore) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardDrive = view.findViewById<MaterialCardView>(R.id.cardRestoreDrive)
        val cardDropbox = view.findViewById<MaterialCardView>(R.id.cardRestoreDropbox)

        cardDrive.setOnClickListener {
            Toast.makeText(requireContext(), "Buscando respaldos en Drive...", Toast.LENGTH_SHORT).show()
        }

        cardDropbox.setOnClickListener {
            Toast.makeText(requireContext(), "Buscando respaldos en Dropbox...", Toast.LENGTH_SHORT).show()
        }
    }
}