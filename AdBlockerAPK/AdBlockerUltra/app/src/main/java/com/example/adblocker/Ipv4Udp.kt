package com.example.adblocker

data class IpUdp(
    val srcIp:ByteArray,
    val dstIp:ByteArray,
    val srcPort:Int,
    val dstPort:Int,
    val payloadOffset:Int,
    val original:ByteArray
)

object Ipv4Udp {
    fun parse(b:ByteArray,n:Int):IpUdp? {
        if(n<28 || (b[0].toInt() ushr 4)!=4) return null
        val ihl=(b[0].toInt() and 15)*4
        if(ihl<20 || n<ihl+8 || (b[9].toInt() and 255)!=17) return null
        return IpUdp(
            b.copyOfRange(12,16),b.copyOfRange(16,20),
            u16(b,ihl),u16(b,ihl+2),ihl+8,b.copyOf(n)
        )
    }

    fun reply(p:IpUdp,dns:ByteArray):ByteArray {
        val total=28+dns.size
        val o=ByteArray(total)
        o[0]=0x45
        o[2]=(total ushr 8).toByte();o[3]=total.toByte()
        o[8]=64;o[9]=17
        System.arraycopy(p.dstIp,0,o,12,4)
        System.arraycopy(p.srcIp,0,o,16,4)
        put16(o,20,p.dstPort);put16(o,22,p.srcPort)
        put16(o,24,8+dns.size);put16(o,26,0)
        System.arraycopy(dns,0,o,28,dns.size)
        put16(o,10,checksum(o,0,20))
        return o
    }

    private fun u16(b:ByteArray,i:Int)=
        ((b[i].toInt() and 255) shl 8) or (b[i+1].toInt() and 255)

    private fun put16(b:ByteArray,i:Int,v:Int) {
        b[i]=(v ushr 8).toByte(); b[i+1]=v.toByte()
    }

    private fun checksum(b:ByteArray,off:Int,len:Int):Int {
        var sum=0L;var i=off
        while(i<off+len) {
            sum += (((b[i].toInt() and 255) shl 8) or
                    (if(i+1<off+len)b[i+1].toInt() and 255 else 0))
            sum=(sum and 65535)+(sum ushr 16);i+=2
        }
        sum=(sum and 65535)+(sum ushr 16)
        return sum.inv().toInt() and 65535
    }
}
