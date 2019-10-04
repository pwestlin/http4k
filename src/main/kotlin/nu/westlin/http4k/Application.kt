package nu.westlin.http4k

import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.time.Instant

// Inspiration: https://kotlinexpertise.com/kotlin-http4k/ and https://www.http4k.org/

data class Car(val brand: String, val model: String, val year: Int)

val carDB = listOf(
    Car("Porsche", "997", 2001),
    Car("Ferrari", "LaFerrari", 2011),
    Car("Volvo", "142", 1972)
)

val pingPongHandler: HttpHandler = { Response(OK).body("Pong!") }

val marcoPoloHandler: HttpHandler = { request: Request ->
    if (request.query("name") == "Marco") {
        Response(OK).body("Polo!")
    } else {
        Response(BAD_REQUEST)
    }
}

val carListLens = Body.auto<List<Car>>().toLens()

val allCarsHandler: HttpHandler = { Response(OK).with(carListLens of carDB) }

val routing: RoutingHttpHandler = routes(
    "/ping" bind GET to pingPongHandler,
    "/marco" bind GET to marcoPoloHandler,
    "/cars" bind GET to allCarsHandler
)

val requestTimeLogger: Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val start = System.currentTimeMillis()
        val response = next(request)
        val latency = System.currentTimeMillis() - start
        log { "Request to ${request.uri} took ${latency} ms" }
        response
    }
}

fun main() {
    log { "Server starting..." }

    requestTimeLogger
        .then(routing)
        .asServer(SunHttp(8080)).start().let { server ->
            log { "Server started on port ${server.port()}" }
        }
}

fun log(message: () -> String) {
    println("${Instant.now()}\t ${message()}")
}