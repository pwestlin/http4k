package nu.westlin.http4k

import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.format.Jackson.auto

fun main() {
    val client: HttpHandler = JavaHttpClient()

    ping(client)
    listAllCars(client)
    addNewCar(client)
}

fun addNewCar(client: HttpHandler) {
    val carLens = Body.auto<Car>().toLens()
    val car = Car("Mazda", "Miata", 1993)
    val request = Request(POST, "http://localhost:8080/cars").with(carLens of car)
    val response = client(request)
    log { "response = $response" }
}

fun listAllCars(client: HttpHandler) {
    val carListLens = Body.auto<List<Car>>().toLens()

    val request = Request(GET, "http://localhost:8080/cars")
    val response = client(request)
    log { "response = $response" }
    log { "carListLens.extract(response) = ${carListLens.extract(response)}" }
}

private fun ping(client: HttpHandler) {
    val request = Request(GET, "http://localhost:8080/ping")
    val response = client(request)
    log { "response = $response" }
}
