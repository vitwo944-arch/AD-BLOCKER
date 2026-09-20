package com.example.adblocker

data class DnsQuestion(val id:Int, val host:String)

object DnsPacket {
    fun parseQuestion(b:ByteArray, n:Int):DnsQuestion? {
        if (n < 17) return null
        val id=u16(b,0)
        val flags=u16(b,2)
        val qd=u16(b,4)
        if ((flags and 0x8000)!=0 || qd < 1) return null

        var p=12
        val labels=ArrayList<String>()
        while(p<n) {
            val l=b[p].toInt() and 255
            p++
            if(l==0) break
            if(l>63 || p+l>n) return null
            labels.add(String(b,p,l,Charsets.US_ASCII))
            p+=l
        }
        if(labels.isEmpty() || p+4>n) return null
        return DnsQuestion(id,labels.joinToString("."))
    }

    fun response(original:ByteArray, n:Int, rcode:Int):ByteArray {
        var end=12
        while(end<n) {
            val l=original[end].toInt() and 255
            end++
            if(l==0) break
            if(l>63 || end+l>n) return ByteArray(0)
            end+=l
        }
        if(end+4>n) return ByteArray(0)
        val size=end+4
        val out=original.copyOf(size)
        val flags=0x8000 or (rcode and 15)
        out[2]=(flags ushr 8).toByte()
        out[3]=flags.toByte()
        out[4]=0;out[5]=1
        out[6]=0;out[7]=0;out[8]=0;out[9]=0;out[10]=0;out[11]=0
        return out
    }

    private fun u16(b:ByteArray,i:Int)=
        ((b[i].toInt() and 255) shl 8) or (b[i+1].toInt() and 255)
}
