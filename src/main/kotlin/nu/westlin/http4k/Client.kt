package nu.westlin.http4k

import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request

fun main() {
    val client: HttpHandler = JavaHttpClient()

    ping(client)
    listAllCars(client)
}

fun listAllCars(client: HttpHandler) {
    val request = Request(Method.GET, "http://localhost:8080/cars")
    val response = client(request)
    log { "response = $response" }
    log { "carListLens.extract(response) = ${carListLens.extract(response)}" }
}

private fun ping(client: HttpHandler) {
    val request = Request(Method.GET, "http://localhost:8080/ping")
    val response = client(request)
    log { "response = $response" }
}
