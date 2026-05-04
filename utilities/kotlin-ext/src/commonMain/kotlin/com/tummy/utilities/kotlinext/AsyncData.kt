package com.tummy.utilities.kotlinext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface AsyncData<out T> {
    data object Loading : AsyncData<Nothing>
    data class Ready<T>(val data: T) : AsyncData<T>
}

fun <T> Flow<AsyncData<T>>.filterReadyValues(): Flow<T> {
    return this
        .filterIsInstance<AsyncData.Ready<T>>()
        .map { it.data }
}

fun <T> Flow<T>.toAsyncDataFlow(): Flow<AsyncData<T>> {
    return this
        .map { AsyncData.Ready(data = it) as AsyncData<T> }
        .onStart { emit(AsyncData.Loading) }
}

fun <T> AsyncData<T>.valueOr(default: () -> T): T {
    return when (this) {
        is AsyncData.Loading -> default()
        is AsyncData.Ready -> data
    }
}

fun <T> AsyncData<T>.valueOr(default: T): T {
    return when (this) {
        is AsyncData.Loading -> default
        is AsyncData.Ready -> data
    }
}

fun <T> AsyncData<T>.valueOrNull(): T? {
    return when (this) {
        is AsyncData.Loading -> null
        is AsyncData.Ready -> data
    }
}

fun <T, R> AsyncData<T>.map(transform: (T) -> R): AsyncData<R> {
    return when (this) {
        is AsyncData.Loading -> AsyncData.Loading
        is AsyncData.Ready -> AsyncData.Ready(transform(data))
    }
}
