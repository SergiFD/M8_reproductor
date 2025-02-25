package com.example.m8_reproductor_musica

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    //crear el adaptador personalizado
    private lateinit var adapter: CustomAdapter
    private var currentSongIndex = 0 // Índice de la canción actual
    private lateinit var listView: ListView
    private var mediaPlayer: MediaPlayer? = null
    private val audioList = mutableListOf<String>()
    private val audioPaths = mutableListOf<String>()
    private lateinit var seekBar : SeekBar
    private val handler = Handler(Looper.getMainLooper())

    //botones
    private lateinit var ib_Play_Puase : ImageButton
    private lateinit var ib_Stop : ImageButton
    private lateinit var ib_loop : ImageButton
    private lateinit var ib_Next : ImageButton
    private lateinit var ib_Back : ImageButton

    //los tiempos de las canciones
    private lateinit var time_start : TextView
    private lateinit var time_out : TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.lV_audio)
        seekBar = findViewById(R.id.seekBar)

        // Obtener los ImagenButton
        ib_Play_Puase = findViewById(R.id.ib_Play)
        ib_Stop = findViewById(R.id.ib_Stop)
        ib_loop = findViewById(R.id.ib_Loop)
        ib_Next = findViewById(R.id.ib_Next)
        ib_Back = findViewById(R.id.ib_Back)

        //optener los tiempos de la cancion
        time_start = findViewById(R.id.tv_time_start)
        time_out = findViewById(R.id.tv_time_out)

        checkPermissions()

        ib_Play_Puase.setOnClickListener {

            // Cambiar la imagen cuando se haga clic
            play_pause()
            toggleImage()
            setButtonOpacity(ib_Play_Puase)
        }

        ib_Stop.setOnClickListener()
        {
            stop_song()
            setButtonOpacity(ib_Stop)
        }

        ib_loop.setOnClickListener(){
            loop_song()
            toggleBackground()
            setButtonOpacity(ib_loop)
        }
        ib_Next.setOnClickListener(){
            nextSound()
            setButtonOpacity(ib_Next)

        }
        ib_Back.setOnClickListener(){
            backSound()
            setButtonOpacity(ib_Back)
        }
    }
    private fun toggleImage() {
        val playDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher_foreground_play)
        val pauseDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher_foreground_pause)

            if (mediaPlayer?.isPlaying == true)
            {
                ib_Play_Puase.setImageDrawable(pauseDrawable)
        } else {
            // Cambiar a play
            ib_Play_Puase.setImageDrawable(playDrawable)
        }
    }

    private fun toggleBackground() {
        // Obtener el color actual
        val currentColor = (ib_loop.background as ColorDrawable).color

        // Cambiar el color basado en el color actual
        if (currentColor == ContextCompat.getColor(this, R.color.transparente)) {
            ib_loop.setBackgroundColor(ContextCompat.getColor(this, R.color.semi_transparente)) // Cambia a otro color
        } else {
            ib_loop.setBackgroundColor(ContextCompat.getColor(this, R.color.transparente)) // Cambia al color personalizado
        }

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
            adapter = CustomAdapter(this, audioList)
            listView.adapter = adapter
            listView.setOnItemClickListener { _, _, position, _ ->
                playAudio(audioPaths[position])
                currentSongIndex = position
                adapter.setSelectedPosition(position) // Cambia la posición seleccionada
            }


    }
    private fun playAudio(path: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            setOnPreparedListener { mp ->
                mp.start()
                mediaPlayer?.let {
                seekBar.max = it.duration
                seekBar.progress = it.currentPosition
                updateDurationText(it.duration)
            }
                toggleImage()
            // Actualizar el SeekBar mientras el audio se reproduce
            updateSeekBar()

            // Listener para cambiar la posición del audio cuando el usuario mueva el SeekBar
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            }
            setOnErrorListener { mp, what, extra ->
                // Manejar errores de reproducción
                true
            }
        }
    }
private fun play_pause(){
    if (mediaPlayer?.isPlaying == true) {
        mediaPlayer?.pause()
    }else
    {
        mediaPlayer?.start()
        updateSeekBar()
    }
}
    private fun stop_song(){
        if (mediaPlayer?.isPlaying == true){
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
            toggleImage()
            updateSeekBar()
        }
    }
    private fun loop_song(){
        mediaPlayer?.isLooping = !(mediaPlayer?.isLooping == true) // Cambia el estado de bucle
    }

    private fun updateSeekBar() {
        // Eliminar cualquier callback anterior que pueda estar corriendo
        handler.removeCallbacksAndMessages(null)
        val runnable = object : Runnable {
            override fun run() {
                // Verificar si el audio ya terminó
                if (mediaPlayer?.isPlaying == true) {
                    seekBar.progress = mediaPlayer?.currentPosition ?: 0
                    handler.postDelayed(this, 1000) // Actualizar cada segundo
                    updateStartTime(mediaPlayer?.currentPosition ?: 0) // Actualiza el tiempo de inicio
                }
            }
            private fun updateStartTime(currentPosition: Int) {
                val minutes = (currentPosition / 1000) / 60
                val seconds = (currentPosition / 1000) % 60
                time_start.text = String.format("%02d:%02d", minutes, seconds) // Actualiza el TextView
            }
        }
        handler.postDelayed(runnable, 0)
    }


    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissos")
        builder.setMessage("Es necessita permisos de lectura per accedir a les cançons.")

        builder.setPositiveButton("Anar") { dialog, _ ->
               //lo llevamos a la configuración
                openAppSettings()
        }

        builder.setNegativeButton("Tancar aplicació") { _, _ ->
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
        handler.removeCallbacksAndMessages(null)
    }
    private fun setButtonOpacity(button: ImageButton) {
        button.alpha = 0.5f // Cambiar la opacidad a 0.5
        button.postDelayed({ button.alpha = 1.0f }, 50) // Restaurar la opacidad después de milesimas (un segundo es mucho) segundo
    }

    class CustomAdapter(context: Context, private val audioList: List<String>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, audioList) {
        private var selectedPosition = -1 // Para mantener la posición seleccionada

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            if (position == selectedPosition) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.negrito_selecionado)) // Color para el elemento seleccionado
            } else {
                view.setBackgroundColor(Color.TRANSPARENT) // Color por defecto
            }
            return view
        }

        fun setSelectedPosition(position: Int) {
            selectedPosition = position
            notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
        }
    }

    private fun nextSound() {
        if (currentSongIndex < audioPaths.size - 1) {
            currentSongIndex++
        } else {
            currentSongIndex = 0 // Volver al inicio si es la última canción
        }
        changePosition()
    }

    private fun changePosition(){
        playAudio(audioPaths[currentSongIndex])
        adapter.setSelectedPosition(currentSongIndex) // Cambia la posición seleccionada
    }

    private fun backSound() {
        if (currentSongIndex > 0) {
            currentSongIndex--
        } else {
            currentSongIndex = audioPaths.size - 1 // Volver a la última canción si es la primera
        }
        changePosition()
    }
    private fun updateDurationText(duration: Int) {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        time_out.text = String.format("%02d:%02d", minutes, seconds) // Actualiza el TextView
    }


    //09122C negrito
    //#872341 granate
    //#BE3144 rojo
    //#E17564 naranja

}




