package com.tummy.utilities.kotlinext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

fun <T> mutableStateFlow(initial: T): MutableStateFlow<T> = MutableStateFlow(initial)

fun <T> MutableStateFlow<T>.readOnly(): StateFlow<T> = asStateFlow()

/**
 * Mappe chaque valeur vers un R? puis dédoublonne sur les valeurs non nulles successives.
 * Combine `mapNotNull` + `distinctUntilChanged` — pattern fréquent côté VM.
 */
fun <T, R> Flow<T>.mapNotNullChanges(transform: suspend (T) -> R?): Flow<R> =
    mapNotNull(transform).distinctUntilChanged()
