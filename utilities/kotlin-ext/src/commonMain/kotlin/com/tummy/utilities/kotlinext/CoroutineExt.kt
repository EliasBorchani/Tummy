package com.tummy.utilities.kotlinext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Lance [block] dans [scope] et retourne le [Job]. Utilitaire léger pour éviter
 * le pattern `scope.launch { ... }` répété dans les VM.
 */
inline fun CoroutineScope.launching(crossinline block: suspend () -> Unit): Job =
    launch { block() }
