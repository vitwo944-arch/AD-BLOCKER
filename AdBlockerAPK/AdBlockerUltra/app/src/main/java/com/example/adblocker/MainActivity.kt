package com.example.adblocker

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Screen() } }
    }

    private fun enableVpn() {
        val intent=VpnService.prepare(this)
        if(intent!=null) startActivityForResult(intent,100)
        else startService(Intent(this,AdBlockVpnService::class.java))
    }

    private fun disableVpn() {
        startService(Intent(this,AdBlockVpnService::class.java)
            .setAction(AdBlockVpnService.ACTION_STOP))
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==100 && resultCode==RESULT_OK)
            startService(Intent(this,AdBlockVpnService::class.java))
    }

    @Composable
    private fun Screen() {
        var enabled by remember { mutableStateOf(false) }
        val blocked by Stats.blocked.collectAsState()
        val queries by Stats.queries.collectAsState()

        Column(
            Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement=Arrangement.spacedBy(14.dp)
        ) {
            Text("🛡️ AdBlocker Ultra",
                style=MaterialTheme.typography.headlineMedium)
            Text("Bloqueur DNS local • sans root")

            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement=Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if(enabled) "PROTECTION ACTIVE" else "PROTECTION ARRÊTÉE",
                            style=MaterialTheme.typography.titleMedium)
                        Text(if(enabled) "VPN DNS en fonctionnement"
                             else "Appuie sur le bouton pour démarrer")
                    }
                    Switch(
                        checked=enabled,
                        onCheckedChange={
                            enabled=it
                            if(it) enableVpn() else disableVpn()
                        }
                    )
                }
            }

            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                StatCard("Requêtes",queries.toString(),Modifier.weight(1f))
                StatCard("Bloquées",blocked.toString(),Modifier.weight(1f))
            }

            Button(
                onClick={Stats::reset},
                modifier=Modifier.fillMaxWidth()
            ) { Text("Réinitialiser les statistiques") }

            Text("Blocklist intégrée (${BlockList.all().size} domaines)",
                style=MaterialTheme.typography.titleLarge)

            LazyColumn(
                modifier=Modifier.fillMaxWidth(),
                verticalArrangement=Arrangement.spacedBy(5.dp)
            ) {
                items(BlockList.all()) { Text("🚫 $it") }
            }
        }
    }

    @Composable
    private fun StatCard(title:String,value:String,modifier:Modifier) {
        Card(modifier) {
            Column(Modifier.padding(14.dp)) {
                Text(title)
                Text(value,style=MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
