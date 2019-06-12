package com.example.paymentapplication.infrastructure

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val background: CoroutineDispatcher

}
