package nu.westlin.http4k

import java.time.Instant

fun log(message: () -> String) {
    println("${Instant.now()}\t ${message()}")
}