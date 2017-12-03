package kr.co.lemming.arduinoautocurtain

import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.graphics.Color
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.widget.SeekBar
import android.widget.Switch
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    val writeBuffer: ArrayList<Byte> = ArrayList<Byte>()

    val paredDeivceArr: ArrayList<String> = ArrayList()
    val paredDeviceMacArr: ArrayList<String> = ArrayList()
    val BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var connectedThread: ClientThread? = null

    fun sendFunction(byte: Byte) {
        if (connectedThread != null)
            writeBuffer.add(byte)
        else
            Toast.makeText(this, "Not connected.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btConnectNewDevice.setOnClickListener { view ->

            val btDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
            val arr: ArrayList<String> = ArrayList()

            val builder: AlertDialog.Builder = AlertDialog.Builder(this);
            builder.setTitle("새로운 아두이노 기기 선택")

            for (device in btDefaultAdapter.bondedDevices) {
                arr.add(device.name)
                paredDeviceMacArr.add(device.address)
            }

            builder.setItems(arr.toArray(arrayOfNulls<String>(arr.size)) as Array<String>, DialogInterface.OnClickListener { dialogInterface, i ->
                //                Toast.makeText(this, arr[i], Toast.LENGTH_SHORT).show()
                val device = btDefaultAdapter.getRemoteDevice(paredDeviceMacArr[i])
                // 클라이언트 소켓 스레드 생성 & 시작
                if (connectedThread != null) {
                    connectedThread!!.cancel()
                }
                try {
                    connectedThread = ClientThread(device)
                    connectedThread!!.start()
                } catch (e: Exception) {
                    Toast.makeText(this, "연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
            val dialog = builder.create()
            dialog.show()
        }

        btn_init.setOnClickListener { view ->
            sendFunction(6)
        }
//        btn_up.setOnTouchListener { view, motionEvent ->
//            when(motionEvent.action){
//                MotionEvent.ACTION_DOWN -> writeBuffer(4)
//            }
//        }
        btn_up.setOnClickListener { view ->
            sendFunction(4)
        }

        btn_down.setOnClickListener { view ->
            sendFunction(5)
        }

        switchSoundMode.setOnCheckedChangeListener { compoundButton, b ->
            sendFunction(if (b) 3 else 2)
        }
        sliderSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sendFunction((seekBar.progress + 50).toByte())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

        })
        switchAutoMode.setOnCheckedChangeListener { compoundButton, b ->
            sendFunction(if (b) 0 else 1)
        }
        btn_stop.setOnClickListener { view ->
            sendFunction(7)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class ClientThread // 원격 디바이스와 접속을 위한 클라이언트 소켓 생성

    (device: BluetoothDevice) : Thread() {
        private var clientSocket: BluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(BT_UUID);
        private lateinit var inputStream: InputStream
        private lateinit var outputStream: OutputStream

        fun notifyUI(isConnected: Boolean) {
            runOnUiThread {
                if (isConnected) {
                    txtConnected.setTextColor(Color.GREEN)
                    txtConnected.text = "Connected."
                } else {
                    Toast.makeText(this@MainActivity, "Connection refused", Toast.LENGTH_SHORT).show()
                    connectedThread = null
                    txtConnected.setTextColor(Color.RED)
                    txtConnected.text = "Not connected."
                }
            }
        }

        override fun run() {
            try {
                clientSocket.connect()
            } catch (e: IOException) {
                e.printStackTrace()
                try {
                    clientSocket.close()
                } catch (e2: IOException) {
                    e2.printStackTrace()
                }
                notifyUI(false)
                return
            }
            notifyUI(true)

            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();

            // 60~200
            while (clientSocket.isConnected) {
                try {
                    if (inputStream.available() > 0) {
                        val buffer = ByteArray(1024)
                        val bytes = inputStream.read(buffer)
                        val strBuf = String(buffer, 0, bytes)
                        // StringBuffer
//                    showMessage("Receive: " + strBuf)
//                    SystemClock.sleep(1)
                    }
                } catch (e: Exception) {
                    notifyUI(false)
                    break
                }

                try {
                    if (writeBuffer.size > 0) {
                        outputStream.write((writeBuffer[0].toInt()))
                        outputStream.flush()
                        
                        Log.d("Writing", writeBuffer[0].toString())
                        writeBuffer.removeAt(0)
                    }
                } catch (e: Exception) {
                    break

                }

                sleep(100);
            }
            try {
                inputStream.close()
                outputStream.close()
            } catch (e: Exception) {

            }
        }

        // 클라이언트 소켓 중지
        fun cancel() {
            try {
                clientSocket.close()
            } catch (e: IOException) {
                // showMessage("Client Socket close error")
            }

        }
    }

}
