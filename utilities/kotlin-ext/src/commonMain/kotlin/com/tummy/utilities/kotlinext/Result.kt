package com.tummy.utilities.kotlinext

/**
 * Résultat d'une opération domaine. Préféré à kotlin.Result pour l'interop Swift
 * (kotlin.Result est class-private côté iOS).
 */
sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Failure(val error: Throwable) : DomainResult<Nothing>
}

inline fun <T> runDomainCatching(block: () -> T): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (t: Throwable) {
    DomainResult.Failure(t)
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(value))
    is DomainResult.Failure -> this
}
