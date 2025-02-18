package com.example.m8_reproductor_musica

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var mediaPlayer: MediaPlayer? = null
    private val audioList = mutableListOf<String>()
    private val audioPaths = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.lV_audio)
        checkPermissions()
    }


    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadAudioFiles()
        } else {
            showPermissionDialog()
        }
    }


    private fun loadAudioFiles() {
        val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA)
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        cursor?.use {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (it.moveToNext()) {
                val name = it.getString(nameColumn)
                val path = it.getString(pathColumn)
                audioList.add(name)
                audioPaths.add(path)
            }
        }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, audioList)
        listView.setOnItemClickListener { _, _, position, _ ->
            playAudio(audioPaths[position])
        }
    }

    private fun playAudio(path: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
    }
    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissos")
        builder.setMessage("Es necessita permisos de lectura per accedir a les cançons.")

        builder.setPositiveButton("Anar") { dialog, _ ->
               //lo llevamos a la configuración
                openAppSettings()
                checkPermissions()
        }

        builder.setNegativeButton("Pantalla d'inici") { _, _ ->
            finish() // Cierra la aplicación
        }

        builder.setCancelable(false) // Evita que el usuario lo cierre tocando fuera
        builder.show()
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}


    //#09122C negrito
    //#872341 granate
    //#BE3144 rojo
    //#E17564 naranja

