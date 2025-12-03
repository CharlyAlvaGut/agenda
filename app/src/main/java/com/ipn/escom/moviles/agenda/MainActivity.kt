package com.ipn.escom.moviles.agenda

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        bottomNav = findViewById(R.id.bottomNav)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        replaceFragment(HomeFragment())

        // Manejo de clics del menú lateral
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> replaceFragment(AddEventFragment())
                R.id.nav_consult -> replaceFragment(ConsultFragment())
                R.id.nav_backup -> replaceFragment(BackupFragment())
                R.id.nav_restore -> replaceFragment(RestoreFragment())
                R.id.nav_about -> replaceFragment(AboutFragment())
                R.id.nav_exit -> confirmarSalida()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Manejo del menú inferior
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> replaceFragment(HomeFragment())
                R.id.bottom_consult -> replaceFragment(ConsultFragment())
                R.id.bottom_exit -> confirmarSalida()
            }
            true
        }
    }

    // Función para cambiar de fragments
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    //Funcion para mostrar un modal de confirmación al salir
    private fun confirmarSalida() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Salir")
        builder.setMessage("¿Estás seguro de que deseas salir de la aplicación?")
        builder.setPositiveButton("Sí") { _, _ ->
            finish() // Aquí sí cerramos la app
        }
        builder.setNegativeButton("No", null) // Simplemente cierra el diálogo
        builder.show()
    }
}

