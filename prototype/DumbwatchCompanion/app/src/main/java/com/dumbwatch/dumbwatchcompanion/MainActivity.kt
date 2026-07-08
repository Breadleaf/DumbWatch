package com.dumbwatch.dumbwatchcompanion

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.dumbwatch.dumbwatchcompanion.ui.theme.DumbwatchCompanionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class MainActivity : ComponentActivity() {

    lateinit var btAdapter: BluetoothAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

            DumbwatchCompanionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (
                        // if we have bluetooth scan permission
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                        ||
                        // if we have bluetooth connection permission
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Box() {
                            Text(
                                text = "Bluetooth Permission Not Granted (Click to Grant)",
                                modifier = Modifier.padding(innerPadding)
                                    .clickable(
                                        onClick = {
                                            requestPermissions(
                                                arrayOf(
                                                    Manifest.permission.BLUETOOTH_SCAN,
                                                    Manifest.permission.BLUETOOTH_CONNECT
                                                ),
                                                1
                                            )
                                        }
                                    )
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                        ) {
                            DeviceList(
                                devices = getPairedDevices(),
                                onSelect = { device -> selectedDevice = device },
                            )

                            selectedDevice?.let {
                                Text(
                                    text = "Selected Device: ${it.name}",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            // if there is a device, open the scrollback view
                            var consoleText by remember { mutableStateOf("") }
                            selectedDevice?.let { device ->
//                                val text = "The standard Lorem Ipsum passage, used since 1966\n" +
//                                        "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\"\n" +
//                                        "\n" +
//                                        "Section 1.10.32 of \"de Finibus Bonorum et Malorum\", written by Cicero in 45 BC\n" +
//                                        "\"Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?\"\n" +
//                                        "\n" +
//                                        "1914 translation by H. Rackham\n" +
//                                        "\"But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness. No one rejects, dislikes, or avoids pleasure itself, because it is pleasure, but because those who do not know how to pursue pleasure rationally encounter consequences that are extremely painful. Nor again is there anyone who loves or pursues or desires to obtain pain of itself, because it is pain, but because occasionally circumstances occur in which toil and pain can procure him some great pleasure. To take a trivial example, which of us ever undertakes laborious physical exercise, except to obtain some advantage from it? But who has any right to find fault with a man who chooses to enjoy a pleasure that has no annoying consequences, or one who avoids a pain that produces no resultant pleasure?\"\n" +
//                                        "\n" +
//                                        "Section 1.10.33 of \"de Finibus Bonorum et Malorum\", written by Cicero in 45 BC\n" +
//                                        "\"At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.\"\n" +
//                                        "\n" +
//                                        "1914 translation by H. Rackham\n" +
//                                        "\"On the other hand, we denounce with righteous indignation and dislike men who are so beguiled and demoralized by the charms of pleasure of the moment, so blinded by desire, that they cannot foresee the pain and trouble that are bound to ensue; and equal blame belongs to those who fail in their duty through weakness of will, which is the same as saying through shrinking from toil and pain. These cases are perfectly simple and easy to distinguish. In a free hour, when our power of choice is untrammelled and when nothing prevents our being able to do what we like best, every pleasure is to be welcomed and every pain avoided. But in certain circumstances and owing to the claims of duty or the obligations of business it will frequently occur that pleasures have to be repudiated and annoyances accepted. The wise man therefore always holds in these matters to this principle of selection: he rejects pleasures to secure other greater pleasures, or else he endures pains to avoid worse pains.\""

                                val scrollState = rememberScrollState()
                                val coroutineScope = rememberCoroutineScope()

                                DisposableEffect(device) {
                                    var connectedSocket: BluetoothSocket? = null
                                    var isReading = true

                                    val job = coroutineScope.launch {
                                        try {
                                            connectedSocket = withContext(Dispatchers.IO) {
                                                val uuid: UUID =
                                                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                                                try {
                                                    val socket =
                                                        device.createRfcommSocketToServiceRecord(
                                                            uuid
                                                        )
                                                    socket.connect()
                                                    socket
                                                } catch (e: IOException) {
                                                    consoleText += "Connection failed: ${e.message}\n"
                                                    null
                                                }
                                            }

                                            connectedSocket?.let { socket ->
                                                withContext(Dispatchers.IO) {
                                                    try {
                                                        val inputStream = socket.inputStream
                                                        val outputStream = socket.outputStream

                                                        val buffer = ByteArray(1024)
                                                        val lineBuffer = StringBuilder()
                                                        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                                                        consoleText += "Connected\n"

                                                        while (isReading && socket.isConnected && isActive) {
                                                            try {
                                                                val bytes = inputStream.read(buffer)
                                                                if (bytes > 0) {
                                                                    val received =
                                                                        String(buffer, 0, bytes)
                                                                    lineBuffer.append(received)
                                                                    var lastNewline = lineBuffer.lastIndexOf("\n")
                                                                    while (lastNewline != -1) {
                                                                        val line = lineBuffer.substring(0, lastNewline + 1)
                                                                        lineBuffer.delete(0, lastNewline + 1)

                                                                        val currentTime = LocalTime.now().format(formatter)

                                                                        withContext(Dispatchers.Main) {
                                                                            consoleText += "$currentTime $line"

                                                                        }

                                                                        if (line.trim() == "GET_TIME") {
                                                                            outputStream.write((currentTime + "\n").toByteArray())
                                                                            outputStream.flush()
                                                                        }

                                                                        lastNewline = lineBuffer.lastIndexOf("\n")
                                                                    }


                                                                }
                                                            } catch (e: IOException) {
                                                                break
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        consoleText += "Read error: ${e.message}\n"
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            consoleText += "Error: ${e.message}\n"
                                        }
                                    }

                                    onDispose {
                                        isReading = false
                                        connectedSocket?.close()
                                        job.cancel()
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .verticalScroll(scrollState)
                                ) {
                                    Text(
                                        text = consoleText,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getPairedDevices(): Set<BluetoothDevice> {
        btAdapter = getSystemService(BluetoothManager::class.java).adapter
        return btAdapter.bondedDevices
    }
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceList(
    modifier: Modifier = Modifier,
    devices: Set<BluetoothDevice>,
    onSelect: (BluetoothDevice) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
    ) {
        Text(
            text = "Select a device",
            modifier = Modifier
                .padding(16.dp)
                .clickable( onClick = { expanded = true } )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            devices.forEach { device ->
                DropdownMenuItem(
                    text = { Text(text = device.name) },
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        expanded = false
                        onSelect(device)
                    }
                )
            }
        }
    }
}