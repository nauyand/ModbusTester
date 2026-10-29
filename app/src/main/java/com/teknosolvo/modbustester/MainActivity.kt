package com.teknosolvo.modbustester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teknosolvo.modbustester.ui.theme.ModbusTesterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModbusTesterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConnectionScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ConnectionScreen(modifier: Modifier = Modifier) {
    var host by rememberSaveable { mutableStateOf("192.168.1.100") }
    var port by rememberSaveable { mutableStateOf("502") }
    var unitId by rememberSaveable { mutableStateOf("1") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Modbus TCP", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host / IP") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = port,
            onValueChange = { port = it.filter(Char::isDigit) },
            label = { Text("Port") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = unitId,
            onValueChange = { unitId = it.filter(Char::isDigit) },
            label = { Text("Unit ID") },
            modifier = Modifier.fillMaxWidth()
        )
        // TODO: hook up Modbus client
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("Connect")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionScreenPreview() {
    ModbusTesterTheme {
        ConnectionScreen()
    }
}
