package com.tummy.utilities.kotlinext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

fun <T> mutableStateFlow(initial: T): MutableStateFlow<T> = MutableStateFlow(initial)

fun <T> MutableStateFlow<T>.readOnly(): StateFlow<T> = asStateFlow()
