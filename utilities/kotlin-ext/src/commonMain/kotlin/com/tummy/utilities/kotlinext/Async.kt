package com.tummy.utilities.kotlinext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface Async<out T> {
    data object Loading : Async<Nothing>
    data class Ready<T>(val data: T) : Async<T>
}

fun <T> Flow<Async<T>>.filterReadyValues(): Flow<T> {
    return this
        .filterIsInstance<Async.Ready<T>>()
        .map { it.data }
}

fun <T> Flow<T>.toAsyncFlow(): Flow<Async<T>> {
    return this
        .map { Async.Ready(data = it) as Async<T> }
        .onStart { emit(Async.Loading) }
}

fun <T> Async<T>.valueOr(default: () -> T): T {
    return when (this) {
        is Async.Loading -> default()
        is Async.Ready -> data
    }
}

fun <T> Async<T>.valueOr(default: T): T {
    return when (this) {
        is Async.Loading -> default
        is Async.Ready -> data
    }
}

fun <T> Async<T>.valueOrNull(): T? {
    return when (this) {
        is Async.Loading -> null
        is Async.Ready -> data
    }
}

fun <T, R> Async<T>.map(transform: (T) -> R): Async<R> {
    return when (this) {
        is Async.Loading -> Async.Loading
        is Async.Ready -> Async.Ready(transform(data))
    }
}
