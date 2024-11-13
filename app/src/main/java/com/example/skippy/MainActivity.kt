package com.example.skippy

import android.app.Activity
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.skippy.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val appUuid: UUID = UUID.fromString("e8e10f95-1a70-4b27-9ccf-02010264e9c8") // UUID único para o app
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Abre o seletor de arquivos ao iniciar a Activity
        selectGifFromStorage()

        // Configura o clique no GIF para abrir uma nova Activity
        binding.gifImageView.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

        // Verifica permissões e inicia o servidor Bluetooth se permitido
        //checkBluetoothPermissionsAndStartServer()
    }

    // Cria um launcher para obter o resultado da seleção de arquivo
    private val gifPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            displayGif(it)
        }
    }

    // Função para abrir o seletor de arquivos
    private fun selectGifFromStorage() {
        gifPickerLauncher.launch("image/gif")
    }

    // Exibe o GIF selecionado no ImageView
    private fun displayGif(uri: Uri) {
        Glide.with(this)
            .asGif()
            .load(uri)
            .into(binding.gifImageView)
    }

    //FUNCIONAMENTO BLUETOOTH
    private fun checkBluetoothPermissionsAndStartServer() {
        // Verifica se as permissões Bluetooth são concedidas
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicita permissões se não foram concedidas
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        } else {
            // Permissões concedidas, inicia o servidor Bluetooth
            startBluetoothServer()
        }
    }
    // Lida com a resposta do usuário para o pedido de permissão
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Se todas as permissões foram concedidas, inicia o servidor Bluetooth
                startBluetoothServer()
            } else {
                Log.e("Bluetooth", "Permissões de Bluetooth não foram concedidas")
            }
        }
    }

    private fun startBluetoothServer() {
        // Verifica permissões novamente antes de abrir o servidor Bluetooth
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Bluetooth", "Permissão BLUETOOTH_CONNECT necessária para iniciar o servidor")
            return
        }

        val serverSocket: BluetoothServerSocket? = try {
            bluetoothAdapter?.listenUsingRfcommWithServiceRecord("GIFTransfer", appUuid)
        } catch (e: IOException) {
            Log.e("Bluetooth", "Erro ao iniciar servidor Bluetooth", e)
            null
        }

        Thread {
            var socket: BluetoothSocket? = null
            while (socket == null) {
                try {
                    socket = serverSocket?.accept() // Aguarda conexão
                } catch (e: IOException) {
                    Log.e("Bluetooth", "Erro ao aceitar conexão", e)
                }
            }

            socket?.let {
                handleBluetoothConnection(it)
                try {
                    serverSocket?.close()
                } catch (e: IOException) {
                    Log.e("Bluetooth", "Erro ao fechar serverSocket", e)
                }
            }
        }.start()
    }

    private fun handleBluetoothConnection(socket: BluetoothSocket) {
        val inputStream = socket.inputStream
        val file = File(filesDir, "received_gif.gif")

        // Salva o arquivo recebido
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        //Atualiza o ImageView com o novo GIF
        runOnUiThread {
            Glide.with(this)
                .asGif()
                .load(file) //Recebe o Gif por bluetooth
                .into(binding.gifImageView)
        }

        // Configurar clique no GIF para abrir uma nova Activity
        binding.gifImageView.setOnClickListener {
            // Iniciar a SecondActivity
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

//    // Declaração do binding
//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Inicialização do binding
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Carregue o GIF usando Glide no ImageView do binding
//        Glide.with(this)
//            .asGif()
//            .load("https://media1.tenor.com/m/0FefgWaVTG4AAAAd/fire.gif") // Substitua pelo URL do seu GIF ou caminho local
//            .into(binding.gifImageView)
//    }
}


