package com.example.adblocker

object BlockList {
    // Multiple categories. This is a starter list; users can extend it.
    private val domains = setOf(
        "doubleclick.net","googlesyndication.com","googleadservices.com",
        "adservice.google.com","adnxs.com","adsrvr.org","criteo.com",
        "taboola.com","outbrain.com","scorecardresearch.com",
        "amazon-adsystem.com","ads.yahoo.com","app-measurement.com",
        "advertising.com","adform.net","rubiconproject.com",
        "pubmatic.com","openx.net","smartadserver.com","casalemedia.com",
        "33across.com","contextweb.com","lijit.com","sharethrough.com",
        "zedo.com","moatads.com","quantserve.com","demdex.net",
        "turn.com","bluekai.com","krxd.net","rlcdn.com"
    )

    fun isBlocked(hostname: String): Boolean {
        val h = hostname.trimEnd('.').lowercase()
        if (h.isEmpty()) return false
        return domains.any { h == it || h.endsWith(".$it") }
    }

    fun all(): List<String> = domains.sorted()
}
