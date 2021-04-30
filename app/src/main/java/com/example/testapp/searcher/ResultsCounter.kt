package com.example.testapp.searcher

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor

enum class Action {
    RESET, INCREMENT
}

object ResultsCounter {
    private val context = newSingleThreadContext("counter")
    private var counter = 0
    private val notifications = Channel<Int>(Channel.CONFLATED)

    private val actorCounter = CoroutineScope(context).actor<Action> {
        for (msg in channel) {
            when (msg) {
                Action.INCREMENT -> counter++
                Action.RESET -> counter =0
            }
            notifications.send(counter)
        }
    }

    suspend fun increment() {
        actorCounter.send(Action.INCREMENT)
    }

    suspend fun reset() {
        actorCounter.send(Action.RESET)

    }

    fun getNotification() : ReceiveChannel<Int> = notifications

}

