package com.example.adblocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class AdBlockVpnService : VpnService() {
    private var tun:ParcelFileDescriptor?=null
    private val running=AtomicBoolean(false)
    private var thread:Thread?=null

    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        if(intent?.action==ACTION_STOP) stopVpn() else startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if(running.get()) return
        createChannel()
        startForeground(NOTIFICATION_ID, notification())

        // Only the virtual DNS subnet is routed into the VPN. Normal Internet
        // traffic therefore keeps working; DNS is handled by this service.
        tun=Builder()
            .setSession("AdBlocker Ultra")
            .addAddress("10.99.0.2",32)
            .addRoute("10.99.0.0",24)
            .addDnsServer("10.99.0.1")
            .setBlocking(true)
            .establish() ?: return

        running.set(true)
        thread=Thread { loop(tun!!) }.also { it.start() }
    }

    private fun loop(fd:ParcelFileDescriptor) {
        val input=FileInputStream(fd.fileDescriptor)
        val output=FileOutputStream(fd.fileDescriptor)
        val buf=ByteArray(32767)

        try {
            while(running.get()) {
                val n=input.read(buf)
                if(n<=0) continue
                val p=Ipv4Udp.parse(buf,n) ?: continue
                if(p.dstPort!=53) continue

                val dns=buf.copyOfRange(p.payloadOffset,n)
                val q=DnsPacket.parseQuestion(dns,dns.size) ?: continue
                Stats.query()

                if(BlockList.isBlocked(q.host)) {
                    val blocked=DnsPacket.response(dns,dns.size,3)
                    output.write(Ipv4Udp.reply(p,blocked))
                    Stats.blocked()
                } else {
                    val answer=forwardDns(dns)
                    if(answer!=null) output.write(Ipv4Udp.reply(p,answer))
                    else {
                        val fail=DnsPacket.response(dns,dns.size,2)
                        output.write(Ipv4Udp.reply(p,fail))
                    }
                }
            }
        } catch(_:Exception) {}
    }

    private fun forwardDns(query:ByteArray):ByteArray? {
        return try {
            DatagramSocket().use { socket ->
                if(!protect(socket)) return null
                socket.soTimeout=2500
                val server=InetAddress.getByName("1.1.1.1")
                socket.send(DatagramPacket(query,query.size,server,53))
                val b=ByteArray(4096)
                val r=DatagramPacket(b,b.size)
                socket.receive(r)
                b.copyOf(r.length)
            }
        } catch(_:Exception) { null }
    }

    private fun createChannel() {
        if(Build.VERSION.SDK_INT>=26) {
            val nm=getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL,"AdBlocker",NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun notification():Notification {
        val b=if(Build.VERSION.SDK_INT>=26)
            Notification.Builder(this,CHANNEL) else Notification.Builder(this)
        return b.setContentTitle("AdBlocker Ultra")
            .setContentText("Protection DNS active")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setOngoing(true)
            .build()
    }

    private fun stopVpn() {
        running.set(false)
        try{tun?.close()}catch(_:Exception){}
        tun=null
        thread?.interrupt()
        thread=null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy(){stopVpn();super.onDestroy()}
    override fun onRevoke(){stopVpn();super.onRevoke()}

    companion object {
        const val ACTION_STOP="com.example.adblocker.STOP"
        private const val CHANNEL="adblocker"
        private const val NOTIFICATION_ID=41
    }
}
