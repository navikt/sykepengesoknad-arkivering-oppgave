package no.nav.syfo.util

import java.util.*
import java.util.function.BiConsumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream

object MapUtil {    // TODO: Brukes denne noe sted?

    fun <T, R, S : R> mapNullable(fra: T, til: S, exp: BiConsumer<T, R>): S {
        return Optional.ofNullable(fra).map { f ->
            exp.accept(f, til)
            til
        }.orElse(null)
    }

    fun <T, R, S : R> mapNullable(fra: T, til: S, exp: BiConsumer<T, R>, other: S): S {
        return Optional.ofNullable(fra).map { f ->
            exp.accept(f, til)
            til
        }.orElse(other)
    }

    fun <T, R, S : R> map(fra: T, til: S, exp: BiConsumer<T, R>): S {
        return Optional.of(fra).map { f ->
            exp.accept(f, til)
            til
        }.orElseThrow { RuntimeException("Resultatet fra exp ble null") }
    }

//    fun <T, U : T, R, S : R> mapListe(fra: List<U>, til: Supplier<S>, exp: BiConsumer<T, R>): List<S> {
//        return ofNullable(fra).map { f -> mapStream(f.stream(), til, exp).collect<List<S>, Any>(toList()) }.orElse(ArrayList())
//    }
//
//    fun <T, U : T, R, S : R> mapListe(fra: List<U>, til: Supplier<S>, filter: Predicate<U>, exp: BiConsumer<T, R>): List<S> {
//        return ofNullable(fra).map { f -> mapStream(f.stream().filter(filter), til, exp).collect<List<S>, Any>(toList()) }.orElse(ArrayList())
//    }

    fun <T, U : T, R, S : R> mapStream(fra: Stream<U>, til: Supplier<S>, exp: BiConsumer<T, R>): Stream<S> {
        return Optional.ofNullable(fra).map { f ->
            f.map { f1 ->
                val s = til.get()
                exp.accept(f1, s)
                s
            }
        }.orElse(Stream.empty())
    }

//    fun <T, U : T, R, S : R> mapListe(fra: List<U>, til: Function<U, S>, exp: BiConsumer<T, R>): List<S> {
//        return ofNullable(fra).map { f -> mapStream(f.stream(), til, exp).collect<List<S>, Any>(toList()) }.orElse(ArrayList())
//    }
//
//    fun <T, U : T, R, S : R> mapListe(fra: List<U>, til: Function<U, S>, filter: Predicate<U>, exp: BiConsumer<T, R>): List<S> {
//        return ofNullable(fra).map { f -> mapStream(f.stream().filter(filter), til, exp).collect<List<S>, Any>(toList()) }.orElse(ArrayList())
//    }

    fun <T, U : T, R, S : R> mapStream(fra: Stream<U>, til: Function<U, S>, exp: BiConsumer<T, R>): Stream<S> {
        return Optional.ofNullable(fra).map { f ->
            f.map { f1 ->
                val s = map(f1, til)
                exp.accept(f1, s)
                s
            }
        }.orElse(Stream.empty())
    }

    fun <T, R> mapNullable(fra: T, exp: Function<T, R>): R {
        return Optional.ofNullable(fra).map(exp).orElse(null)
    }

    fun <T, R> mapNullable(fra: T, exp: Function<T, R>, other: R): R {
        return Optional.ofNullable(fra).map(exp).orElse(other)
    }

    fun <T, R> mapNullable(fra: T, filter: Predicate<T>, exp: Function<T, R>): R {
        return Optional.ofNullable(fra).filter(filter).map(exp).orElse(null)
    }

    fun <T> mapNullable(fra: T, other: T): T {
        return Optional.ofNullable(fra).orElse(other)
    }

    fun <T, R> map(fra: T, exp: Function<T, R>): R {
        return Optional.of(fra).map(exp).orElseThrow { RuntimeException("Resultatet fra exp ble null") }
    }

    fun <T, R> mapMangetilEn(fra: List<T>, selector: Predicate<T>, exp: Function<T, R>): R {
        return Optional.ofNullable(fra).flatMap { g -> g.stream().filter(selector).map(exp).findFirst() }.orElse(null)
    }

//    fun <T, R> mapListe(fra: List<T>, filter: Predicate<T>, exp: Function<T, R>): List<R> {
//        return ofNullable(fra).map { f -> mapStream(f.stream().filter(filter), exp).collect<List<R>, Any>(toList()) }.orElse(ArrayList())
//    }
//
//    fun <T, R> mapListe(fra: List<T>, exp: Function<T, R>): List<R> {
//        return ofNullable(fra).map { f -> mapStream(f.stream(), exp).collect<List<R>, Any>(toList()) }.orElse(ArrayList())
//    }

    fun <T, R> mapStream(fra: Stream<T>, exp: Function<T, R>): Stream<R> {
        return Optional.ofNullable(fra).map { f -> f.map(exp) }.orElse(Stream.empty())
    }
}