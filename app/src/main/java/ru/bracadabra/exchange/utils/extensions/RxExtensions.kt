package ru.bracadabra.exchange.utils.extensions

import io.reactivex.Observable

/**
 * Temporary workaround for simplicity. Should be replaced with custom operator.
 * */
fun <T, R> Observable<T>.mapNotNull(mapper: (T) -> R?): Observable<R> {
    return flatMap {
        val result = mapper(it)
        if (result == null) Observable.empty() else Observable.just(result)
    }
}
