package com.example.adblocker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object Stats {
    private val _blocked = MutableStateFlow(0L)
    private val _queries = MutableStateFlow(0L)
    val blocked: StateFlow<Long> = _blocked
    val queries: StateFlow<Long> = _queries

    fun query() { _queries.value++ }
    fun blocked() { _blocked.value++ }
    fun reset() { _blocked.value = 0; _queries.value = 0 }
}
