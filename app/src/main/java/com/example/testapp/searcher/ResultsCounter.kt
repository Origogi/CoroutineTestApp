package com.example.testapp.searcher

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor


object ResultsCounter {
    private val context = newSingleThreadContext("counter")
    private var counter = 0
    private val notifications = Channel<Int>(Channel.CONFLATED)

    private val actorCounter = CoroutineScope(context).actor<Void?> {
        for (msg in channel) {
            counter++
        }
        notifications.send(counter)
    }

    suspend fun increment() {
        actorCounter.send(null)
    }

    fun getNotification() : ReceiveChannel<Int> = notifications

}

